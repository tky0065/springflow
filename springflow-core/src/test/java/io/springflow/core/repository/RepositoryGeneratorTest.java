package io.springflow.core.repository;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.repository.support.SpringFlowRepositoryFactoryBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static org.assertj.core.api.Assertions.assertThat;


class RepositoryGeneratorTest {

    @Test
    void generate_shouldRegisterBeanDefinition() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        RepositoryGenerator generator = new RepositoryGenerator(registry);

        EntityMetadata metadata = new EntityMetadata(
                (Class) String.class,
                Long.class,
                "Product",
                "product",
                null,
                java.util.Collections.emptyList()
        );

        generator.generate(metadata);

        assertThat(registry.containsBeanDefinition("productRepository")).isTrue();
        
        BeanDefinition bd = registry.getBeanDefinition("productRepository");
        assertThat(bd.getBeanClassName()).isEqualTo(SpringFlowRepositoryFactoryBean.class.getName());
        assertThat(bd.getPropertyValues().get("entityClass")).isEqualTo(String.class);
    }
}
