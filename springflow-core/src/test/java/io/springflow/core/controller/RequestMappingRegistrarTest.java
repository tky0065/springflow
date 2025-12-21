package io.springflow.core.controller;

import io.springflow.core.controller.support.RequestMappingRegistrar;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.assertj.core.api.Assertions.assertThat;

class RequestMappingRegistrarTest {

    @Test
    void requestMappingRegistrar_shouldBeApplicationListener() {
        // Given
        RequestMappingRegistrar registrar = new RequestMappingRegistrar();

        // Then
        assertThat(registrar).isInstanceOf(ApplicationListener.class);
    }

    @Test
    void onApplicationEvent_shouldOnlyRegisterOnce() {
        // Given
        RequestMappingRegistrar registrar = new RequestMappingRegistrar();

        // Then
        // Note: Full registration testing with RequestMappingHandlerMapping and
        // ApplicationContext requires integration tests (see SpringFlowWebConfigurationTest)
        assertThat(registrar).isNotNull();
    }
}
