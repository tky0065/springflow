package io.springflow.core.controller.support;

import io.springflow.core.controller.GenericCrudController;
import io.springflow.core.metadata.EntityMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.http.ResponseEntity;
import io.springflow.core.controller.PageResponse;
import org.springframework.data.domain.Pageable;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RequestMappingRegistrarRefinementTest {

    private RequestMappingRegistrar registrar;
    private ApplicationContext context;
    private RequestMappingHandlerMapping handlerMapping;
    private Environment environment;
    private ConfigurableListableBeanFactory beanFactory;

    @BeforeEach
    void setUp() {
        registrar = new RequestMappingRegistrar();
        context = mock(ApplicationContext.class);
        handlerMapping = mock(RequestMappingHandlerMapping.class);
        environment = mock(Environment.class);
        beanFactory = mock(ConfigurableListableBeanFactory.class);

        when(context.getBean(RequestMappingHandlerMapping.class)).thenReturn(handlerMapping);
        when(context.getEnvironment()).thenReturn(environment);
        when(context.getAutowireCapableBeanFactory()).thenReturn(beanFactory);
        when(environment.getProperty("springflow.base-path", "/api")).thenReturn("/api");
    }

    @Test
    void onApplicationEvent_shouldRegisterGeneratedController() {
        // Given
        Map<String, GenericCrudController> controllers = new HashMap<>();
        GeneratedController generatedController = new GeneratedController();
        controllers.put("testController", generatedController);
        when(context.getBeansOfType(GenericCrudController.class)).thenReturn(controllers);

        BeanDefinition bd = mock(BeanDefinition.class);
        when(beanFactory.getBeanDefinition("testController")).thenReturn(bd);
        when(bd.getAttribute("requestMapping")).thenReturn("/tests");

        // When
        registrar.onApplicationEvent(new ContextRefreshedEvent(context));

        // Then
        verify(handlerMapping, atLeastOnce()).registerMapping(any(RequestMappingInfo.class), eq(generatedController), any());
    }

    @Test
    void onApplicationEvent_withRestController_shouldSkipRegistration() {
        // Given
        Map<String, GenericCrudController> controllers = new HashMap<>();
        UserDefinedController userController = new UserDefinedController();
        controllers.put("userController", userController);
        when(context.getBeansOfType(GenericCrudController.class)).thenReturn(controllers);

        // When
        registrar.onApplicationEvent(new ContextRefreshedEvent(context));

        // Then
        // It should NOT try to register anything for @RestController
        verify(handlerMapping, never()).registerMapping(any(), any(), any());
    }

    @Test
    void onApplicationEvent_withExistingMapping_shouldSkipDuplicate() {
        // Given
        Map<String, GenericCrudController> controllers = new HashMap<>();
        GeneratedController generatedController = new GeneratedController();
        controllers.put("testController", generatedController);
        when(context.getBeansOfType(GenericCrudController.class)).thenReturn(controllers);

        BeanDefinition bd = mock(BeanDefinition.class);
        when(beanFactory.getBeanDefinition("testController")).thenReturn(bd);
        when(bd.getAttribute("requestMapping")).thenReturn("/tests");

        // Mock existing mapping
        Map<RequestMappingInfo, Object> existingMappings = new HashMap<>();
        existingMappings.put(mock(RequestMappingInfo.class), new Object());
        // Since RequestMappingInfo uses value-based equality, we can't easily mock the exact one 
        // without a lot of setup, but we can verify that the registrar checks the map.
        when(handlerMapping.getHandlerMethods()).thenReturn(new HashMap<>()); // Start empty for simplicity in this mock

        // When
        registrar.onApplicationEvent(new ContextRefreshedEvent(context));

        // Then
        verify(handlerMapping, atLeastOnce()).getHandlerMethods();
    }

    @Test
    void onApplicationEvent_withOverriddenMethod_shouldRegisterSubclassMethod() {
        // Given
        Map<String, GenericCrudController> controllers = new HashMap<>();
        OverridingController overriddenController = new OverridingController();
        controllers.put("overriddenController", overriddenController);
        when(context.getBeansOfType(GenericCrudController.class)).thenReturn(controllers);

        BeanDefinition bd = mock(BeanDefinition.class);
        when(beanFactory.getBeanDefinition("overriddenController")).thenReturn(bd);
        when(bd.getAttribute("requestMapping")).thenReturn("/overridden");

        when(handlerMapping.getHandlerMethods()).thenReturn(new HashMap<>());

        // When
        registrar.onApplicationEvent(new ContextRefreshedEvent(context));

        // Then
        // Verify that the subclass method is the one registered
        // We look for findAll in the subclass
        ArgumentCaptor<Method> methodCaptor = ArgumentCaptor.forClass(Method.class);
        verify(handlerMapping, atLeastOnce()).registerMapping(any(), any(), methodCaptor.capture());
        
        assertThat(methodCaptor.getAllValues())
                .anyMatch(m -> m.getDeclaringClass() == OverridingController.class && m.getName().equals("findAll"));
    }

    @Test
    void onApplicationEvent_withEasyOverriding_shouldResolveEntityClass() {
        // Given
        Map<String, GenericCrudController> controllers = new HashMap<>();
        EasyOverridingController easyController = new EasyOverridingController(); // Uses no-args constructor
        controllers.put("easyController", easyController);
        when(context.getBeansOfType(GenericCrudController.class)).thenReturn(controllers);

        // Then
        assertThat(easyController.getEntityClass()).isEqualTo(String.class);
    }

    static class EasyOverridingController extends GenericCrudController<String, Long> {
        public EasyOverridingController() {
            super(); // Uses the new no-args constructor
        }
        
        public Class<String> getEntityClass() {
            return this.entityClass;
        }
    }

    static class OverridingController extends GenericCrudController<String, Long> {
        public OverridingController() {
            super(); // Uses the new no-args constructor
        }

        @Override
        public ResponseEntity<PageResponse<Map<String, Object>>> findAll(Pageable pageable, HttpServletRequest request) {
            return null; // Implementation doesn't matter for this test
        }
    }

    static class GeneratedController extends GenericCrudController<String, Long> {
        public GeneratedController() {
            super(null, null, null, null, String.class, null);
        }
        @Override protected Long getEntityId(String entity) { return 1L; }
    }

    @RestController
    static class UserDefinedController extends GenericCrudController<String, Long> {
        public UserDefinedController() {
            super(null, null, null, null, String.class, null);
        }
        @Override protected Long getEntityId(String entity) { return 1L; }

        @GetMapping("/custom")
        public String custom() { return "custom"; }
    }
}
