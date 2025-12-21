package io.springflow.core.controller.support;

import io.springflow.core.controller.GenericCrudController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * Registers dynamically generated controllers with Spring MVC after application context is fully initialized.
 * <p>
 * This component detects GenericCrudController instances and registers their request mappings
 * with the Spring MVC RequestMappingHandlerMapping once the context is refreshed.
 * </p>
 */
@Component
public class RequestMappingRegistrar implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(RequestMappingRegistrar.class);

    private boolean registered = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Only register once (in case of parent-child contexts)
        if (registered) {
            return;
        }
        registered = true;

        ApplicationContext applicationContext = event.getApplicationContext();

        try {
            RequestMappingHandlerMapping handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);

            // Get global base path from environment
            String globalBasePath = getGlobalBasePath(applicationContext);

            // Find all GenericCrudController beans
            Map<String, GenericCrudController> controllers = applicationContext.getBeansOfType(GenericCrudController.class);

            log.info("Registering {} SpringFlow controllers with Spring MVC at base path: {}", controllers.size(), globalBasePath);

            for (Map.Entry<String, GenericCrudController> entry : controllers.entrySet()) {
                registerControllerMappings(entry.getValue(), entry.getKey(), handlerMapping, applicationContext, globalBasePath);
            }

        } catch (Exception e) {
            log.error("Failed to register controller mappings", e);
        }
    }

    private String getGlobalBasePath(ApplicationContext applicationContext) {
        try {
            Environment env = applicationContext.getEnvironment();
            String basePath = env.getProperty("springflow.base-path", "/api");
            // Ensure it starts with /
            if (!basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }
            return basePath;
        } catch (Exception e) {
            log.debug("Could not retrieve global base path, using default: /api");
            return "/api";
        }
    }

    private void registerControllerMappings(Object controller, String beanName,
                                           RequestMappingHandlerMapping handlerMapping,
                                           ApplicationContext applicationContext,
                                           String globalBasePath) {
        try {
            // Get the entity path from bean definition attributes
            String entityPath = getEntityPath(beanName, applicationContext);

            // Combine global base path with entity path
            String fullBasePath = combineBasePaths(globalBasePath, entityPath);

            log.debug("Registering controller mappings for {} with full path: {}", beanName, fullBasePath);

            // Register each handler method
            Method[] methods = controller.getClass().getMethods();
            int registeredCount = 0;

            for (Method method : methods) {
                if (registerMethodMapping(controller, method, fullBasePath, handlerMapping)) {
                    registeredCount++;
                }
            }

            log.info("Registered controller: {} with {} endpoints at {}",
                    beanName,
                    registeredCount,
                    fullBasePath);

        } catch (Exception e) {
            log.error("Failed to register controller mappings for {}: {}", beanName, e.getMessage(), e);
        }
    }

    private String combineBasePaths(String globalBasePath, String entityPath) {
        // Remove trailing slash from global base path
        if (globalBasePath.endsWith("/")) {
            globalBasePath = globalBasePath.substring(0, globalBasePath.length() - 1);
        }

        // Ensure entity path starts with /
        if (!entityPath.startsWith("/")) {
            entityPath = "/" + entityPath;
        }

        return globalBasePath + entityPath;
    }

    private boolean registerMethodMapping(Object controller, Method method, String basePath,
                                         RequestMappingHandlerMapping handlerMapping) {
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

                handlerMapping.registerMapping(mappingInfo, controller, method);

                log.debug("Registered mapping: {} {} -> {}.{}",
                        requestMethod,
                        fullPaths[0],
                        controller.getClass().getSimpleName(),
                        method.getName());

                return true;
            }
        } catch (Exception e) {
            log.debug("Could not register method {}: {}", method.getName(), e.getMessage());
        }

        return false;
    }

    private String[] combinePaths(String basePath, String[] methodPaths) {
        if (methodPaths == null || methodPaths.length == 0) {
            return new String[]{basePath};
        }

        return Arrays.stream(methodPaths)
                .map(path -> path.isEmpty() ? basePath : basePath + path)
                .toArray(String[]::new);
    }

    private String getEntityPath(String beanName, ApplicationContext applicationContext) {
        try {
            ConfigurableListableBeanFactory beanFactory =
                    (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

            Object attribute = beanFactory.getBeanDefinition(beanName).getAttribute("requestMapping");
            if (attribute != null) {
                return attribute.toString();
            }
        } catch (Exception e) {
            log.debug("Could not retrieve entity path for {}: {}", beanName, e.getMessage());
        }

        // Default fallback: derive from bean name
        String entityName = beanName.replace("Controller", "");
        return "/" + entityName.toLowerCase() + "s";
    }
}
