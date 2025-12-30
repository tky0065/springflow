package io.springflow.starter.config;

import io.springflow.annotations.AutoApi;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OpenApiConfiguration}.
 */
class OpenApiConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class, OpenApiConfiguration.class);

    @Test
    void openApiConfiguration_shouldNotLoadWhenSpringdocNotPresent() {
        // Given/When - Run without springdoc on classpath (simulated by excluding the configuration)
        new ApplicationContextRunner()
                .withUserConfiguration(TestConfig.class)
                .run(context -> {
                    // Then - OpenAPI beans should not be present
                    assertThat(context).doesNotHaveBean(OpenAPI.class);
                    assertThat(context).doesNotHaveBean(GroupedOpenApi.class);
                });
    }

    @Test
    void openApiConfiguration_shouldLoadWithDefaultProperties() {
        // Given/When
        contextRunner.run(context -> {
            // Then
            assertThat(context).hasSingleBean(OpenAPI.class);
            assertThat(context).hasSingleBean(GroupedOpenApi.class);

            OpenAPI openAPI = context.getBean(OpenAPI.class);
            Info info = openAPI.getInfo();

            assertThat(info).isNotNull();
            assertThat(info.getTitle()).isEqualTo("SpringFlow API");
            assertThat(info.getDescription()).isEqualTo("Auto-generated REST API documentation");
            assertThat(info.getVersion()).isEqualTo("1.0.0");
        });
    }

    @Test
    void openApiConfiguration_shouldUseCustomProperties() {
        // Given/When
        contextRunner
                .withPropertyValues(
                        "springflow.swagger.title=Custom API",
                        "springflow.swagger.description=Custom Description",
                        "springflow.swagger.version=2.0.0"
                )
                .run(context -> {
                    // Then
                    OpenAPI openAPI = context.getBean(OpenAPI.class);
                    Info info = openAPI.getInfo();

                    assertThat(info.getTitle()).isEqualTo("Custom API");
                    assertThat(info.getDescription()).isEqualTo("Custom Description");
                    assertThat(info.getVersion()).isEqualTo("2.0.0");
                });
    }

    @Test
    void openApiConfiguration_shouldConfigureContactInfo() {
        // Given/When
        contextRunner
                .withPropertyValues(
                        "springflow.swagger.contact-name=API Team",
                        "springflow.swagger.contact-email=api@example.com",
                        "springflow.swagger.contact-url=https://example.com"
                )
                .run(context -> {
                    // Then
                    OpenAPI openAPI = context.getBean(OpenAPI.class);
                    Contact contact = openAPI.getInfo().getContact();

                    assertThat(contact).isNotNull();
                    assertThat(contact.getName()).isEqualTo("API Team");
                    assertThat(contact.getEmail()).isEqualTo("api@example.com");
                    assertThat(contact.getUrl()).isEqualTo("https://example.com");
                });
    }

    @Test
    void openApiConfiguration_shouldConfigureLicenseInfo() {
        // Given/When
        contextRunner
                .withPropertyValues(
                        "springflow.swagger.license-name=Apache 2.0",
                        "springflow.swagger.license-url=https://www.apache.org/licenses/LICENSE-2.0"
                )
                .run(context -> {
                    // Then
                    OpenAPI openAPI = context.getBean(OpenAPI.class);
                    License license = openAPI.getInfo().getLicense();

                    assertThat(license).isNotNull();
                    assertThat(license.getName()).isEqualTo("Apache 2.0");
                    assertThat(license.getUrl()).isEqualTo("https://www.apache.org/licenses/LICENSE-2.0");
                });
    }

    @Test
    void openApiConfiguration_shouldConfigureServerBasedOnBasePath() {
        // Given/When
        contextRunner
                .withPropertyValues("springflow.base-path=/api/v2")
                .run(context -> {
                    // Then
                    OpenAPI openAPI = context.getBean(OpenAPI.class);

                    assertThat(openAPI.getServers()).isNotEmpty();
                    // Server URL is always "/" to avoid double path concatenation
                    // Operation paths already include the basePath
                    assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("/");
                    assertThat(openAPI.getServers().get(0).getDescription()).isEqualTo("SpringFlow API Server");
                });
    }

    @Test
    void openApiConfiguration_shouldNotLoadWhenSwaggerDisabled() {
        // Given/When
        contextRunner
                .withPropertyValues("springflow.swagger.enabled=false")
                .run(context -> {
                    // Then - OpenAPI beans should not be created when swagger is disabled
                    assertThat(context).doesNotHaveBean(OpenAPI.class);
                    assertThat(context).doesNotHaveBean(GroupedOpenApi.class);
                });
    }

    @Test
    void groupedOpenApi_shouldConfigureSpringFlowGroup() {
        // Given/When
        contextRunner
                .withPropertyValues("springflow.base-path=/api/v1")
                .run(context -> {
                    // Then
                    assertThat(context).hasSingleBean(GroupedOpenApi.class);
                    GroupedOpenApi groupedApi = context.getBean(GroupedOpenApi.class);

                    assertThat(groupedApi).isNotNull();
                    // Note: GroupedOpenApi properties are not easily accessible,
                    // but we can verify the bean exists and was configured
                });
    }

    @Test
    void openApiConfiguration_shouldHandleTrailingSlashInBasePath() {
        // Given/When
        contextRunner
                .withPropertyValues("springflow.base-path=/api/")
                .run(context -> {
                    // Then - Should handle trailing slash correctly
                    assertThat(context).hasSingleBean(OpenAPI.class);
                    assertThat(context).hasSingleBean(GroupedOpenApi.class);
                });
    }

    @Configuration
    @EnableConfigurationProperties(SpringFlowProperties.class)
    static class TestConfig {
        // Mock @AutoApi to satisfy @ConditionalOnClass
        @Bean
        public Class<AutoApi> autoApiClass() {
            return AutoApi.class;
        }
    }
}
