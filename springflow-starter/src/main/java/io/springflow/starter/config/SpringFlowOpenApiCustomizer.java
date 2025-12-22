package io.springflow.starter.config;

import io.springflow.core.controller.GenericCrudController;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.starter.openapi.OperationBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.*;

/**
 * OpenAPI customizer for SpringFlow-generated controllers.
 * <p>
 * This component hooks into springdoc's lifecycle to programmatically add
 * operations for dynamically generated controllers to the OpenAPI specification.
 * It runs AFTER all beans are initialized, solving the timing issue where
 * springdoc scans before dynamic controllers are registered.
 * </p>
 */
public class SpringFlowOpenApiCustomizer implements OpenApiCustomizer {

    private static final Logger log = LoggerFactory.getLogger(SpringFlowOpenApiCustomizer.class);

    @Autowired
    private ApplicationContext applicationContext;

    private final OperationBuilder operationBuilder;

    public SpringFlowOpenApiCustomizer() {
        this.operationBuilder = new OperationBuilder();
        log.info("SpringFlowOpenApiCustomizer bean created");
    }

    @Override
    public void customise(OpenAPI openApi) {
        log.info("Customizing OpenAPI specification for SpringFlow controllers");

        // Get all GenericCrudController beans
        Map<String, GenericCrudController> controllers =
                applicationContext.getBeansOfType(GenericCrudController.class);

        log.debug("Found {} SpringFlow controllers to document", controllers.size());

        // Get global base path
        String globalBasePath = getGlobalBasePath();

        // Initialize paths if null
        if (openApi.getPaths() == null) {
            openApi.setPaths(new Paths());
        }

        // Track tags to add
        Set<Tag> tagsToAdd = new HashSet<>();

        for (Map.Entry<String, GenericCrudController> entry : controllers.entrySet()) {
            String beanName = entry.getKey();

            try {
                // Get metadata from the factory bean (prefix with & to access factory)
                EntityMetadata metadata = getMetadataFromFactory(beanName);
                if (metadata == null) {
                    log.warn("No metadata found for controller bean: {}", beanName);
                    continue;
                }

                // Get entity path
                String entityPath = getEntityPath(beanName);
                String fullBasePath = combineBasePaths(globalBasePath, entityPath);

                // Determine tags
                String[] tags = getTags(metadata);

                // Add tag definitions
                for (String tagName : tags) {
                    Tag tag = new Tag();
                    tag.setName(tagName);
                    if (metadata.autoApiConfig() != null &&
                            !metadata.autoApiConfig().description().isEmpty()) {
                        tag.setDescription(metadata.autoApiConfig().description());
                    }
                    tagsToAdd.add(tag);
                }

                // Build and register all 5 CRUD operations
                addFindAllOperation(openApi, fullBasePath, metadata, tags);
                addFindByIdOperation(openApi, fullBasePath, metadata, tags);
                addCreateOperation(openApi, fullBasePath, metadata, tags);
                addUpdateOperation(openApi, fullBasePath, metadata, tags);
                addDeleteOperation(openApi, fullBasePath, metadata, tags);

                log.debug("Added OpenAPI operations for {} at {}",
                        metadata.entityName(), fullBasePath);

            } catch (Exception e) {
                log.error("Failed to customize OpenAPI for controller: {}", beanName, e);
            }
        }

        // Add all tags to OpenAPI
        if (openApi.getTags() == null) {
            openApi.setTags(new ArrayList<>());
        }
        openApi.getTags().addAll(tagsToAdd);

        log.info("OpenAPI customization completed for {} controllers", controllers.size());
    }

    /**
     * Add findAll operation (GET /).
     */
    private void addFindAllOperation(OpenAPI openApi, String basePath,
                                      EntityMetadata metadata, String[] tags) {
        Operation operation = operationBuilder.buildFindAllOperation(metadata, tags);
        addOperation(openApi, basePath, PathItem.HttpMethod.GET, operation);
    }

    /**
     * Add findById operation (GET /{id}).
     */
    private void addFindByIdOperation(OpenAPI openApi, String basePath,
                                       EntityMetadata metadata, String[] tags) {
        Operation operation = operationBuilder.buildFindByIdOperation(metadata, tags);
        String pathWithId = basePath + "/{id}";
        addOperation(openApi, pathWithId, PathItem.HttpMethod.GET, operation);
    }

    /**
     * Add create operation (POST /).
     */
    private void addCreateOperation(OpenAPI openApi, String basePath,
                                     EntityMetadata metadata, String[] tags) {
        Operation operation = operationBuilder.buildCreateOperation(metadata, tags);
        addOperation(openApi, basePath, PathItem.HttpMethod.POST, operation);
    }

    /**
     * Add update operation (PUT /{id}).
     */
    private void addUpdateOperation(OpenAPI openApi, String basePath,
                                     EntityMetadata metadata, String[] tags) {
        Operation operation = operationBuilder.buildUpdateOperation(metadata, tags);
        String pathWithId = basePath + "/{id}";
        addOperation(openApi, pathWithId, PathItem.HttpMethod.PUT, operation);
    }

    /**
     * Add delete operation (DELETE /{id}).
     */
    private void addDeleteOperation(OpenAPI openApi, String basePath,
                                     EntityMetadata metadata, String[] tags) {
        Operation operation = operationBuilder.buildDeleteOperation(metadata, tags);
        String pathWithId = basePath + "/{id}";
        addOperation(openApi, pathWithId, PathItem.HttpMethod.DELETE, operation);
    }

    /**
     * Add an operation to the OpenAPI spec.
     */
    private void addOperation(OpenAPI openApi, String path,
                              PathItem.HttpMethod method, Operation operation) {
        PathItem pathItem = openApi.getPaths().get(path);
        if (pathItem == null) {
            pathItem = new PathItem();
            openApi.getPaths().addPathItem(path, pathItem);
        }

        switch (method) {
            case GET -> pathItem.setGet(operation);
            case POST -> pathItem.setPost(operation);
            case PUT -> pathItem.setPut(operation);
            case DELETE -> pathItem.setDelete(operation);
            case PATCH -> pathItem.setPatch(operation);
        }
    }

    /**
     * Get entity metadata from the controller's factory bean.
     * <p>
     * The metadata is stored in SpringFlowControllerFactoryBean.
     * Access the factory bean by prefixing the bean name with "&".
     * </p>
     */
    private EntityMetadata getMetadataFromFactory(String beanName) {
        try {
            // Access factory bean by prefixing with &
            String factoryBeanName = "&" + beanName;
            Object factoryBean = applicationContext.getBean(factoryBeanName);

            if (factoryBean instanceof io.springflow.core.controller.support.SpringFlowControllerFactoryBean) {
                // Use reflection to access the metadata field
                java.lang.reflect.Field metadataField = factoryBean.getClass().getDeclaredField("metadata");
                metadataField.setAccessible(true);
                EntityMetadata metadata = (EntityMetadata) metadataField.get(factoryBean);
                log.debug("Retrieved metadata for {} from factory bean: {}", beanName, metadata != null ? metadata.entityName() : null);
                return metadata;
            } else {
                log.warn("Bean {} is not a SpringFlowControllerFactoryBean", factoryBeanName);
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to retrieve metadata from factory bean for {}: {}", beanName, e.getMessage());
            return null;
        }
    }

    /**
     * Get entity path from bean definition attributes.
     */
    private String getEntityPath(String beanName) {
        ConfigurableListableBeanFactory beanFactory =
                (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        try {
            Object attribute = beanFactory.getBeanDefinition(beanName)
                    .getAttribute("requestMapping");
            if (attribute != null) {
                return attribute.toString();
            }
        } catch (Exception e) {
            log.debug("Could not retrieve entity path for {}", beanName);
        }

        // Fallback: generate default path from bean name
        return "/" + beanName.replace("Controller", "").toLowerCase() + "s";
    }

    /**
     * Get global base path from configuration.
     */
    private String getGlobalBasePath() {
        Environment env = applicationContext.getEnvironment();
        String basePath = env.getProperty("springflow.base-path", "/api");
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }
        return basePath;
    }

    /**
     * Combine global base path with entity-specific path.
     */
    private String combineBasePaths(String globalBasePath, String entityPath) {
        if (globalBasePath.endsWith("/")) {
            globalBasePath = globalBasePath.substring(0, globalBasePath.length() - 1);
        }
        if (!entityPath.startsWith("/")) {
            entityPath = "/" + entityPath;
        }
        return globalBasePath + entityPath;
    }

    /**
     * Get tags for the entity from @AutoApi annotation or use default.
     */
    private String[] getTags(EntityMetadata metadata) {
        if (metadata.autoApiConfig() != null &&
                metadata.autoApiConfig().tags() != null &&
                metadata.autoApiConfig().tags().length > 0) {
            return metadata.autoApiConfig().tags();
        }

        // Default: use entity name as tag
        return new String[]{metadata.entityName()};
    }
}
