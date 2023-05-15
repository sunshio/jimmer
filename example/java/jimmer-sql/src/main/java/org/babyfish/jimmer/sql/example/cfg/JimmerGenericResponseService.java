package org.babyfish.jimmer.sql.example.cfg;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.*;
import org.apache.commons.lang3.ArrayUtils;
import org.babyfish.jimmer.client.meta.*;

import org.springdoc.core.*;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springdoc.core.SpringDocAnnotationsUtils.extractSchema;
import static org.springdoc.core.converters.ConverterUtils.isResponseTypeWrapper;

@Component
public class JimmerGenericResponseService extends GenericResponseService {

    /**
     * Instantiates a new Generic response builder.
     *
     * @param operationService          the operation builder
     * @param returnTypeParsers         the return type parsers
     * @param springDocConfigProperties the spring doc config properties
     * @param propertyResolverUtils     the property resolver utils
     */
    public JimmerGenericResponseService(OperationService operationService, List<ReturnTypeParser> returnTypeParsers, SpringDocConfigProperties springDocConfigProperties, PropertyResolverUtils propertyResolverUtils) {
        super(operationService, returnTypeParsers, springDocConfigProperties, propertyResolverUtils);
    }

    @Override
    public Content buildContent(Components components, Annotation[] annotations, String[] methodProduces, JsonView jsonView, Type returnType) {
        Map<Annotation[], Schema> map = JimmerReturnTypeParser.map;
        Content content = new Content();
        // if void, no content
        if (isVoid(returnType))
            return null;
        if (ArrayUtils.isNotEmpty(methodProduces)) {
            Schema<?> schemaN;
            if(map.get(annotations)!=null){
                schemaN = map.get(annotations);
            }else {
                schemaN = calculateSchema(components, returnType, jsonView, annotations);
            }
            if (schemaN != null) {
                MediaType mediaType = new MediaType();
                mediaType.setSchema(schemaN);
                // Fill the content
                setContent(methodProduces, content, mediaType);
            }
        }
        return content;
    }

    private void setContent(String[] methodProduces, Content content,
                            MediaType mediaType) {
        Arrays.stream(methodProduces).forEach(mediaTypeStr -> content.addMediaType(mediaTypeStr, mediaType));
    }

    private Schema<?> calculateSchema(Components components, Type returnType, JsonView jsonView, Annotation[] annotations) {
        if (!isVoid(returnType) && !SpringDocAnnotationsUtils.isAnnotationToIgnore(returnType))
            return extractSchema(components, returnType, jsonView, annotations);
        return null;
    }

    private boolean isVoid(Type returnType) {
        boolean result = false;
        if (Void.TYPE.equals(returnType) || Void.class.equals(returnType))
            result = true;
        else if (returnType instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) returnType).getActualTypeArguments();
            if (types != null && isResponseTypeWrapper(ResolvableType.forType(returnType).getRawClass()))
                result = isVoid(types[0]);
        }
        return result;
    }


    @Component
    private static class JimmerReturnTypeParser implements ReturnTypeParser {

        private static Map<Annotation[], Schema> map = new HashMap<>();

        @Override
        public Type getReturnType(MethodParameter methodParameter) {

            Method method = methodParameter.getMethod();
            Metadata metadata = SpringUtil.getBean(Metadata.class);


            Map<Class<?>, Service> services = metadata.getServices();

            Class<?> controllerClass = methodParameter.getDeclaringClass();
            Service service = services.get(controllerClass);
            List<Operation> operations = service.getOperations();
            for (org.babyfish.jimmer.client.meta.Operation operation : operations) {
                if(Objects.equals(operation.getRawMethod().toString(), method.toString())){
                    org.babyfish.jimmer.client.meta.Type type = operation.getType();
                    Schema m =  docTypeToSchema(type);
                    map.put(methodParameter.getParameterAnnotations(), m);
                }
            }
            return ReturnTypeParser.super.getReturnType(methodParameter);
        }


        private Schema docTypeToSchema(org.babyfish.jimmer.client.meta.Type type) {
            Schema schema = createSchemaByDocType(type);
            docTypeToSchema0(schema, type);
            return schema;
        }

        private void docTypeToSchema0(Schema schema, org.babyfish.jimmer.client.meta.Type type) {
            if(type instanceof ObjectType){
                Map<String, Property> properties = ((ObjectType) type).getProperties();
                Class<?> javaType = ((ObjectType) type).getJavaType();
                Map<String, Field> fieldMap = ReflectUtil.getFieldMap(javaType);
                properties.forEach((s, property) -> {
                    Schema childSchema = createSchemaByDocType(property.getType());
                    org.babyfish.jimmer.client.meta.Type elementType;
                    if(property.getType() instanceof ArrayType){
                        elementType = ((ArrayType) property.getType()).getElementType();
                    }else {
                        elementType = property.getType();
                    }

                    if(elementType instanceof NullableType){
                        elementType = ((NullableType) elementType).getTargetType();
                    }
                    boolean c = false;

                    if(elementType instanceof ObjectType){
                        Map<String, Property> properties1 = ((ObjectType) elementType).getProperties();
                        List<org.babyfish.jimmer.client.meta.Type> types = properties1.values().stream().map(Property::getType).collect(Collectors.toList());
                        List<org.babyfish.jimmer.client.meta.Type> arrayTypes = types.stream().filter(t -> t instanceof ArrayType).collect(Collectors.toList());
                        types.addAll(arrayTypes.stream().map(a->{
                            return ((ArrayType)a).getElementType();
                        }).collect(Collectors.toList()));
                        c = types.contains(type);
                    }

                    if(type != elementType && !c) {
                        docTypeToSchema0(childSchema, property.getType());
                    }
                    io.swagger.v3.oas.annotations.media.Schema schemaAnno = null;

                    if(type instanceof ImmutableObjectType){

                        try {
                            Method method = javaType.getMethod(s);
                            schemaAnno = method.getAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
                        } catch (NoSuchMethodException e) {

                        }
                    }else {
                        Field field = fieldMap.get(s);
                        if(field != null) {
                            schemaAnno = field.getAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
                        }
                    }
                    if(schemaAnno != null){
                        childSchema.setDescription(schemaAnno.description());
                    }

                    schema.addProperty(s, childSchema);
                });
            }else if(type instanceof NullableType){
                org.babyfish.jimmer.client.meta.Type targetType = ((NullableType) type).getTargetType();
                docTypeToSchema0(schema,targetType);
            }
            else if(type instanceof ArrayType){
                org.babyfish.jimmer.client.meta.Type elementType = ((ArrayType) type).getElementType();


                Schema schemaByDocType = createSchemaByDocType(elementType);

                docTypeToSchema0(schemaByDocType, elementType);

                ((ArraySchema)schema).items(schemaByDocType);
            }

            else if(type instanceof SimpleType){
                //TODO
                //格式等
            }
        }

        private Schema createSchemaByDocType(org.babyfish.jimmer.client.meta.Type type) {
            if(type instanceof ObjectType){
                return new ObjectSchema();
            }else if(type instanceof NullableType){
                org.babyfish.jimmer.client.meta.Type targetType = ((NullableType) type).getTargetType();
                return createSchemaByDocType(targetType);
            }else if(type instanceof EnumType){
                List<String> items = ((EnumType) type).getItems();
                StringSchema stringSchema = new StringSchema();
                stringSchema.setEnum(items);
                return stringSchema;
            }
            else if(type instanceof ArrayType){
                return new ArraySchema();
            }else if(type instanceof SimpleType){
                Class<?> javaType = ((SimpleType) type).getJavaType();
                if(javaType == String.class){
                    return new StringSchema();
                }else if(javaType == Date.class){
                    return new DateTimeSchema();
                }
                else if(javaType == LocalDate.class){
                    return new DateSchema();
                }else if(javaType == LocalDateTime.class){
                    return new DateTimeSchema();
                }else if(javaType == int.class || javaType == Integer.class){
                    return new IntegerSchema();
                }else if(javaType == Long.class || javaType == long.class){
                    IntegerSchema integerSchema = new IntegerSchema();
                    integerSchema.setFormat("int64");
                    return integerSchema;
                }else if(javaType == BigDecimal.class){
                    return new NumberSchema();
                }else if(javaType == boolean.class){
                    return new BooleanSchema();
                }
            }
            return new Schema();
        }
    }
}
