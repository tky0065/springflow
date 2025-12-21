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
        SpringFlowWebConfiguration config = new SpringFlowWebConfiguration();
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        // When
        config.addArgumentResolvers(resolvers);

        // Then
        assertThat(resolvers).hasSize(1);
        assertThat(resolvers.get(0)).isInstanceOf(PageableHandlerMethodArgumentResolver.class);
    }

    @Test
    void configuration_withCustomProperties_shouldUseCustomValues() {
        // Given
        PageableProperties customProperties = new PageableProperties();
        customProperties.setDefaultPageSize(50);
        customProperties.setMaxPageSize(200);

        SpringFlowWebConfiguration config = new SpringFlowWebConfiguration(customProperties);

        // When
        PageableProperties bean = config.pageableProperties();

        // Then
        assertThat(bean.getDefaultPageSize()).isEqualTo(50);
        assertThat(bean.getMaxPageSize()).isEqualTo(200);
    }

    @Test
    void pageablePropertiesBean_shouldBeCreated() {
        // Given
        SpringFlowWebConfiguration config = new SpringFlowWebConfiguration();

        // When
        PageableProperties properties = config.pageableProperties();

        // Then
        assertThat(properties).isNotNull();
        assertThat(properties.getDefaultPageSize()).isEqualTo(20);
        assertThat(properties.getMaxPageSize()).isEqualTo(100);
    }
}
