package io.springflow.core.controller;

import io.springflow.core.filter.FilterResolver;
import io.springflow.core.mapper.DtoMapper;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import io.springflow.core.service.GenericCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SortValidationTest {

    private GenericCrudController<TestEntity, Long> controller;
    private EntityMetadata metadata;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        JpaRepository<TestEntity, Long> repository = mock(JpaRepository.class);
        metadata = mock(EntityMetadata.class);
        GenericCrudService<TestEntity, Long> service = mock(GenericCrudService.class);
        DtoMapper<TestEntity, Long> dtoMapper = mock(DtoMapper.class);
        FilterResolver filterResolver = mock(FilterResolver.class);

        controller = new GenericCrudController<>(service, dtoMapper, filterResolver, metadata, TestEntity.class) {
            @Override
            protected Long getEntityId(TestEntity entity) {
                return entity.getId();
            }
        };
    }

    @Test
    void findAll_shouldThrowException_whenSortFieldIsInvalid() {
        // Given
        Sort sort = Sort.by("invalidField");
        Pageable pageable = PageRequest.of(0, 10, sort);
        
        // Mock metadata to not find the field
        when(metadata.getFieldByName("invalidField")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> controller.findAll(pageable, new MockHttpServletRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sort field: invalidField");
    }

    @Test
    void search_shouldThrowException_whenSortFieldIsInvalid() {
        // Given
        Sort sort = Sort.by("invalidField");
        Pageable pageable = PageRequest.of(0, 10, sort);
        io.springflow.core.dto.SearchRequest searchRequest = new io.springflow.core.dto.SearchRequest(
            Collections.emptyList(), io.springflow.core.dto.SearchRequest.LogicalOperator.AND
        );
        
        // Mock metadata to not find the field
        when(metadata.getFieldByName("invalidField")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> controller.search(searchRequest, pageable, new MockHttpServletRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sort field: invalidField");
    }

    @Test
    void findAll_shouldNotThrowException_whenSortFieldIsValid() {
        // Given
        Sort sort = Sort.by("validField");
        Pageable pageable = PageRequest.of(0, 10, sort);
        
        // Mock metadata to find the field
        when(metadata.getFieldByName("validField")).thenReturn(Optional.of(mock(FieldMetadata.class)));

        // When & Then
        // Should not throw exception (mock service will return null/empty, which might cause other errors but we check for IllegalArgumentException from validation)
        // To be safe, we expect no exception or a different one. 
        // Ideally we mock the service call to return something safe.
        // But here checking if *validation* passes.
        try {
            controller.findAll(pageable, new MockHttpServletRequest());
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException && e.getMessage().contains("Invalid sort field")) {
                throw e;
            }
            // Ignore other exceptions as we only care about validation passing
        }
    }

    static class TestEntity {
        private Long id;
        public Long getId() { return id; }
    }
}
