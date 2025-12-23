package io.springflow.core.controller.support;

import io.springflow.core.controller.GenericCrudController;
import io.springflow.core.filter.FilterResolver;
import io.springflow.core.mapper.DtoMapper;
import io.springflow.core.mapper.DtoMapperFactory;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import io.springflow.core.security.SecurityExpressionBuilder;
import io.springflow.core.service.GenericCrudService;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Factory bean for creating concrete controller instances.
 * <p>
 * This factory creates a concrete implementation of {@link GenericCrudController}
 * using Byte Buddy to dynamically add @PreAuthorize annotations if Spring Security is present.
 * </p>
 *
 * @param <T>  the entity type
 * @param <ID> the entity ID type
 */
public class SpringFlowControllerFactoryBean<T, ID> implements FactoryBean<GenericCrudController<T, ID>> {

    private static final Logger log = LoggerFactory.getLogger(SpringFlowControllerFactoryBean.class);
    private static final boolean SPRING_SECURITY_PRESENT = ClassUtils.isPresent(
            "org.springframework.security.access.prepost.PreAuthorize",
            SpringFlowControllerFactoryBean.class.getClassLoader()
    );

    private Class<T> entityClass;
    private GenericCrudService<T, ID> service;
    private DtoMapperFactory dtoMapperFactory;
    private FilterResolver filterResolver;
    private EntityMetadata metadata;
    private final SecurityExpressionBuilder securityExpressionBuilder = new SecurityExpressionBuilder();

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void setService(GenericCrudService<T, ID> service) {
        this.service = service;
    }

    public void setDtoMapperFactory(DtoMapperFactory dtoMapperFactory) {
        this.dtoMapperFactory = dtoMapperFactory;
    }

    public void setFilterResolver(FilterResolver filterResolver) {
        this.filterResolver = filterResolver;
    }

    public void setMetadata(EntityMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GenericCrudController<T, ID> getObject() throws Exception {
        DtoMapper<T, ID> dtoMapper = dtoMapperFactory.getMapper(entityClass, metadata);

        if (!SPRING_SECURITY_PRESENT || metadata.autoApiConfig().security() == null) {
            return createAnonymousController(dtoMapper);
        }

        try {
            return createSecureController(dtoMapper);
        } catch (Exception e) {
            log.error("Failed to create secure controller for {}, falling back to anonymous implementation", 
                    entityClass.getSimpleName(), e);
            return createAnonymousController(dtoMapper);
        }
    }

    private GenericCrudController<T, ID> createAnonymousController(DtoMapper<T, ID> dtoMapper) {
        return new GenericCrudController<T, ID>(service, dtoMapper, filterResolver, metadata, entityClass) {
            @Override
            protected ID getEntityId(T entity) {
                return extractIdFromEntity(entity);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private GenericCrudController<T, ID> createSecureController(DtoMapper<T, ID> dtoMapper) throws Exception {
        Class<? extends java.lang.annotation.Annotation> preAuthorizeClass = 
                (Class<? extends java.lang.annotation.Annotation>) 
                Class.forName("org.springframework.security.access.prepost.PreAuthorize");

        DynamicType.Builder<GenericCrudController> builder = (DynamicType.Builder<GenericCrudController>) new ByteBuddy()
                .subclass(GenericCrudController.class)
                .name(GenericCrudController.class.getName() + "$" + entityClass.getSimpleName() + "Impl");

        // Override CRUD methods and add method-specific @PreAuthorize
        String[] methodsToSecure = {"findAll", "findById", "create", "update", "delete"};
        for (String methodName : methodsToSecure) {
            Method method = findMethod(GenericCrudController.class, methodName);
            if (method != null) {
                String securityExpression = securityExpressionBuilder.buildExpression(metadata.autoApiConfig().security(), methodName);
                AnnotationDescription preAuthorizeAnnotation = AnnotationDescription.Builder
                        .ofType(preAuthorizeClass)
                        .define("value", securityExpression)
                        .build();

                builder = builder.method(ElementMatchers.is(method))
                        .intercept(MethodCall.invoke(method).onSuper().withAllArguments())
                        .annotateMethod(preAuthorizeAnnotation);
            }
        }

        // Override getEntityId
        Method getEntityIdMethod = findMethod(GenericCrudController.class, "getEntityId");
        builder = builder.method(ElementMatchers.is(getEntityIdMethod))
                .intercept(MethodCall.invoke(getClass().getDeclaredMethod("extractIdFromEntity", Object.class))
                        .on(this)
                        .withAllArguments());

        Class<? extends GenericCrudController<T, ID>> loadedClass = (Class<? extends GenericCrudController<T, ID>>) builder
                .make()
                .load(GenericCrudController.class.getClassLoader())
                .getLoaded();

        return loadedClass
                .getConstructor(GenericCrudService.class, DtoMapper.class, FilterResolver.class, EntityMetadata.class, Class.class)
                .newInstance(service, dtoMapper, filterResolver, metadata, entityClass);
    }

    private Method findMethod(Class<?> clazz, String name) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return GenericCrudController.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Extract the ID value from an entity using reflection.
     *
     * @param entity the entity
     * @return the ID value
     */
    @SuppressWarnings("unchecked")
    public ID extractIdFromEntity(Object entity) {
        if (entity == null || metadata == null) {
            return null;
        }

        // Find the ID field from metadata
        FieldMetadata idField = metadata.fields().stream()
                .filter(FieldMetadata::isId)
                .findFirst()
                .orElse(null);

        if (idField == null) {
            throw new IllegalStateException("No ID field found for entity: " + entityClass.getName());
        }

        try {
            Field field = idField.field();
            field.setAccessible(true);
            return (ID) field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to extract ID from entity: " + entityClass.getName(), e);
        }
    }
}
