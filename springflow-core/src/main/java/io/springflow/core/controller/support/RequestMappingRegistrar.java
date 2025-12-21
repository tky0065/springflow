package io.springflow.core.controller.support;

import io.springflow.core.controller.GenericCrudController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Bean post-processor that registers dynamically generated controllers with Spring MVC.
 * <p>
 * This component detects GenericCrudController instances and registers their request mappings
 * with the Spring MVC RequestMappingHandlerMapping.
 * </p>
 */
@Component
public class RequestMappingRegistrar implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(RequestMappingRegistrar.class);

    private ApplicationContext applicationContext;
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof GenericCrudController) {
            registerControllerMappings(bean, beanName);
        }
        return bean;
    }

    private void registerControllerMappings(Object controller, String beanName) {
        try {
            // Lazy initialization of RequestMappingHandlerMapping
            if (requestMappingHandlerMapping == null) {
                requestMappingHandlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
            }

            // Get the base path from bean definition attributes
            String basePath = getBasePath(beanName);

            log.debug("Registering controller mappings for {} with base path: {}", beanName, basePath);

            // Register each handler method
            Method[] methods = controller.getClass().getMethods();
            for (Method method : methods) {
                registerMethodMapping(controller, method, basePath);
            }

            log.info("Registered controller: {} with {} endpoints at {}",
                    beanName,
                    countMappedMethods(methods),
                    basePath);

        } catch (Exception e) {
            log.warn("Failed to register controller mappings for {}: {}", beanName, e.getMessage());
        }
    }

    private void registerMethodMapping(Object controller, Method method, String basePath) {
        try {
            // Check if method has request mapping annotation
            GetMapping getMapping = AnnotatedElementUtils.findMergedAnnotation(method, GetMapping.class);
            PostMapping postMapping = AnnotatedElementUtils.findMergedAnnotation(method, PostMapping.class);
            PutMapping putMapping = AnnotatedElementUtils.findMergedAnnotation(method, PutMapping.class);
            DeleteMapping deleteMapping = AnnotatedElementUtils.findMergedAnnotation(method, DeleteMapping.class);
            PatchMapping patchMapping = AnnotatedElementUtils.findMergedAnnotation(method, PatchMapping.class);

            String[] paths = null;
            RequestMethod requestMethod = null;

            if (getMapping != null) {
                paths = getMapping.value();
                requestMethod = RequestMethod.GET;
            } else if (postMapping != null) {
                paths = postMapping.value();
                requestMethod = RequestMethod.POST;
            } else if (putMapping != null) {
                paths = putMapping.value();
                requestMethod = RequestMethod.PUT;
            } else if (deleteMapping != null) {
                paths = deleteMapping.value();
                requestMethod = RequestMethod.DELETE;
            } else if (patchMapping != null) {
                paths = patchMapping.value();
                requestMethod = RequestMethod.PATCH;
            }

            if (requestMethod != null) {
                // Combine base path with method path
                String[] fullPaths = combinePaths(basePath, paths);

                RequestMappingInfo mappingInfo = RequestMappingInfo
                        .paths(fullPaths)
                        .methods(requestMethod)
                        .build();

                requestMappingHandlerMapping.registerMapping(mappingInfo, controller, method);

                log.debug("Registered mapping: {} {} -> {}.{}",
                        requestMethod,
                        fullPaths[0],
                        controller.getClass().getSimpleName(),
                        method.getName());
            }
        } catch (Exception e) {
            log.debug("Could not register method {}: {}", method.getName(), e.getMessage());
        }
    }

    private String[] combinePaths(String basePath, String[] methodPaths) {
        if (methodPaths == null || methodPaths.length == 0) {
            return new String[]{basePath};
        }

        return Arrays.stream(methodPaths)
                .map(path -> path.isEmpty() ? basePath : basePath + path)
                .toArray(String[]::new);
    }

    private String getBasePath(String beanName) {
        try {
            ConfigurableListableBeanFactory beanFactory =
                    (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

            Object attribute = beanFactory.getBeanDefinition(beanName).getAttribute("requestMapping");
            if (attribute != null) {
                return attribute.toString();
            }
        } catch (Exception e) {
            log.debug("Could not retrieve base path for {}: {}", beanName, e.getMessage());
        }

        // Default fallback
        return "/api";
    }

    private int countMappedMethods(Method[] methods) {
        return (int) Arrays.stream(methods)
                .filter(m -> AnnotatedElementUtils.hasAnnotation(m, GetMapping.class) ||
                            AnnotatedElementUtils.hasAnnotation(m, PostMapping.class) ||
                            AnnotatedElementUtils.hasAnnotation(m, PutMapping.class) ||
                            AnnotatedElementUtils.hasAnnotation(m, DeleteMapping.class) ||
                            AnnotatedElementUtils.hasAnnotation(m, PatchMapping.class))
                .count();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
