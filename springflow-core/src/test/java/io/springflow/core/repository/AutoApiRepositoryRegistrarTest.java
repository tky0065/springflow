package io.springflow.core.repository;

import io.springflow.annotations.AutoApi;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.scanner.EntityScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.mock.env.MockEnvironment;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoApiRepositoryRegistrarTest {

    private AutoApiRepositoryRegistrar registrar;
    private DefaultListableBeanFactory registry;

    @BeforeEach
    void setUp() {
        registrar = new AutoApiRepositoryRegistrar();
        registrar.setEnvironment(new MockEnvironment());
        registry = new DefaultListableBeanFactory();
    }

    @Test
    void postProcessBeanDefinitionRegistry_whenBeanExists_shouldSkipGeneration() {
        // Given
        registry.registerBeanDefinition("testEntityController", new GenericBeanDefinition());
        registry.registerBeanDefinition("testEntityService", new GenericBeanDefinition());
        registry.registerBeanDefinition("testEntityRepository", new GenericBeanDefinition());

        try (MockedConstruction<EntityScanner> scannerMock = mockConstruction(EntityScanner.class,
                (mock, context) -> {
                    when(mock.scanEntities(any())).thenReturn(Collections.singletonList(TestEntity.class));
                });
             MockedStatic<AutoConfigurationPackages> autoConfigMock = mockStatic(AutoConfigurationPackages.class)) {
            
            autoConfigMock.when(() -> AutoConfigurationPackages.get(any())).thenReturn(Collections.singletonList("io.springflow"));
            
            // When
            registrar.postProcessBeanDefinitionRegistry(registry);

            // Then
            BeanDefinition bd = registry.getBeanDefinition("testEntityController");
            assertThat(bd).isNotNull();
            // It should be our GenericBeanDefinition, not a SpringFlowControllerFactoryBean
            assertThat(bd.getBeanClassName()).isNull(); 
        }
    }

    @AutoApi
    static class TestEntity {
        private Long id;
    }
}
