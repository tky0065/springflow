package io.springflow.core.security;

import io.springflow.annotations.*;
import io.springflow.core.controller.GenericCrudController;
import io.springflow.core.controller.support.SpringFlowControllerFactoryBean;
import io.springflow.core.mapper.DtoMapper;
import io.springflow.core.mapper.DtoMapperFactory;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.service.GenericCrudService;
import io.springflow.core.filter.FilterResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityIntegrationTest {

    @Mock
    private GenericCrudService<TestEntity, Long> service;

    @Mock
    private DtoMapperFactory dtoMapperFactory;

    @Mock
    private DtoMapper<TestEntity, Long> dtoMapper;

    @Mock
    private FilterResolver filterResolver;

    @Mock
    private AutoApi autoApi;

    @Mock
    private Security security;

    private SpringFlowControllerFactoryBean<TestEntity, Long> factoryBean;

    @BeforeEach
    void setUp() {
        factoryBean = new SpringFlowControllerFactoryBean<>();
        factoryBean.setEntityClass(TestEntity.class);
        factoryBean.setService(service);
        factoryBean.setDtoMapperFactory(dtoMapperFactory);
        factoryBean.setFilterResolver(filterResolver);
        
        when(dtoMapperFactory.getMapper(any(), any())).thenReturn((DtoMapper) dtoMapper);
    }

    @Test
    void getObject_whenSecurityEnabled_shouldGenerateControllerWithPreAuthorize() throws Exception {
        // Given
        when(autoApi.security()).thenReturn(security);
        when(security.enabled()).thenReturn(true);
        when(security.level()).thenReturn(SecurityLevel.ROLE_BASED);
        when(security.readLevel()).thenReturn(SecurityLevel.UNDEFINED);
        when(security.writeLevel()).thenReturn(SecurityLevel.UNDEFINED);
        when(security.roles()).thenReturn(new String[]{"ADMIN"});
        when(security.authorities()).thenReturn(new String[]{});

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", autoApi, Collections.emptyList()
        );
        factoryBean.setMetadata(metadata);

        // When
        GenericCrudController<TestEntity, Long> controller = factoryBean.getObject();

        // Then
        assertThat(controller).isNotNull();
        assertThat(controller.getClass().getName()).contains("AutoController");
        
        Method findAllMethod = controller.getClass().getMethod("findAll", 
                org.springframework.data.domain.Pageable.class, java.util.Map.class);
        
        PreAuthorize preAuthorize = findAllMethod.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAnyRole('ADMIN')");
    }

    @Test
    void getObject_whenSecurityPublic_shouldNotHavePreAuthorizeOrHavePermitAll() throws Exception {
        // Given
        when(autoApi.security()).thenReturn(security);
        when(security.enabled()).thenReturn(true);
        when(security.level()).thenReturn(SecurityLevel.PUBLIC);
        when(security.readLevel()).thenReturn(SecurityLevel.UNDEFINED);
        when(security.writeLevel()).thenReturn(SecurityLevel.UNDEFINED);

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", autoApi, Collections.emptyList()
        );
        factoryBean.setMetadata(metadata);

        // When
        GenericCrudController<TestEntity, Long> controller = factoryBean.getObject();

        // Then
        assertThat(controller).isNotNull();
        
        Method findAllMethod = controller.getClass().getMethod("findAll", 
                org.springframework.data.domain.Pageable.class, java.util.Map.class);
        
        PreAuthorize preAuthorize = findAllMethod.getAnnotation(PreAuthorize.class);
        if (preAuthorize != null) {
            assertThat(preAuthorize.value()).isEqualTo("permitAll()");
        }
    }

    @Test
    void getObject_whenAuthenticated_shouldHaveIsAuthenticatedExpression() throws Exception {
        // Given
        when(autoApi.security()).thenReturn(security);
        when(security.enabled()).thenReturn(true);
        when(security.level()).thenReturn(SecurityLevel.AUTHENTICATED);
        when(security.readLevel()).thenReturn(SecurityLevel.UNDEFINED);
        when(security.writeLevel()).thenReturn(SecurityLevel.UNDEFINED);

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", autoApi, Collections.emptyList()
        );
        factoryBean.setMetadata(metadata);

        // When
        GenericCrudController<TestEntity, Long> controller = factoryBean.getObject();

        // Then
        Method findAllMethod = controller.getClass().getMethod("findAll", 
                org.springframework.data.domain.Pageable.class, java.util.Map.class);
        
        PreAuthorize preAuthorize = findAllMethod.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("isAuthenticated()");
    }

    @Test
    void getObject_withGranularSecurity_shouldHaveDifferentExpressions() throws Exception {
        // Given
        when(autoApi.security()).thenReturn(security);
        when(security.enabled()).thenReturn(true);
        when(security.level()).thenReturn(SecurityLevel.PUBLIC);
        when(security.readLevel()).thenReturn(SecurityLevel.PUBLIC);
        when(security.writeLevel()).thenReturn(SecurityLevel.ROLE_BASED);
        when(security.roles()).thenReturn(new String[]{"ADMIN"});
        when(security.authorities()).thenReturn(new String[]{});

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", autoApi, Collections.emptyList()
        );
        factoryBean.setMetadata(metadata);

        // When
        GenericCrudController<TestEntity, Long> controller = factoryBean.getObject();

        // Then
        Method findAllMethod = controller.getClass().getMethod("findAll", 
                org.springframework.data.domain.Pageable.class, java.util.Map.class);
        PreAuthorize readPreAuth = findAllMethod.getAnnotation(PreAuthorize.class);
        assertThat(readPreAuth.value()).isEqualTo("permitAll()");

        Method createMethod = controller.getClass().getMethod("create", java.util.Map.class);
        PreAuthorize writePreAuth = createMethod.getAnnotation(PreAuthorize.class);
        assertThat(writePreAuth.value()).isEqualTo("hasAnyRole('ADMIN')");
    }

    static class TestEntity {
        private Long id;
    }
}
