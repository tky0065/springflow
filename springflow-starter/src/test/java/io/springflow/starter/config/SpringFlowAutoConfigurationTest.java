package io.springflow.starter.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SpringFlowAutoConfiguration}.
 * <p>
 * Note: Full auto-configuration activation testing requires a complex Spring Boot context
 * with JPA EntityManager and related beans. These tests focus on verifying that properties
 * are correctly loaded and configured. Integration testing of the full auto-configuration
 * is done in the springflow-demo module.
 * </p>
 */
class SpringFlowAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void properties_shouldBeLoadedCorrectly() {
        // Given/When
        contextRunner.run(context -> {
            // Then
            assertThat(context).hasSingleBean(SpringFlowProperties.class);
            SpringFlowProperties properties = context.getBean(SpringFlowProperties.class);

            assertThat(properties.isEnabled()).isTrue();
            assertThat(properties.getBasePath()).isEqualTo("/api");
        });
    }

    @Test
    void autoConfiguration_withDisabledProperty_shouldStillLoadProperties() {
        // Given/When
        contextRunner
                .withPropertyValues("springflow.enabled=false")
                .run(context -> {
                    // Then - Properties are still loaded even if enabled=false
                    assertThat(context).hasSingleBean(SpringFlowProperties.class);
                    SpringFlowProperties properties = context.getBean(SpringFlowProperties.class);
                    assertThat(properties.isEnabled()).isFalse();
                });
    }

    @Test
    void properties_withCustomValues_shouldOverrideDefaults() {
        // Given/When
        contextRunner
                .withPropertyValues(
                        "springflow.base-path=/api/v2",
                        "springflow.pagination.default-page-size=50",
                        "springflow.pagination.max-page-size=200"
                )
                .run(context -> {
                    // Then
                    SpringFlowProperties properties = context.getBean(SpringFlowProperties.class);
                    assertThat(properties.getBasePath()).isEqualTo("/api/v2");
                    assertThat(properties.getPagination().getDefaultPageSize()).isEqualTo(50);
                    assertThat(properties.getPagination().getMaxPageSize()).isEqualTo(200);
                });
    }

    @Test
    void properties_withBasePackages_shouldConfigureCorrectly() {
        // Given/When
        contextRunner
                .withPropertyValues(
                        "springflow.base-packages[0]=com.example.domain",
                        "springflow.base-packages[1]=com.example.model"
                )
                .run(context -> {
                    // Then
                    SpringFlowProperties properties = context.getBean(SpringFlowProperties.class);
                    assertThat(properties.getBasePackages())
                            .containsExactly("com.example.domain", "com.example.model");
                });
    }

    @Test
    void properties_swaggerConfiguration_shouldWorkCorrectly() {
        // Given/When
        contextRunner
                .withPropertyValues(
                        "springflow.swagger.enabled=false",
                        "springflow.swagger.title=Custom API"
                )
                .run(context -> {
                    // Then
                    SpringFlowProperties properties = context.getBean(SpringFlowProperties.class);
                    assertThat(properties.getSwagger().isEnabled()).isFalse();
                    assertThat(properties.getSwagger().getTitle()).isEqualTo("Custom API");
                });
    }

    @EnableConfigurationProperties(SpringFlowProperties.class)
    static class TestConfiguration {
    }
}
