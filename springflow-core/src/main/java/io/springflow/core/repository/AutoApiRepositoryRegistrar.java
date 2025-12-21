package io.springflow.core.repository;

import io.springflow.core.controller.ControllerGenerator;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.MetadataResolver;
import io.springflow.core.scanner.EntityScanner;
import io.springflow.core.service.ServiceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AutoApiRepositoryRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, ResourceLoaderAware {

    private static final Logger log = LoggerFactory.getLogger(AutoApiRepositoryRegistrar.class);

    private Environment environment;
    private ResourceLoader resourceLoader;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        log.info("Starting AutoApi Repository, Service, and Controller Registration...");

        List<String> packages = getPackagesToScan(registry);
        if (packages.isEmpty()) {
            log.warn("No packages found to scan for @AutoApi entities.");
            return;
        }

        EntityScanner scanner = new EntityScanner();
        List<Class<?>> entities = scanner.scanEntities(packages.toArray(new String[0]));

        MetadataResolver resolver = new MetadataResolver();
        RepositoryGenerator repositoryGenerator = new RepositoryGenerator(registry);
        ServiceGenerator serviceGenerator = new ServiceGenerator(registry);
        ControllerGenerator controllerGenerator = new ControllerGenerator(registry);

        for (Class<?> entityClass : entities) {
            try {
                EntityMetadata metadata = resolver.resolve(entityClass);

                // Generate repository
                repositoryGenerator.generate(metadata);
                log.debug("Registered repository for {}", entityClass.getSimpleName());

                // Generate service
                serviceGenerator.generate(metadata);
                log.debug("Registered service for {}", entityClass.getSimpleName());

                // Generate controller
                controllerGenerator.generate(metadata);
                log.debug("Registered controller for {}", entityClass.getSimpleName());

            } catch (Exception e) {
                log.error("Failed to generate repository/service/controller for {}", entityClass.getName(), e);
            }
        }

        log.info("AutoApi registration completed. Registered {} entities.", entities.size());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // no-op
    }

    private List<String> getPackagesToScan(BeanDefinitionRegistry registry) {
        if (registry instanceof ConfigurableListableBeanFactory) {
            try {
                return AutoConfigurationPackages.get((BeanFactory) registry);
            } catch (Exception e) {
                log.warn("Could not determine auto-configuration packages.", e);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
