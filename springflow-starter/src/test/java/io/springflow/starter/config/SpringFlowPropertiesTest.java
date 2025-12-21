package io.springflow.starter.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SpringFlowProperties} configuration properties.
 */
class SpringFlowPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void properties_withDefaults_shouldHaveCorrectValues() {
        // Given/When
        contextRunner.run(context -> {
            // Then
            assertThat(context).hasSingleBean(SpringFlowProperties.class);
            SpringFlowProperties properties = context.getBean(SpringFlowProperties.class);

            assertThat(properties.isEnabled()).isTrue();
            assertThat(properties.getBasePath()).isEqualTo("/api");
            assertThat(properties.getBasePackages()).isEmpty();

            assertThat(properties.getPagination()).isNotNull();
            assertThat(properties.getPagination().getDefaultPageSize()).isEqualTo(20);
            assertThat(properties.getPagination().getMaxPageSize()).isEqualTo(100);
            assertThat(properties.getPagination().getPageParameter()).isEqualTo("page");
            assertThat(properties.getPagination().getSizeParameter()).isEqualTo("size");
            assertThat(properties.getPagination().getSortParameter()).isEqualTo("sort");
            assertThat(properties.getPagination().isOneIndexedParameters()).isFalse();

            assertThat(properties.getSwagger()).isNotNull();
            assertThat(properties.getSwagger().isEnabled()).isTrue();
            assertThat(properties.getSwagger().getTitle()).isEqualTo("SpringFlow API");
            assertThat(properties.getSwagger().getDescription()).isEqualTo("Auto-generated REST API documentation");
            assertThat(properties.getSwagger().getVersion()).isEqualTo("1.0.0");
        });
    }

    @Test
    void properties_withCustomValues_shouldOverrideDefaults() {
        // Given/When
        contextRunner
                .withPropertyValues(
                        "springflow.enabled=false",
                        "springflow.base-path=/api/v2",
                        "springflow.base-packages=com.example.domain,com.example.model",
                        "springflow.pagination.default-page-size=50",
                        "springflow.pagination.max-page-size=200",
                        "springflow.swagger.title=Custom API",
                        "springflow.swagger.version=2.0.0"
                )
                .run(context -> {
                    // Then
                    SpringFlowProperties properties = context.getBean(SpringFlowProperties.class);

                    assertThat(properties.isEnabled()).isFalse();
                    assertThat(properties.getBasePath()).isEqualTo("/api/v2");
                    assertThat(properties.getBasePackages())
                            .containsExactly("com.example.domain", "com.example.model");

                    assertThat(properties.getPagination().getDefaultPageSize()).isEqualTo(50);
                    assertThat(properties.getPagination().getMaxPageSize()).isEqualTo(200);

                    assertThat(properties.getSwagger().getTitle()).isEqualTo("Custom API");
                    assertThat(properties.getSwagger().getVersion()).isEqualTo("2.0.0");
                });
    }

    @Test
    void properties_paginationCustomization_shouldWork() {
        // Given/When
        contextRunner
                .withPropertyValues(
                        "springflow.pagination.page-parameter=p",
                        "springflow.pagination.size-parameter=s",
                        "springflow.pagination.sort-parameter=orderBy",
                        "springflow.pagination.one-indexed-parameters=true"
                )
                .run(context -> {
                    // Then
                    SpringFlowProperties properties = context.getBean(SpringFlowProperties.class);
                    SpringFlowProperties.Pagination pagination = properties.getPagination();

                    assertThat(pagination.getPageParameter()).isEqualTo("p");
                    assertThat(pagination.getSizeParameter()).isEqualTo("s");
                    assertThat(pagination.getSortParameter()).isEqualTo("orderBy");
                    assertThat(pagination.isOneIndexedParameters()).isTrue();
                });
    }

    @Test
    void properties_swaggerCustomization_shouldWork() {
        // Given/When
        contextRunner
                .withPropertyValues(
                        "springflow.swagger.enabled=false",
                        "springflow.swagger.contact-name=Support Team",
                        "springflow.swagger.contact-email=support@example.com",
                        "springflow.swagger.license-name=MIT",
                        "springflow.swagger.license-url=https://opensource.org/licenses/MIT"
                )
                .run(context -> {
                    // Then
                    SpringFlowProperties properties = context.getBean(SpringFlowProperties.class);
                    SpringFlowProperties.Swagger swagger = properties.getSwagger();

                    assertThat(swagger.isEnabled()).isFalse();
                    assertThat(swagger.getContactName()).isEqualTo("Support Team");
                    assertThat(swagger.getContactEmail()).isEqualTo("support@example.com");
                    assertThat(swagger.getLicenseName()).isEqualTo("MIT");
                    assertThat(swagger.getLicenseUrl()).isEqualTo("https://opensource.org/licenses/MIT");
                });
    }

    @EnableConfigurationProperties(SpringFlowProperties.class)
    static class TestConfiguration {
    }
}
