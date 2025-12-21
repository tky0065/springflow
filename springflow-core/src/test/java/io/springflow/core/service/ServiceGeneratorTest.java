package io.springflow.core.service;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.service.support.SpringFlowServiceFactoryBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceGeneratorTest {

    @Test
    void generate_shouldRegisterServiceBeanDefinition() {
        // Given
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        ServiceGenerator generator = new ServiceGenerator(registry);

        EntityMetadata metadata = new EntityMetadata(
                (Class) String.class,
                Long.class,
                "Product",
                "product",
                null,
                java.util.Collections.emptyList()
        );

        // Register a mock repository bean (required by service)
        registry.registerSingleton("productRepository", new Object());

        // When
        generator.generate(metadata);

        // Then
        assertThat(registry.containsBeanDefinition("productService")).isTrue();

        BeanDefinition bd = registry.getBeanDefinition("productService");
        assertThat(bd.getBeanClassName()).isEqualTo(SpringFlowServiceFactoryBean.class.getName());
        assertThat(bd.getPropertyValues().get("entityClass")).isEqualTo(String.class);
        assertThat(bd.getPropertyValues().get("repository")).isNotNull();
    }

    @Test
    void getServiceBeanName_shouldReturnCorrectName() {
        // Given
        EntityMetadata metadata = new EntityMetadata(
                (Class) String.class,
                Long.class,
                "Product",
                "product",
                null,
                java.util.Collections.emptyList()
        );

        // When
        String beanName = ServiceGenerator.getServiceBeanName(metadata);

        // Then
        assertThat(beanName).isEqualTo("productService");
    }
}
