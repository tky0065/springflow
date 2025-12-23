package io.springflow.core.controller;

import io.springflow.core.mapper.DtoMapper;
import io.springflow.core.service.GenericCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for pagination and sorting functionality in GenericCrudController.
 */
class PaginationAndSortingTest {

    private JpaRepository<TestEntity, Long> repository;
    private TestEntityService service;
    private DtoMapper<TestEntity, Long> dtoMapper;
    private GenericCrudController<TestEntity, Long> controller;

    @BeforeEach
    void setUp() {
        repository = mock(JpaRepository.class);
        service = new TestEntityService(repository);
        dtoMapper = mock(DtoMapper.class);

        // Setup DtoMapper mocks
        when(dtoMapper.toOutputDto(any(TestEntity.class))).thenAnswer(inv -> {
            TestEntity e = inv.getArgument(0);
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("name", e.getName());
            return map;
        });

        when(dtoMapper.toOutputDtoPage(any(Page.class))).thenAnswer(inv -> {
            Page<TestEntity> p = inv.getArgument(0);
            return p.map(e -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", e.getId());
                map.put("name", e.getName());
                return map;
            });
        });

        controller = new GenericCrudController<>(service, dtoMapper, TestEntity.class) {
            @Override
            protected Long getEntityId(TestEntity entity) {
                return entity.getId();
            }
        };
    }

    @Test
    void pagination_withDefaultPageSize_shouldReturnFirstPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        List<TestEntity> entities = createEntities(20);
        Page<TestEntity> page = new PageImpl<>(entities, pageable, 100);
        when(repository.findAll(pageable)).thenReturn(page);

        // When
        ResponseEntity<Page<Map<String, Object>>> response = controller.findAll(pageable);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(20);
        assertThat(response.getBody().getTotalElements()).isEqualTo(100);
        assertThat(response.getBody().getTotalPages()).isEqualTo(5);
        assertThat(response.getBody().getNumber()).isEqualTo(0);
        assertThat(response.getBody().getSize()).isEqualTo(20);
        assertThat(response.getBody().isFirst()).isTrue();
        assertThat(response.getBody().isLast()).isFalse();
    }

    @Test
    void pagination_withCustomPageSize_shouldReturnCorrectPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<TestEntity> entities = createEntities(10);
        Page<TestEntity> page = new PageImpl<>(entities, pageable, 100);
        when(repository.findAll(pageable)).thenReturn(page);

        // When
        ResponseEntity<Page<Map<String, Object>>> response = controller.findAll(pageable);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(10);
        assertThat(response.getBody().getTotalPages()).isEqualTo(10);
    }

    @Test
    void pagination_secondPage_shouldReturnCorrectPage() {
        // Given
        Pageable pageable = PageRequest.of(1, 20);
        List<TestEntity> entities = createEntities(20);
        Page<TestEntity> page = new PageImpl<>(entities, pageable, 100);
        when(repository.findAll(pageable)).thenReturn(page);

        // When
        ResponseEntity<Page<Map<String, Object>>> response = controller.findAll(pageable);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNumber()).isEqualTo(1);
        assertThat(response.getBody().isFirst()).isFalse();
        assertThat(response.getBody().isLast()).isFalse();
    }

    @Test
    void pagination_lastPage_shouldReturnCorrectMetadata() {
        // Given
        Pageable pageable = PageRequest.of(4, 20);
        List<TestEntity> entities = createEntities(20);
        Page<TestEntity> page = new PageImpl<>(entities, pageable, 100);
        when(repository.findAll(pageable)).thenReturn(page);

        // When
        ResponseEntity<Page<Map<String, Object>>> response = controller.findAll(pageable);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNumber()).isEqualTo(4);
        assertThat(response.getBody().isFirst()).isFalse();
        assertThat(response.getBody().isLast()).isTrue();
    }

    @Test
    void sorting_singleField_shouldReturnSortedResults() {
        // Given
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Pageable pageable = PageRequest.of(0, 20, sort);
        List<TestEntity> entities = Arrays.asList(
                new TestEntity(1L, "Alpha"),
                new TestEntity(2L, "Beta"),
                new TestEntity(3L, "Gamma")
        );
        Page<TestEntity> page = new PageImpl<>(entities, pageable, 3);
        when(repository.findAll(pageable)).thenReturn(page);

        // When
        ResponseEntity<Page<Map<String, Object>>> response = controller.findAll(pageable);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().get(0).get("name")).isEqualTo("Alpha");
        assertThat(response.getBody().getContent().get(1).get("name")).isEqualTo("Beta");
        assertThat(response.getBody().getContent().get(2).get("name")).isEqualTo("Gamma");
    }

    @Test
    void sorting_descending_shouldReturnReversedResults() {
        // Given
        Sort sort = Sort.by(Sort.Direction.DESC, "name");
        Pageable pageable = PageRequest.of(0, 20, sort);
        List<TestEntity> entities = Arrays.asList(
                new TestEntity(3L, "Gamma"),
                new TestEntity(2L, "Beta"),
                new TestEntity(1L, "Alpha")
        );
        Page<TestEntity> page = new PageImpl<>(entities, pageable, 3);
        when(repository.findAll(pageable)).thenReturn(page);

        // When
        ResponseEntity<Page<Map<String, Object>>> response = controller.findAll(pageable);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().get(0).get("name")).isEqualTo("Gamma");
        assertThat(response.getBody().getContent().get(1).get("name")).isEqualTo("Beta");
        assertThat(response.getBody().getContent().get(2).get("name")).isEqualTo("Alpha");
    }

    @Test
    void sorting_multipleFields_shouldSortByBothFields() {
        // Given
        Sort sort = Sort.by(
                Sort.Order.asc("category"),
                Sort.Order.desc("name")
        );
        Pageable pageable = PageRequest.of(0, 20, sort);
        List<TestEntity> entities = createEntities(5);
        Page<TestEntity> page = new PageImpl<>(entities, pageable, 5);
        when(repository.findAll(pageable)).thenReturn(page);

        // When
        ResponseEntity<Page<Map<String, Object>>> response = controller.findAll(pageable);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(5);
        assertThat(response.getBody().getSort().isSorted()).isTrue();
    }

    @Test
    void pagination_emptyResults_shouldReturnEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<TestEntity> page = Page.empty(pageable);
        when(repository.findAll(pageable)).thenReturn(page);

        // When
        ResponseEntity<Page<Map<String, Object>>> response = controller.findAll(pageable);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
        assertThat(response.getBody().getTotalPages()).isEqualTo(0);
    }

    @Test
    void pagination_withSorting_shouldCombineBoth() {
        // Given
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(1, 10, sort);
        List<TestEntity> entities = createEntities(10);
        Page<TestEntity> page = new PageImpl<>(entities, pageable, 50);
        when(repository.findAll(pageable)).thenReturn(page);

        // When
        ResponseEntity<Page<Map<String, Object>>> response = controller.findAll(pageable);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNumber()).isEqualTo(1);
        assertThat(response.getBody().getSize()).isEqualTo(10);
        assertThat(response.getBody().getSort().isSorted()).isTrue();
    }

    // Helper methods
    private List<TestEntity> createEntities(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new TestEntity((long) i, "Entity " + i))
                .toList();
    }

    // Test classes
    static class TestEntity {
        private Long id;
        private String name;
        private String category;

        public TestEntity(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }
    }

    static class TestEntityService extends GenericCrudService<TestEntity, Long> {
        public TestEntityService(JpaRepository<TestEntity, Long> repository) {
            super(repository, TestEntity.class);
        }
    }
}