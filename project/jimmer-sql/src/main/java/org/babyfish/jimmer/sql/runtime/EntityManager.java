package org.babyfish.jimmer.sql.runtime;

import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.JoinTable;
import org.babyfish.jimmer.sql.ManyToOne;
import org.babyfish.jimmer.sql.association.meta.AssociationType;
import org.babyfish.jimmer.sql.meta.MetadataStrategy;
import org.babyfish.jimmer.sql.meta.impl.DatabaseIdentifiers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EntityManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityManager.class);

    private final ReadWriteLock reloadingLock = new ReentrantReadWriteLock();

    private volatile Data data;

    public EntityManager(Class<?> ... classes) {
        this(Arrays.asList(classes));
    }

    public EntityManager(Collection<Class<?>> classes) {
        if (!(classes instanceof Set<?>)) {
            classes = new LinkedHashSet<>(classes);
        }
        Map<ImmutableType, ImmutableTypeInfo> map = new LinkedHashMap<>();
        for (Class<?> clazz : classes) {
            if (clazz != null) {
                ImmutableType immutableType = ImmutableType.get(clazz);
                if (!immutableType.isEntity()) {
                    throw new IllegalArgumentException(
                            "\"" +
                                    immutableType +
                                    "\" is not entity"
                    );
                }
                map.put(immutableType, new ImmutableTypeInfo());
                immutableType = immutableType.getSuperType();
                while (immutableType != null && (immutableType.isEntity() || immutableType.isMappedSuperclass())) {
                    map.put(immutableType, new ImmutableTypeInfo());
                    immutableType = immutableType.getSuperType();
                }
            }
        }
        for (Map.Entry<ImmutableType, ImmutableTypeInfo> e : map.entrySet()) {
            ImmutableType type = e.getKey();
            ImmutableTypeInfo info = e.getValue();
            if (type.isMappedSuperclass()) {
                for (ImmutableType otherType : map.keySet()) {
                    if (isImplementationType(type, otherType)) {
                        info.implementationTypes.add(otherType);
                    }
                }
            } else {
                for (ImmutableType otherType : map.keySet()) {
                    if (type != otherType && type.isAssignableFrom(otherType)) {
                        info.allDerivedTypes.add(otherType);
                        if (type == otherType.getSuperType()) {
                            info.directDerivedTypes.add(otherType);
                        }
                    }
                }
                for (ImmutableProp prop : type.getProps().values()) {
                    ImmutableType targetType = prop.getTargetType();
                    if (targetType != null && targetType.isEntity() && !prop.isRemote()) {
                        ImmutableTypeInfo targetInfo = map.get(targetType);
                        if (targetInfo == null) {
                            throw new IllegalArgumentException(
                                    "The target type \"" +
                                            targetType +
                                            "\" of the non-remote property \"" +
                                            prop +
                                            "\" is not manged by the current entity manager"
                            );
                        }
                        targetInfo.backProps.add(prop);
                    }
                }
            }
        }
        for (ImmutableTypeInfo info : map.values()) {
            info.implementationTypes = Collections.unmodifiableList(info.implementationTypes);
            info.directDerivedTypes = Collections.unmodifiableList(info.directDerivedTypes);
            info.allDerivedTypes = Collections.unmodifiableList(info.allDerivedTypes);
            info.backProps = Collections.unmodifiableList(
                    info
                            .backProps
                            .stream()
                            // sort is important, that means the order of cascade sql operations is fixed
                            .sorted(Comparator.comparing(ImmutableProp::toString))
                            .collect(Collectors.toList())
            );
        }
        map = Collections.unmodifiableMap(map);
        Map<String, ImmutableType> springDevToolMap = new HashMap<>((map.size() * 4 + 2) / 3);
        for (ImmutableType type : map.keySet()) {
            springDevToolMap.put(type.getJavaClass().getName(), type);
        }
        this.data = new Data(map, springDevToolMap);
    }

    public static EntityManager combine(EntityManager ... entityManagers) {
        if (entityManagers.length == 0) {
            throw new IllegalArgumentException("No entity managers");
        }
        if (entityManagers.length == 1) {
            return entityManagers[0];
        }
        Set<Class<?>> classes = new LinkedHashSet<>();
        for (EntityManager entityManager : entityManagers) {
            for (ImmutableType type : entityManager.getAllTypes(null)) {
                if (type.isEntity()) {
                    classes.add(type.getJavaClass());
                }
            }
        }
        return new EntityManager(classes);
    }

    public static EntityManager fromResources(
            @Nullable ClassLoader classLoader,
            @Nullable Predicate<Class<?>> predicate
    ) {
        if (classLoader == null) {
            classLoader = EntityManager.class.getClassLoader();
        }

        Set<Class<?>> classes = new LinkedHashSet<>();
        try {
            Enumeration<URL> urls = classLoader.getResources("META-INF/jimmer/entities");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                    while (true) {
                        String className = reader.readLine();
                        if (className == null) {
                            break;
                        }
                        className = className.trim();
                        if (!className.isEmpty()) {
                            Class<?> clazz;
                            try {
                                clazz = Class.forName(className, true, classLoader);
                            } catch (ClassNotFoundException ex) {
                                throw new IllegalStateException(
                                        "Cannot parse class name \"" +
                                                className +
                                                "\" in \"META-INF/jimmer/entities\"",
                                        ex
                                );
                            }
                            if (predicate == null || predicate.test(clazz)) {
                                classes.add(clazz);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load resources \"META-INF/jimmer/entities\"", ex);
        }
        return new EntityManager(classes);
    }

    public Set<ImmutableType> getAllTypes(String microServiceName) {
        if (microServiceName == null) {
            return data.map.keySet();
        }
        Set<ImmutableType> set = microServiceName.isEmpty() ?
                new LinkedHashSet<>((data.map.size() * 4 + 2) / 3) :
                new LinkedHashSet<>();
        for (ImmutableType type : data.map.keySet()) {
            if (type.getMicroServiceName().equals(microServiceName)) {
                set.add(type);
            }
        }
        return set;
    }

    public List<ImmutableType> getImplementationTypes(ImmutableType type) {
        return info(type).implementationTypes;
    }

    public List<ImmutableType> getDirectDerivedTypes(ImmutableType type) {
        return info(type).directDerivedTypes;
    }

    public List<ImmutableType> getAllDerivedTypes(ImmutableType type) {
        return info(type).allDerivedTypes;
    }

    public List<ImmutableProp> getAllBackProps(ImmutableType type) {
        return info(type).backProps;
    }

    private ImmutableTypeInfo info(ImmutableType type) {
        ImmutableTypeInfo info = data.map.get(type);
        if (info == null) {
            ImmutableType oldType = data.typeMapForSpringDevTools.get(type.getJavaClass().getName());
            if (oldType != null) {
                LOGGER.info(
                        "You seem to be using spring-dev-tools (or other multi-ClassLoader technology), " +
                                "so that some entity metadata changes but the ORM's entire metadata graph " +
                                "is not updated, now try to reload the EntityManager."
                );
                try {
                    reload(type);
                } catch (RuntimeException | Error ex) {
                    throw new IllegalStateException(
                            "You seem to be using spring-dev-tools (or other multi-ClassLoader technology), " +
                                    "so that some entity metadata changes but the ORM's entire metadata graph " +
                                    "is not updated, jimmer try to reload the EntityManager but meet some problem.",
                            ex
                    );
                }
                info = data.map.get(type);
            }
            if (info == null) {
                throw new IllegalArgumentException(
                        "\"" + type + "\" is not managed by current EntityManager"
                );
            }
        }
        return info;
    }

    private void reload(ImmutableType immutableType) {

        Lock lock;

        (lock = reloadingLock.readLock()).lock();
        try {
            if (data.map.containsKey(immutableType)) {
                return;
            }
        } finally {
            lock.unlock();
        }

        (lock = reloadingLock.writeLock()).lock();
        try {
            if (data.map.containsKey(immutableType)) {
                return;
            }
            EntityManager newEntityManager = EntityManager.fromResources(
                    immutableType.getJavaClass().getClassLoader(),
                    null
            );
            data = newEntityManager.data;
        } finally {
            lock.unlock();
        }
    }

    private boolean isImplementationType(ImmutableType mappedSuperClass, ImmutableType type) {
        if (!mappedSuperClass.isMappedSuperclass()) {
            throw new AssertionError("Internal bug");
        }
        if (!type.isEntity()) {
            return false;
        }
        if (!mappedSuperClass.isAssignableFrom(type)) {
            return false;
        }
        return type.getSuperType().isMappedSuperclass();
    }

    private static class ImmutableTypeInfo {

        // For mapped super class
        List<ImmutableType> implementationTypes = new ArrayList<>();

        // For entity
        List<ImmutableType> directDerivedTypes = new ArrayList<>();

        // For entity
        List<ImmutableType> allDerivedTypes = new ArrayList<>();

        // For entity
        List<ImmutableProp> backProps = new ArrayList<>();
    }

    public ImmutableType getTypeByServiceAndTable(
            String microServiceName,
            String tableName,
            MetadataStrategy strategy
    ) {
        Map<Key, ImmutableType> typeMap = data.getTypeMap(strategy);
        return typeMap.get(
                new Key(
                        Objects.requireNonNull(microServiceName, "`microServiceName` cannot be null"),
                        DatabaseIdentifiers.comparableIdentifier(
                                Objects.requireNonNull(tableName, "`tableName` cannot be null")
                        )
                )
        );
    }

    @NotNull
    public ImmutableType getNonNullTypeByServiceAndTable(
            String microServiceName,
            String tableName,
            MetadataStrategy strategy
    ) {
        ImmutableType type = getTypeByServiceAndTable(microServiceName, tableName, strategy);
        if (type == null) {
            throw new IllegalArgumentException(
                    "The table \"" +
                            tableName +
                            "\" of micro service \"" +
                            microServiceName +
                            "\" is not managed by current EntityManager"
            );
        }
        return type;
    }

    private static void tableSharedBy(Key key, ImmutableType type1, ImmutableType type2) {
        if (type1 instanceof AssociationType && type2 instanceof AssociationType) {
            AssociationType associationType1 = (AssociationType) type1;
            AssociationType associationType2 = (AssociationType) type2;
            if (associationType1.getSourceType() == associationType2.getTargetType() &&
                    associationType1.getTargetType() == associationType2.getSourceType()) {
                throw new IllegalArgumentException(
                        "Illegal entity manager, in the micro-service \"" +
                                key.microServiceName +
                                "\", the table \"" +
                                key.tableName +
                                "\" is shared by both \"" +
                                type1 +
                                "\" and \"" +
                                type2 +
                                "\". These two associations seem to form a bidirectional association, " +
                                "if so, please make one of them real (using @" +
                                JoinTable.class +
                                ") and the other image (specify `mappedBy` of @" +
                                ManyToOne.class +
                                ")"
                );
            }
        }
        throw new IllegalArgumentException(
                "Illegal entity manager, in the microservice \"" +
                        key.microServiceName +
                        "\", the table \"" +
                        key.tableName +
                        "\" is shared by both \"" +
                        type1 +
                        "\" and \"" +
                        type2 +
                        "\""
        );
    }

    private static class Key {

        final String microServiceName;

        final String tableName;

        private Key(String microServiceName, String tableName) {
            this.microServiceName = microServiceName;
            this.tableName = tableName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return microServiceName.equals(key.microServiceName) && tableName.equals(key.tableName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(microServiceName, tableName);
        }

        @Override
        public String toString() {
            return "Key{" +
                    "microServiceName='" + microServiceName + '\'' +
                    ", tableName='" + tableName + '\'' +
                    '}';
        }
    }

    private static class Data {

        final Map<ImmutableType, ImmutableTypeInfo> map;

        final Map<String, ImmutableType> typeMapForSpringDevTools;

        final ConcurrentMap<MetadataStrategy, Map<Key, ImmutableType>> typesMap =
                new ConcurrentHashMap<>();

        Data(Map<ImmutableType, ImmutableTypeInfo> map, Map<String, ImmutableType> typeMapForSpringDevTools) {
            this.map = map;
            this.typeMapForSpringDevTools = typeMapForSpringDevTools;
        }

        public Map<Key, ImmutableType> getTypeMap(MetadataStrategy strategy) {
            return typesMap.computeIfAbsent(strategy, this::createTypeMap);
        }

        private Map<Key, ImmutableType> createTypeMap(MetadataStrategy strategy) {
            Map<Key, ImmutableType> typeMap = new HashMap<>();
            for (ImmutableType type : map.keySet()) {
                if (!type.isEntity()) {
                    continue;
                }
                String tableName = DatabaseIdentifiers.comparableIdentifier(type.getTableName(strategy));
                String microServiceName = type.getMicroServiceName();
                Key key = new Key(microServiceName, tableName);
                ImmutableType conflictType = typeMap.put(key, type);
                if (conflictType != null) {
                    tableSharedBy(key, conflictType, type);
                }
                for (ImmutableProp prop : type.getProps().values()) {
                    if (prop.isMiddleTableDefinition()) {
                        AssociationType associationType = AssociationType.of(prop);
                        String associationTableName = DatabaseIdentifiers.comparableIdentifier(
                                associationType.getTableName(strategy)
                        );
                        key = new Key(microServiceName, associationTableName);
                        conflictType = typeMap.put(key, associationType);
                        if (conflictType != null) {
                            tableSharedBy(key, conflictType, associationType);
                        }
                    }
                }
            }
            return typeMap;
        }
    }
}
