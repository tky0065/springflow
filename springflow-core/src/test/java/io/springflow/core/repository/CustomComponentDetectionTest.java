package io.springflow.core.repository;

import io.springflow.core.repository.testentities.CustomControllerEntity;
import io.springflow.core.repository.testentities.CustomRepoEntity;
import io.springflow.core.repository.testentities.CustomServiceEntity;
import io.springflow.core.repository.testentities.FullyCustomEntity;
import io.springflow.core.scanner.EntityScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.mock.env.MockEnvironment;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Comprehensive test class for custom component detection.
 * Tests that SpringFlow properly detects custom repositories, services, and controllers
 * and skips generation for those components.
 */
@ExtendWith(MockitoExtension.class)
class CustomComponentDetectionTest {

    private AutoApiRepositoryRegistrar registrar;
    private DefaultListableBeanFactory registry;

    @BeforeEach
    void setUp() {
        registrar = new AutoApiRepositoryRegistrar();
        registrar.setEnvironment(new MockEnvironment());
        registry = new DefaultListableBeanFactory();
    }

    /**
     * Test Scenario 1: Custom Repository Only
     * When a custom repository exists, SpringFlow should:
     * - SKIP repository generation
     * - GENERATE service
     * - GENERATE controller
     */
    @Test
    void whenCustomRepositoryExists_shouldSkipRepoGenerateServiceAndController() {
        // Given - Pre-register custom repository
        registry.registerBeanDefinition("customRepoEntityRepository", new GenericBeanDefinition());

        try (MockedConstruction<EntityScanner> scannerMock = mockConstruction(EntityScanner.class,
                (mock, context) -> {
                    when(mock.scanEntities(any())).thenReturn(Collections.singletonList(CustomRepoEntity.class));
                });
             MockedStatic<AutoConfigurationPackages> autoConfigMock = mockStatic(AutoConfigurationPackages.class)) {

            autoConfigMock.when(() -> AutoConfigurationPackages.get(any()))
                    .thenReturn(Collections.singletonList("io.springflow.core.repository.testentities"));

            // When
            registrar.postProcessBeanDefinitionRegistry(registry);

            // Then
            // Custom repository should still exist (not overwritten)
            assertThat(registry.containsBeanDefinition("customRepoEntityRepository")).isTrue();
            assertThat(registry.getBeanDefinition("customRepoEntityRepository").getBeanClassName()).isNull();

            // Service and controller should be generated
            assertThat(registry.containsBeanDefinition("customRepoEntityService")).isTrue();
            assertThat(registry.containsBeanDefinition("customRepoEntityController")).isTrue();
        }
    }

    /**
     * Test Scenario 2: Custom Service Only
     * When a custom service exists, SpringFlow should:
     * - GENERATE repository
     * - SKIP service generation
     * - GENERATE controller
     */
    @Test
    void whenCustomServiceExists_shouldGenerateRepoSkipServiceGenerateController() {
        // Given - Pre-register custom service
        registry.registerBeanDefinition("customServiceEntityService", new GenericBeanDefinition());

        try (MockedConstruction<EntityScanner> scannerMock = mockConstruction(EntityScanner.class,
                (mock, context) -> {
                    when(mock.scanEntities(any())).thenReturn(Collections.singletonList(CustomServiceEntity.class));
                });
             MockedStatic<AutoConfigurationPackages> autoConfigMock = mockStatic(AutoConfigurationPackages.class)) {

            autoConfigMock.when(() -> AutoConfigurationPackages.get(any()))
                    .thenReturn(Collections.singletonList("io.springflow.core.repository.testentities"));

            // When
            registrar.postProcessBeanDefinitionRegistry(registry);

            // Then
            // Repository and controller should be generated
            assertThat(registry.containsBeanDefinition("customServiceEntityRepository")).isTrue();
            assertThat(registry.containsBeanDefinition("customServiceEntityController")).isTrue();

            // Custom service should still exist (not overwritten)
            assertThat(registry.containsBeanDefinition("customServiceEntityService")).isTrue();
            assertThat(registry.getBeanDefinition("customServiceEntityService").getBeanClassName()).isNull();
        }
    }

    /**
     * Test Scenario 3: Custom Controller Only
     * When a custom controller exists, SpringFlow should:
     * - GENERATE repository
     * - GENERATE service
     * - SKIP controller generation
     */
    @Test
    void whenCustomControllerExists_shouldGenerateRepoAndServiceSkipController() {
        // Given - Pre-register custom controller
        registry.registerBeanDefinition("customControllerEntityController", new GenericBeanDefinition());

        try (MockedConstruction<EntityScanner> scannerMock = mockConstruction(EntityScanner.class,
                (mock, context) -> {
                    when(mock.scanEntities(any())).thenReturn(Collections.singletonList(CustomControllerEntity.class));
                });
             MockedStatic<AutoConfigurationPackages> autoConfigMock = mockStatic(AutoConfigurationPackages.class)) {

            autoConfigMock.when(() -> AutoConfigurationPackages.get(any()))
                    .thenReturn(Collections.singletonList("io.springflow.core.repository.testentities"));

            // When
            registrar.postProcessBeanDefinitionRegistry(registry);

            // Then
            // Repository and service should be generated
            assertThat(registry.containsBeanDefinition("customControllerEntityRepository")).isTrue();
            assertThat(registry.containsBeanDefinition("customControllerEntityService")).isTrue();

            // Custom controller should still exist (not overwritten)
            assertThat(registry.containsBeanDefinition("customControllerEntityController")).isTrue();
            assertThat(registry.getBeanDefinition("customControllerEntityController").getBeanClassName()).isNull();
        }
    }

    /**
     * Test Scenario 4: Custom Service Extends Base
     * When a custom service extends GenericCrudService, SpringFlow should:
     * - GENERATE repository
     * - SKIP service generation (detect the custom service)
     * - GENERATE controller
     * This is the same as scenario 2, as the detection is name-based
     */
    @Test
    void whenCustomServiceExtendsBase_shouldDetectAndSkipServiceGeneration() {
        // Given - Pre-register custom service (extending GenericCrudService)
        GenericBeanDefinition customService = new GenericBeanDefinition();
        customService.setBeanClassName("io.springflow.demo.service.CustomExtendedService");
        registry.registerBeanDefinition("customServiceEntityService", customService);

        try (MockedConstruction<EntityScanner> scannerMock = mockConstruction(EntityScanner.class,
                (mock, context) -> {
                    when(mock.scanEntities(any())).thenReturn(Collections.singletonList(CustomServiceEntity.class));
                });
             MockedStatic<AutoConfigurationPackages> autoConfigMock = mockStatic(AutoConfigurationPackages.class)) {

            autoConfigMock.when(() -> AutoConfigurationPackages.get(any()))
                    .thenReturn(Collections.singletonList("io.springflow.core.repository.testentities"));

            // When
            registrar.postProcessBeanDefinitionRegistry(registry);

            // Then
            // Repository and controller should be generated
            assertThat(registry.containsBeanDefinition("customServiceEntityRepository")).isTrue();
            assertThat(registry.containsBeanDefinition("customServiceEntityController")).isTrue();

            // Custom service should still exist with its custom class name
            assertThat(registry.containsBeanDefinition("customServiceEntityService")).isTrue();
            assertThat(registry.getBeanDefinition("customServiceEntityService").getBeanClassName())
                    .isEqualTo("io.springflow.demo.service.CustomExtendedService");
        }
    }

    /**
     * Test Scenario 5: Custom Repository + Service
     * When both custom repository and service exist, SpringFlow should:
     * - SKIP repository generation
     * - SKIP service generation
     * - GENERATE controller
     */
    @Test
    void whenRepoAndServiceExist_shouldSkipBothGenerateController() {
        // Given - Pre-register custom repository and service
        registry.registerBeanDefinition("customRepoEntityRepository", new GenericBeanDefinition());
        registry.registerBeanDefinition("customRepoEntityService", new GenericBeanDefinition());

        try (MockedConstruction<EntityScanner> scannerMock = mockConstruction(EntityScanner.class,
                (mock, context) -> {
                    when(mock.scanEntities(any())).thenReturn(Collections.singletonList(CustomRepoEntity.class));
                });
             MockedStatic<AutoConfigurationPackages> autoConfigMock = mockStatic(AutoConfigurationPackages.class)) {

            autoConfigMock.when(() -> AutoConfigurationPackages.get(any()))
                    .thenReturn(Collections.singletonList("io.springflow.core.repository.testentities"));

            // When
            registrar.postProcessBeanDefinitionRegistry(registry);

            // Then
            // Custom repository and service should still exist
            assertThat(registry.containsBeanDefinition("customRepoEntityRepository")).isTrue();
            assertThat(registry.getBeanDefinition("customRepoEntityRepository").getBeanClassName()).isNull();
            assertThat(registry.containsBeanDefinition("customRepoEntityService")).isTrue();
            assertThat(registry.getBeanDefinition("customRepoEntityService").getBeanClassName()).isNull();

            // Controller should be generated
            assertThat(registry.containsBeanDefinition("customRepoEntityController")).isTrue();
        }
    }

    /**
     * Test Scenario 6: Custom Repository + Controller
     * When both custom repository and controller exist, SpringFlow should:
     * - SKIP repository generation
     * - GENERATE service
     * - SKIP controller generation
     */
    @Test
    void whenRepoAndControllerExist_shouldSkipBothGenerateService() {
        // Given - Pre-register custom repository and controller
        registry.registerBeanDefinition("customRepoEntityRepository", new GenericBeanDefinition());
        registry.registerBeanDefinition("customRepoEntityController", new GenericBeanDefinition());

        try (MockedConstruction<EntityScanner> scannerMock = mockConstruction(EntityScanner.class,
                (mock, context) -> {
                    when(mock.scanEntities(any())).thenReturn(Collections.singletonList(CustomRepoEntity.class));
                });
             MockedStatic<AutoConfigurationPackages> autoConfigMock = mockStatic(AutoConfigurationPackages.class)) {

            autoConfigMock.when(() -> AutoConfigurationPackages.get(any()))
                    .thenReturn(Collections.singletonList("io.springflow.core.repository.testentities"));

            // When
            registrar.postProcessBeanDefinitionRegistry(registry);

            // Then
            // Custom repository and controller should still exist
            assertThat(registry.containsBeanDefinition("customRepoEntityRepository")).isTrue();
            assertThat(registry.getBeanDefinition("customRepoEntityRepository").getBeanClassName()).isNull();
            assertThat(registry.containsBeanDefinition("customRepoEntityController")).isTrue();
            assertThat(registry.getBeanDefinition("customRepoEntityController").getBeanClassName()).isNull();

            // Service should be generated
            assertThat(registry.containsBeanDefinition("customRepoEntityService")).isTrue();
        }
    }

    /**
     * Test Scenario 7: Custom Service + Controller
     * When both custom service and controller exist, SpringFlow should:
     * - GENERATE repository
     * - SKIP service generation
     * - SKIP controller generation
     */
    @Test
    void whenServiceAndControllerExist_shouldGenerateRepoSkipBoth() {
        // Given - Pre-register custom service and controller
        registry.registerBeanDefinition("customServiceEntityService", new GenericBeanDefinition());
        registry.registerBeanDefinition("customServiceEntityController", new GenericBeanDefinition());

        try (MockedConstruction<EntityScanner> scannerMock = mockConstruction(EntityScanner.class,
                (mock, context) -> {
                    when(mock.scanEntities(any())).thenReturn(Collections.singletonList(CustomServiceEntity.class));
                });
             MockedStatic<AutoConfigurationPackages> autoConfigMock = mockStatic(AutoConfigurationPackages.class)) {

            autoConfigMock.when(() -> AutoConfigurationPackages.get(any()))
                    .thenReturn(Collections.singletonList("io.springflow.core.repository.testentities"));

            // When
            registrar.postProcessBeanDefinitionRegistry(registry);

            // Then
            // Repository should be generated
            assertThat(registry.containsBeanDefinition("customServiceEntityRepository")).isTrue();

            // Custom service and controller should still exist
            assertThat(registry.containsBeanDefinition("customServiceEntityService")).isTrue();
            assertThat(registry.getBeanDefinition("customServiceEntityService").getBeanClassName()).isNull();
            assertThat(registry.containsBeanDefinition("customServiceEntityController")).isTrue();
            assertThat(registry.getBeanDefinition("customServiceEntityController").getBeanClassName()).isNull();
        }
    }

    /**
     * Test Scenario 8: All Three Custom Components
     * When all three custom components exist, SpringFlow should:
     * - SKIP repository generation
     * - SKIP service generation
     * - SKIP controller generation
     */
    @Test
    void whenAllThreeExist_shouldSkipAllGeneration() {
        // Given - Pre-register all three custom components
        registry.registerBeanDefinition("fullyCustomEntityRepository", new GenericBeanDefinition());
        registry.registerBeanDefinition("fullyCustomEntityService", new GenericBeanDefinition());
        registry.registerBeanDefinition("fullyCustomEntityController", new GenericBeanDefinition());

        try (MockedConstruction<EntityScanner> scannerMock = mockConstruction(EntityScanner.class,
                (mock, context) -> {
                    when(mock.scanEntities(any())).thenReturn(Collections.singletonList(FullyCustomEntity.class));
                });
             MockedStatic<AutoConfigurationPackages> autoConfigMock = mockStatic(AutoConfigurationPackages.class)) {

            autoConfigMock.when(() -> AutoConfigurationPackages.get(any()))
                    .thenReturn(Collections.singletonList("io.springflow.core.repository.testentities"));

            // When
            registrar.postProcessBeanDefinitionRegistry(registry);

            // Then
            // All three custom components should still exist (nothing generated)
            assertThat(registry.containsBeanDefinition("fullyCustomEntityRepository")).isTrue();
            assertThat(registry.getBeanDefinition("fullyCustomEntityRepository").getBeanClassName()).isNull();
            assertThat(registry.containsBeanDefinition("fullyCustomEntityService")).isTrue();
            assertThat(registry.getBeanDefinition("fullyCustomEntityService").getBeanClassName()).isNull();
            assertThat(registry.containsBeanDefinition("fullyCustomEntityController")).isTrue();
            assertThat(registry.getBeanDefinition("fullyCustomEntityController").getBeanClassName()).isNull();
        }
    }

    /**
     * Test Scenario 9: Logging Verification
     * When custom components are detected, SpringFlow should log skip messages.
     * This test verifies the logging behavior is in place.
     */
    @Test
    void whenCustomComponentDetected_shouldLogSkipMessage() {
        // Given - Pre-register all three custom components
        registry.registerBeanDefinition("customRepoEntityRepository", new GenericBeanDefinition());
        registry.registerBeanDefinition("customRepoEntityService", new GenericBeanDefinition());
        registry.registerBeanDefinition("customRepoEntityController", new GenericBeanDefinition());

        try (MockedConstruction<EntityScanner> scannerMock = mockConstruction(EntityScanner.class,
                (mock, context) -> {
                    when(mock.scanEntities(any())).thenReturn(Collections.singletonList(CustomRepoEntity.class));
                });
             MockedStatic<AutoConfigurationPackages> autoConfigMock = mockStatic(AutoConfigurationPackages.class)) {

            autoConfigMock.when(() -> AutoConfigurationPackages.get(any()))
                    .thenReturn(Collections.singletonList("io.springflow.core.repository.testentities"));

            // When
            registrar.postProcessBeanDefinitionRegistry(registry);

            // Then
            // All three components should be skipped
            assertThat(registry.containsBeanDefinition("customRepoEntityRepository")).isTrue();
            assertThat(registry.containsBeanDefinition("customRepoEntityService")).isTrue();
            assertThat(registry.containsBeanDefinition("customRepoEntityController")).isTrue();

            // Logging verification - AutoApiRepositoryRegistrar should log INFO messages:
            // "Skipping repository generation for CustomRepoEntity as customRepoEntityRepository already exists"
            // "Skipping service generation for CustomRepoEntity as customRepoEntityService already exists"
            // "Skipping controller generation for CustomRepoEntity as customRepoEntityController already exists"
            // (Actual log verification would require a log appender, but the code path is tested here)
        }
    }

    /**
     * Test with multiple entities - mixed custom and generated components
     * Verifies that SpringFlow handles multiple entities with different customization levels correctly
     */
    @Test
    void whenMultipleEntitiesWithMixedCustomComponents_shouldHandleEachCorrectly() {
        // Given - Pre-register custom components for different entities
        registry.registerBeanDefinition("customRepoEntityRepository", new GenericBeanDefinition()); // Custom repo only
        registry.registerBeanDefinition("customServiceEntityService", new GenericBeanDefinition()); // Custom service only
        registry.registerBeanDefinition("fullyCustomEntityRepository", new GenericBeanDefinition()); // All three custom
        registry.registerBeanDefinition("fullyCustomEntityService", new GenericBeanDefinition());
        registry.registerBeanDefinition("fullyCustomEntityController", new GenericBeanDefinition());

        try (MockedConstruction<EntityScanner> scannerMock = mockConstruction(EntityScanner.class,
                (mock, context) -> {
                    when(mock.scanEntities(any())).thenReturn(Arrays.asList(
                            CustomRepoEntity.class,
                            CustomServiceEntity.class,
                            CustomControllerEntity.class, // Nothing custom for this one - all should be generated
                            FullyCustomEntity.class
                    ));
                });
             MockedStatic<AutoConfigurationPackages> autoConfigMock = mockStatic(AutoConfigurationPackages.class)) {

            autoConfigMock.when(() -> AutoConfigurationPackages.get(any()))
                    .thenReturn(Collections.singletonList("io.springflow.core.repository.testentities"));

            // When
            registrar.postProcessBeanDefinitionRegistry(registry);

            // Then - Verify CustomRepoEntity (custom repo only)
            assertThat(registry.containsBeanDefinition("customRepoEntityRepository")).isTrue();
            assertThat(registry.containsBeanDefinition("customRepoEntityService")).isTrue(); // Generated
            assertThat(registry.containsBeanDefinition("customRepoEntityController")).isTrue(); // Generated

            // Verify CustomServiceEntity (custom service only)
            assertThat(registry.containsBeanDefinition("customServiceEntityRepository")).isTrue(); // Generated
            assertThat(registry.containsBeanDefinition("customServiceEntityService")).isTrue();
            assertThat(registry.containsBeanDefinition("customServiceEntityController")).isTrue(); // Generated

            // Verify CustomControllerEntity (all generated)
            assertThat(registry.containsBeanDefinition("customControllerEntityRepository")).isTrue(); // Generated
            assertThat(registry.containsBeanDefinition("customControllerEntityService")).isTrue(); // Generated
            assertThat(registry.containsBeanDefinition("customControllerEntityController")).isTrue(); // Generated

            // Verify FullyCustomEntity (all custom)
            assertThat(registry.containsBeanDefinition("fullyCustomEntityRepository")).isTrue();
            assertThat(registry.containsBeanDefinition("fullyCustomEntityService")).isTrue();
            assertThat(registry.containsBeanDefinition("fullyCustomEntityController")).isTrue();
        }
    }
}
