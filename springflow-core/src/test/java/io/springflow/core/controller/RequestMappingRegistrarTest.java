package io.springflow.core.controller;

import io.springflow.core.controller.support.RequestMappingRegistrar;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestMappingRegistrarTest {

    @Test
    void postProcessAfterInitialization_withNonController_shouldReturnBean() {
        // Given
        RequestMappingRegistrar registrar = new RequestMappingRegistrar();
        String bean = "Not a controller";

        // When
        Object result = registrar.postProcessAfterInitialization(bean, "someBean");

        // Then
        assertThat(result).isSameAs(bean);
    }

    @Test
    void postProcessAfterInitialization_withController_shouldReturnBean() {
        // Given
        RequestMappingRegistrar registrar = new RequestMappingRegistrar();
        Object bean = new Object(); // Any object

        // When
        Object result = registrar.postProcessAfterInitialization(bean, "testBean");

        // Then
        assertThat(result).isSameAs(bean);
        // Note: Full registration testing with RequestMappingHandlerMapping requires integration tests
    }
}
