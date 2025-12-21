package io.springflow.core.repository;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.repository.support.SpringFlowRepositoryFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.StringUtils;

public class RepositoryGenerator {

    private final BeanDefinitionRegistry registry;

    public RepositoryGenerator(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    public void generate(EntityMetadata metadata) {
        String beanName = getRepositoryBeanName(metadata);
        
        BeanDefinition beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(SpringFlowRepositoryFactoryBean.class)
                .addPropertyValue("entityClass", metadata.entityClass())
                .getBeanDefinition();

        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    public static String getRepositoryBeanName(EntityMetadata metadata) {
        return StringUtils.uncapitalize(metadata.entityName()) + "Repository";
    }
}
