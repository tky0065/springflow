package io.springflow.core.controller.support;

import io.springflow.core.controller.GenericCrudController;
import io.springflow.core.filter.FilterResolver;
import io.springflow.core.mapper.DtoMapper;
import io.springflow.core.mapper.DtoMapperFactory;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import io.springflow.core.security.SecurityExpressionBuilder;
import io.springflow.core.service.GenericCrudService;
import io.springflow.core.utils.EntityUtils;
import io.springflow.core.validation.EntityValidator;
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
    private EntityValidator entityValidator;
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

    public void setEntityValidator(EntityValidator entityValidator) {
        this.entityValidator = entityValidator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GenericCrudController<T, ID> getObject() throws Exception {
        DtoMapper<T, ID> dtoMapper = dtoMapperFactory.getMapper(entityClass, metadata);

        boolean hasSecurity = metadata.securedApiConfig() != null || 
                             (metadata.autoApiConfig().security() != null && metadata.autoApiConfig().security().enabled());

        if (!SPRING_SECURITY_PRESENT || !hasSecurity) {
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
        return new GenericCrudController<T, ID>(service, dtoMapper, filterResolver, metadata, entityClass, entityValidator) {
            @Override
            protected ID getEntityId(T entity) {
                return EntityUtils.getEntityId(entity, metadata);
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
                .name("io.springflow.generated.controller." + entityClass.getSimpleName() + "AutoController" + System.nanoTime());

        // Override all mapped methods and add method-specific @PreAuthorize
        for (Method method : GenericCrudController.class.getDeclaredMethods()) {
            if (isMappedMethod(method)) {
                String methodName = method.getName();
                String securityExpression;
                if (metadata.securedApiConfig() != null) {
                    securityExpression = securityExpressionBuilder.buildExpression(metadata.securedApiConfig(), methodName);
                } else {
                    securityExpression = securityExpressionBuilder.buildExpression(metadata.autoApiConfig().security(), methodName);
                }
                
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
                .intercept(MethodCall.invoke(EntityUtils.class.getDeclaredMethod("getEntityId", Object.class, EntityMetadata.class))
                        .withArgument(0) // entity
                        .with(metadata)); // metadata

        Class<? extends GenericCrudController<T, ID>> loadedClass = (Class<? extends GenericCrudController<T, ID>>) builder
                .make()
                .load(GenericCrudController.class.getClassLoader(), net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();

        return loadedClass
                .getConstructor(GenericCrudService.class, DtoMapper.class, FilterResolver.class, EntityMetadata.class, Class.class, EntityValidator.class)
                .newInstance(service, dtoMapper, filterResolver, metadata, entityClass, entityValidator);
    }

    private boolean isMappedMethod(Method method) {
        return method.isAnnotationPresent(org.springframework.web.bind.annotation.RequestMapping.class) ||
               method.isAnnotationPresent(org.springframework.web.bind.annotation.GetMapping.class) ||
               method.isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class) ||
               method.isAnnotationPresent(org.springframework.web.bind.annotation.PutMapping.class) ||
               method.isAnnotationPresent(org.springframework.web.bind.annotation.DeleteMapping.class) ||
               method.isAnnotationPresent(org.springframework.web.bind.annotation.PatchMapping.class);
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
}
