package io.springflow.core.security;

import io.springflow.annotations.SecuredApi;
import io.springflow.annotations.SecurityLevel;
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
class SecuredApiIntegrationTest {

    @Mock
    private GenericCrudService<TestEntity, Long> service;

    @Mock
    private DtoMapperFactory dtoMapperFactory;

    @Mock
    private DtoMapper<TestEntity, Long> dtoMapper;

    @Mock
    private FilterResolver filterResolver;

    @Mock
    private SecuredApi securedApi;

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
    void getObject_withSecuredApi_shouldHaveCorrectExpressions() throws Exception {
        // Given
        when(securedApi.level()).thenReturn(SecurityLevel.ROLE_BASED);
        when(securedApi.roles()).thenReturn(new String[]{"MANAGER"});
        when(securedApi.authorities()).thenReturn(new String[]{});
        when(securedApi.readLevel()).thenReturn(SecurityLevel.UNDEFINED);
        when(securedApi.writeLevel()).thenReturn(SecurityLevel.UNDEFINED);

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", null, null, null, securedApi, Collections.emptyList()
        );
        factoryBean.setMetadata(metadata);

        // When
        GenericCrudController<TestEntity, Long> controller = factoryBean.getObject();

        // Then
        Method findAllMethod = controller.getClass().getMethod("findAll", 
                org.springframework.data.domain.Pageable.class, jakarta.servlet.http.HttpServletRequest.class);
        PreAuthorize preAuthorize = findAllMethod.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize.value()).isEqualTo("hasAnyRole('MANAGER')");
    }

    @Test
    void getObject_withSecuredApiGranular_shouldHaveDifferentExpressions() throws Exception {
        // Given
        when(securedApi.level()).thenReturn(SecurityLevel.AUTHENTICATED);
        when(securedApi.readLevel()).thenReturn(SecurityLevel.PUBLIC);
        when(securedApi.writeLevel()).thenReturn(SecurityLevel.ROLE_BASED);
        when(securedApi.roles()).thenReturn(new String[]{"ADMIN"});
        when(securedApi.authorities()).thenReturn(new String[]{});

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", null, null, null, securedApi, Collections.emptyList()
        );
        factoryBean.setMetadata(metadata);

        // When
        GenericCrudController<TestEntity, Long> controller = factoryBean.getObject();

        // Then
        Method findAllMethod = controller.getClass().getMethod("findAll", 
                org.springframework.data.domain.Pageable.class, jakarta.servlet.http.HttpServletRequest.class);
        assertThat(findAllMethod.getAnnotation(PreAuthorize.class).value()).isEqualTo("permitAll()");

        Method deleteMethod = controller.getClass().getMethod("delete", Object.class);
        assertThat(deleteMethod.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAnyRole('ADMIN')");
    }

    static class TestEntity {
        private Long id;
    }
}
