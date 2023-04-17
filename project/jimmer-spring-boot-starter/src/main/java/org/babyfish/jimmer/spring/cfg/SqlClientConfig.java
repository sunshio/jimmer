package org.babyfish.jimmer.spring.cfg;

import com.fasterxml.jackson.databind.ObjectMapper;
import kotlin.Unit;
import org.babyfish.jimmer.spring.cloud.SpringCloudExchange;
import org.babyfish.jimmer.spring.repository.SpringConnectionManager;
import org.babyfish.jimmer.spring.repository.SpringTransientResolverProvider;
import org.babyfish.jimmer.sql.DraftInterceptor;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.cache.CacheFactory;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.event.TriggerType;
import org.babyfish.jimmer.sql.event.Triggers;
import org.babyfish.jimmer.sql.filter.Filter;
import org.babyfish.jimmer.sql.filter.Filters;
import org.babyfish.jimmer.sql.kt.KCaches;
import org.babyfish.jimmer.sql.kt.KSqlClient;
import org.babyfish.jimmer.sql.kt.KSqlClientKt;
import org.babyfish.jimmer.sql.kt.cfg.KCustomizer;
import org.babyfish.jimmer.sql.kt.cfg.KCustomizerKt;
import org.babyfish.jimmer.sql.kt.cfg.KInitializer;
import org.babyfish.jimmer.sql.kt.cfg.KInitializerKt;
import org.babyfish.jimmer.sql.kt.filter.KFilter;
import org.babyfish.jimmer.sql.kt.filter.KFilters;
import org.babyfish.jimmer.sql.kt.filter.impl.JavaFiltersKt;
import org.babyfish.jimmer.sql.runtime.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SqlClientConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlClientConfig.class);

    @Bean(name = "sqlClient")
    @ConditionalOnMissingBean({JSqlClient.class, KSqlClient.class})
    @ConditionalOnProperty(name = "jimmer.language", havingValue = "java", matchIfMissing = true)
    public JSqlClient javaSqlClient(
            ApplicationContext ctx,
            ApplicationEventPublisher publisher,
            JimmerProperties properties,
            @Autowired(required = false) DataSource dataSource,
            @Autowired(required = false) SpringConnectionManager connectionManager,
            @Autowired(required = false) SpringTransientResolverProvider transientResolverProvider,
            @Autowired(required = false) EntityManager entityManager,
            @Autowired(required = false) Dialect dialect,
            @Autowired(required = false) Executor executor,
            @Autowired(required = false) CacheFactory cacheFactory,
            @Autowired(required = false) MicroServiceExchange exchange,
            List<ScalarProvider<?, ?>> providers,
            List<DraftInterceptor<?>> interceptors,
            List<Filter<?>> javaFilters,
            List<KFilter<?>> kotlinFilters,
            List<Customizer> javaCustomizers,
            List<KCustomizer> kotlinCustomizers,
            List<Initializer> javaInitializers,
            List<KInitializer> kotlinInitializers
    ) {
        if (!kotlinFilters.isEmpty()) {
            LOGGER.warn(
                    "Jimmer is working in java mode, but some kotlin filters " +
                            "has been found in spring context, they will be ignored"
            );
        }
        if (!kotlinCustomizers.isEmpty()) {
            LOGGER.warn(
                    "Jimmer is working in java mode, but some kotlin customizers " +
                            "has been found in spring context, they will be ignored"
            );
        }
        if (!kotlinInitializers.isEmpty()) {
            LOGGER.warn(
                    "Jimmer is working in kotlin mode, but some kotlin initializers " +
                            "has been found in spring context, they will be ignored"
            );
        }
        JSqlClient.Builder builder = JSqlClient.newBuilder();
        preCreateSqlClient(
                builder,
                ctx,
                properties,
                dataSource,
                connectionManager,
                transientResolverProvider,
                entityManager,
                dialect,
                executor,
                cacheFactory,
                exchange,
                providers,
                interceptors,
                javaFilters,
                javaCustomizers,
                javaInitializers
        );
        JSqlClient sqlClient = builder.build();
        postCreateSqlClient(sqlClient, publisher);
        return sqlClient;
    }

    @Bean(name = "sqlClient")
    @ConditionalOnMissingBean({JSqlClient.class, KSqlClient.class})
    @ConditionalOnProperty(name = "jimmer.language", havingValue = "kotlin")
    public KSqlClient kotlinSqlClient(
            ApplicationContext ctx,
            ApplicationEventPublisher publisher,
            JimmerProperties properties,
            @Autowired(required = false) DataSource dataSource,
            @Autowired(required = false) SpringConnectionManager connectionManager,
            @Autowired(required = false) SpringTransientResolverProvider transientResolverProvider,
            @Autowired(required = false) EntityManager entityManager,
            @Autowired(required = false) Dialect dialect,
            @Autowired(required = false) Executor executor,
            @Autowired(required = false) CacheFactory cacheFactory,
            @Autowired(required = false) MicroServiceExchange exchange,
            List<ScalarProvider<?, ?>> providers,
            List<DraftInterceptor<?>> interceptors,
            List<Filter<?>> javaFilters,
            List<KFilter<?>> kotlinFilters,
            List<Customizer> javaCustomizers,
            List<KCustomizer> kotlinCustomizers,
            List<Initializer> javaInitializers,
            List<KInitializer> kotlinInitializers
    ) {
        if (!javaFilters.isEmpty()) {
            LOGGER.warn(
                    "Jimmer is working in kotlin mode, but some java filters " +
                            "has been found in spring context, they will be ignored"
            );
        }
        if (!javaCustomizers.isEmpty()) {
            LOGGER.warn(
                    "Jimmer is working in kotlin mode, but some java customizers " +
                            "has been found in spring context, they will be ignored"
            );
        }
        if (!javaInitializers.isEmpty()) {
            LOGGER.warn(
                    "Jimmer is working in kotlin mode, but some java initializers " +
                            "has been found in spring context, they will be ignored"
            );
        }
        KSqlClient sqlClient = KSqlClientKt.newKSqlClient(dsl -> {
            preCreateSqlClient(
                    dsl.getJavaBuilder(),
                    ctx,
                    properties,
                    dataSource,
                    connectionManager,
                    transientResolverProvider,
                    entityManager,
                    dialect,
                    executor,
                    cacheFactory,
                    exchange,
                    providers,
                    interceptors,
                    kotlinFilters
                            .stream()
                            .map(JavaFiltersKt::toJavaFilter)
                            .collect(Collectors.toList()),
                    kotlinCustomizers
                            .stream()
                            .map(KCustomizerKt::toJavaCustomizer)
                            .collect(Collectors.toList()),
                    kotlinInitializers
                            .stream()
                            .map(KInitializerKt::toJavaInitializer)
                            .collect(Collectors.toList())
            );
            return Unit.INSTANCE;
        });
        postCreateSqlClient(sqlClient.getJavaClient(), publisher);
        return sqlClient;
    }

    private static void preCreateSqlClient(
            JSqlClient.Builder builder,
            ApplicationContext ctx,
            JimmerProperties properties,
            DataSource dataSource,
            SpringConnectionManager connectionManager,
            SpringTransientResolverProvider transientResolverProvider,
            EntityManager entityManager,
            Dialect dialect,
            Executor executor,
            CacheFactory cacheFactory,
            MicroServiceExchange exchange,
            List<ScalarProvider<?, ?>> providers,
            List<DraftInterceptor<?>> interceptors,
            List<Filter<?>> filters,
            List<Customizer> customizers,
            List<Initializer> initializers
    ) {
        if (connectionManager != null) {
            builder.setConnectionManager(connectionManager);
        } else if (dataSource != null) {
            builder.setConnectionManager(new SpringConnectionManager(dataSource));
        }
        if (transientResolverProvider != null) {
            builder.setTransientResolverProvider(transientResolverProvider);
        } else {
            builder.setTransientResolverProvider(new SpringTransientResolverProvider(ctx));
        }

        if (entityManager != null) {
            builder.setEntityManager(entityManager);
        }

        builder.setDialect(dialect != null ? dialect : properties.getDialect());
        builder.setTriggerType(properties.getTriggerType());
        builder.setDefaultEnumStrategy(properties.getDefaultEnumStrategy());
        builder.setDefaultBatchSize(properties.getDefaultBatchSize());
        builder.setDefaultListBatchSize(properties.getDefaultListBatchSize());
        builder.setExecutorContextPrefixes(properties.getExecutorContextPrefixes());
        if (properties.isShowSql()) {
            builder.setExecutor(Executor.log(executor));
        } else {
            builder.setExecutor(executor);
        }
        builder.setDatabaseValidationMode(properties.getDatabaseValidation().getMode());
        builder.setDatabaseValidationCatalog(properties.getDatabaseValidation().getCatalog());
        if (cacheFactory != null) {
            builder.setCaches(cfg -> {
                cfg.setCacheFactory(cacheFactory);
            });
        }

        for (ScalarProvider<?, ?> provider : providers) {
            builder.addScalarProvider(provider);
        }

        builder.addDraftInterceptors(interceptors);
        builder.addFilters(filters);
        builder.addCustomizers(customizers);
        builder.addInitializers(initializers);

        builder.setMicroServiceName(properties.getMicroServiceName());
        if (!properties.getMicroServiceName().isEmpty()) {
            builder.setMicroServiceExchange(exchange);
        }
    }

    @Conditional(MicroServiceCondition.class)
    @ConditionalOnMissingBean(MicroServiceExchange.class)
    @Bean
    public MicroServiceExchange microServiceExchange(
            RestTemplate restTemplate,
            ObjectMapper mapper
    ) {
        return new SpringCloudExchange(restTemplate, mapper);
    }

    private static void postCreateSqlClient(
            JSqlClient sqlClient,
            ApplicationEventPublisher publisher
    ) {
        if (!(sqlClient.getConnectionManager() instanceof SpringConnectionManager)) {
            throw new IllegalStateException(
                    "The connection manager of sqlClient must be \"" +
                            SpringConnectionManager.class.getName() +
                            "\""
            );
        }

        if (sqlClient.getSlaveConnectionManager(false) != null &&
                !(sqlClient.getSlaveConnectionManager(false) instanceof SpringConnectionManager)) {
            throw new IllegalStateException(
                    "The slave connection manager of sqlClient must be null or \"" +
                            SpringConnectionManager.class.getName() +
                            "\""
            );
        }

        if (!SpringTransientResolverProvider.class.isAssignableFrom(sqlClient.getResolverProviderClass())) {
            throw new IllegalStateException(
                    "The transient resolver provider of sqlClient must be \"" +
                            SpringTransientResolverProvider.class.getName() +
                            "\""
            );
        }

        Triggers[] triggersArr = sqlClient.getTriggerType() == TriggerType.BOTH ?
                new Triggers[] { sqlClient.getTriggers(), sqlClient.getTriggers(true) } :
                new Triggers[] { sqlClient.getTriggers() };
        for (Triggers triggers : triggersArr) {
            triggers.addEntityListener(publisher::publishEvent);
            triggers.addAssociationListener(publisher::publishEvent);
        }
    }
}
