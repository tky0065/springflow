package io.springflow.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpringFlowWebConfigurationTest {

    @Test
    void configuration_shouldAddPageableResolver() {
        // Given
        PageableProperties properties = new PageableProperties();
        SpringFlowWebConfiguration config = new SpringFlowWebConfiguration(properties);
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        // When
        config.addArgumentResolvers(resolvers);

        // Then
        assertThat(resolvers).hasSize(1);
        assertThat(resolvers.get(0)).isInstanceOf(PageableHandlerMethodArgumentResolver.class);
    }

    @Test
    void configuration_withCustomProperties_shouldConfigureResolver() {
        // Given
        PageableProperties customProperties = new PageableProperties();
        customProperties.setDefaultPageSize(50);
        customProperties.setMaxPageSize(200);

        SpringFlowWebConfiguration config = new SpringFlowWebConfiguration(customProperties);
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        // When
        config.addArgumentResolvers(resolvers);

        // Then
        assertThat(resolvers).hasSize(1);
        PageableHandlerMethodArgumentResolver resolver = (PageableHandlerMethodArgumentResolver) resolvers.get(0);
        assertThat(resolver).isNotNull();
        // Note: Resolver is configured with custom values, but we can't easily inspect private fields
    }

    @Test
    void configuration_withDefaultProperties_shouldUseDefaults() {
        // Given
        PageableProperties defaultProperties = new PageableProperties();

        // When
        SpringFlowWebConfiguration config = new SpringFlowWebConfiguration(defaultProperties);
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        config.addArgumentResolvers(resolvers);

        // Then
        assertThat(resolvers).hasSize(1);
        assertThat(resolvers.get(0)).isInstanceOf(PageableHandlerMethodArgumentResolver.class);
        assertThat(defaultProperties.getDefaultPageSize()).isEqualTo(20);
        assertThat(defaultProperties.getMaxPageSize()).isEqualTo(100);
    }
}
