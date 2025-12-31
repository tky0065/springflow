package io.springflow.core.controller;

import io.springflow.annotations.FilterType;
import io.springflow.annotations.Filterable;
import io.springflow.core.filter.FilterResolver;
import io.springflow.core.mapper.DtoMapper;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import io.springflow.core.service.GenericCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EntityFilteringIntegrationTest {

    private JpaRepository<TestEntity, Long> repository;
    private GenericCrudService<TestEntity, Long> service;
    private DtoMapper<TestEntity, Long> dtoMapper;
    private FilterResolver filterResolver;
    private EntityMetadata metadata;
    private GenericCrudController<TestEntity, Long> controller;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        repository = mock(JpaRepository.class, withSettings().extraInterfaces(JpaSpecificationExecutor.class));
        filterResolver = new FilterResolver(); // Use real resolver
        
        // Setup Field Metadata with @Filterable
        Filterable nameFilterable = mock(Filterable.class);
        when(nameFilterable.types()).thenReturn(new FilterType[]{FilterType.EQUALS, FilterType.LIKE});
        when(nameFilterable.caseSensitive()).thenReturn(false);

        FieldMetadata nameField = new FieldMetadata(
                TestEntity.class.getDeclaredField("name"),
                "name", String.class, true, false, false, false, false, null,
                Collections.emptyList(), nameFilterable, null, false
        );

        metadata = mock(EntityMetadata.class);
        when(metadata.fields()).thenReturn(Collections.singletonList(nameField));
        when(metadata.getFieldByName("name")).thenReturn(Optional.of(nameField));

        service = new GenericCrudService<TestEntity, Long>(repository, TestEntity.class, metadata) {};
        dtoMapper = mock(DtoMapper.class);

        when(dtoMapper.toOutputDtoPage(any(Page.class), any())).thenAnswer(inv -> {
            Page<TestEntity> p = inv.getArgument(0);
            return p.map(e -> Map.of("id", e.id, "name", e.name));
        });

        controller = new GenericCrudController<>(service, dtoMapper, filterResolver, metadata, TestEntity.class, null) {
            @Override
            protected Long getEntityId(TestEntity entity) {
                return entity.id;
            }
        };
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_withBracketedFilter_shouldApplySpecification() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("name[like]", "John");
        Pageable pageable = PageRequest.of(0, 20);
        
        List<TestEntity> entities = List.of(new TestEntity(1L, "John Doe"));
        Page<TestEntity> page = new PageImpl<>(entities, pageable, 1);
        
        when(((JpaSpecificationExecutor<TestEntity>) repository).findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);

        // When
        ResponseEntity<PageResponse<Map<String, Object>>> response = controller.findAll(pageable, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify((JpaSpecificationExecutor<TestEntity>) repository).findAll(any(Specification.class), eq(pageable));
    }

    static class TestEntity {
        Long id;
        String name;
        TestEntity(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
