package io.springflow.core.repository;

import io.springflow.annotations.AutoApi;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.repository.support.SpringFlowRepositoryFactoryBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepositorySpecificationTest {

    @Test
    void shouldRegisterRepositoryWithSpecificationSupport() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        RepositoryGenerator generator = new RepositoryGenerator(registry);

        AutoApi autoApi = mock(AutoApi.class);
        when(autoApi.supportSpecification()).thenReturn(true);

        EntityMetadata metadata = new EntityMetadata(
                (Class) String.class,
                Long.class,
                "Product",
                "product",
                autoApi,
                java.util.Collections.emptyList()
        );

        generator.generate(metadata);

        BeanDefinition bd = registry.getBeanDefinition("productRepository");
        assertThat(bd.getPropertyValues().get("supportSpecification")).isEqualTo(true);
    }

    @Test
    void shouldRegisterRepositoryWithoutSpecificationSupportByDefault() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        RepositoryGenerator generator = new RepositoryGenerator(registry);

        AutoApi autoApi = mock(AutoApi.class);
        when(autoApi.supportSpecification()).thenReturn(false);

        EntityMetadata metadata = new EntityMetadata(
                (Class) String.class,
                Long.class,
                "Product",
                "product",
                autoApi,
                java.util.Collections.emptyList()
        );

        generator.generate(metadata);

        BeanDefinition bd = registry.getBeanDefinition("productRepository");
        assertThat(bd.getPropertyValues().get("supportSpecification")).isEqualTo(false);
    }
}
