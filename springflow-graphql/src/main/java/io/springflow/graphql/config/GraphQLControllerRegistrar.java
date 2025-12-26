package io.springflow.graphql.config;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.MetadataResolver;
import io.springflow.core.scanner.EntityScanner;
import io.springflow.graphql.generator.GraphQLControllerGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers dynamic GraphQL controller beans for each @AutoApi entity.
 * <p>
 * This post-processor runs during the bean factory initialization phase
 * and creates controller bean definitions before the application context is fully initialized.
 * </p>
 *
 * @author SpringFlow
 * @since 0.3.0
 */
@Slf4j
@Component
public class GraphQLControllerRegistrar implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof BeanDefinitionRegistry registry)) {
            log.warn("BeanFactory is not a BeanDefinitionRegistry. GraphQL controllers will not be generated.");
            return;
        }

        log.info("Starting GraphQL controller registration...");

        try {
            // Create instances of required components
            // Note: We can't use @Autowired here because this is a BeanFactoryPostProcessor
            EntityScanner entityScanner = new EntityScanner();
            MetadataResolver metadataResolver = new MetadataResolver();
            GraphQLControllerGenerator controllerGenerator = new GraphQLControllerGenerator();

            // Get base packages
            List<String> basePackages = determineBasePackages(beanFactory);
            log.debug("Scanning packages for @AutoApi entities: {}", basePackages);

            // Scan entities
            List<Class<?>> entities = entityScanner.scanEntities(basePackages.toArray(new String[0]));
            log.info("Found {} entities with @AutoApi for GraphQL controller generation", entities.size());

            if (entities.isEmpty()) {
                log.warn("No entities found with @AutoApi. GraphQL controllers will not be generated.");
                return;
            }

            // Generate controller for each entity
            for (Class<?> entityClass : entities) {
                EntityMetadata metadata = metadataResolver.resolve(entityClass);
                controllerGenerator.generateController(metadata, registry);
            }

            log.info("GraphQL controller registration completed. Registered {} controllers", entities.size());

        } catch (Exception e) {
            log.error("Failed to register GraphQL controllers", e);
            // Don't throw exception - allow application to start even if GraphQL controller generation fails
            log.warn("GraphQL controller generation failed. GraphQL API may not be available.");
        }
    }

    /**
     * Determines base packages to scan for entities.
     */
    private List<String> determineBasePackages(ConfigurableListableBeanFactory beanFactory) {
        List<String> packages = new ArrayList<>();

        try {
            String[] autoConfigPackages = (String[]) beanFactory.getBean("org.springframework.boot.autoconfigure.AutoConfigurationPackages")
                    .getClass().getMethod("get", Object.class)
                    .invoke(null, beanFactory);

            if (autoConfigPackages != null && autoConfigPackages.length > 0) {
                packages.addAll(List.of(autoConfigPackages));
            }
        } catch (Exception e) {
            log.debug("Could not determine auto-configuration packages", e);
        }

        if (packages.isEmpty()) {
            packages.add("com");
            packages.add("io");
            log.warn("Using fallback packages for entity scanning: {}", packages);
        }

        return packages;
    }
}
