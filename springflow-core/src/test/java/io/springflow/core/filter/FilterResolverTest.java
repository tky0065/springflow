package io.springflow.core.filter;

import io.springflow.annotations.FilterType;
import io.springflow.annotations.Filterable;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilterResolverTest {

    private FilterResolver filterResolver;

    @Mock
    private Root<TestEntity> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> path;

    @Mock
    private Path<String> stringPath;

    @Mock
    private Filterable filterable;

    @BeforeEach
    void setUp() {
        filterResolver = new FilterResolver();
    }

    @Test
    void buildSpecification_withEqualsFilter_shouldCreatePredicate() throws Exception {
        // Given
        when(filterable.types()).thenReturn(new FilterType[]{FilterType.EQUALS});
        when(filterable.caseSensitive()).thenReturn(true);
        
        FieldMetadata fieldMetadata = new FieldMetadata(
                TestEntity.class.getDeclaredField("name"),
                "name",
                String.class,
                true, false, false, false, null,
                Collections.emptyList(),
                filterable,
                null,
                false
        );

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", null,
                Collections.singletonList(fieldMetadata)
        );

        Map<String, String> params = new HashMap<>();
        params.put("name", "John");

        // When
        Specification<TestEntity> spec = filterResolver.buildSpecification(params, metadata);
        when(root.get("name")).thenReturn(path);
        spec.toPredicate(root, query, cb);

        // Then
        verify(cb).equal(path, "John");
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildSpecification_withLikeFilter_shouldCreatePredicate() throws Exception {
        // Given
        when(filterable.types()).thenReturn(new FilterType[]{FilterType.LIKE});
        when(filterable.caseSensitive()).thenReturn(true);

        FieldMetadata fieldMetadata = new FieldMetadata(
                TestEntity.class.getDeclaredField("name"),
                "name",
                String.class,
                true, false, false, false, null,
                Collections.emptyList(),
                filterable,
                null,
                false
        );

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", null,
                Collections.singletonList(fieldMetadata)
        );

        Map<String, String> params = new HashMap<>();
        params.put("name_like", "John");

        // When
        Specification<TestEntity> spec = filterResolver.buildSpecification(params, metadata);
        when(root.get("name")).thenReturn((Path) stringPath);
        spec.toPredicate(root, query, cb);

        // Then
        verify(cb).like(stringPath, "%John%");
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildSpecification_withGtFilter_shouldCreatePredicate() throws Exception {
        // Given
        when(filterable.types()).thenReturn(new FilterType[]{FilterType.GREATER_THAN});

        FieldMetadata fieldMetadata = new FieldMetadata(
                TestEntity.class.getDeclaredField("age"),
                "age",
                Integer.class,
                true, false, false, false, null,
                Collections.emptyList(),
                filterable,
                null,
                false
        );

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", null,
                Collections.singletonList(fieldMetadata)
        );

        Map<String, String> params = new HashMap<>();
        params.put("age_gt", "18");

        // When
        Specification<TestEntity> spec = filterResolver.buildSpecification(params, metadata);
        when(root.get("age")).thenReturn((Path) path);
        spec.toPredicate(root, query, cb);

        // Then
        verify(cb).greaterThan(any(Expression.class), any(Comparable.class));
    }

    @Test
    void buildSpecification_withIsNullFilter_shouldCreatePredicate() throws Exception {
        // Given
        when(filterable.types()).thenReturn(new FilterType[]{FilterType.IS_NULL});

        FieldMetadata fieldMetadata = new FieldMetadata(
                TestEntity.class.getDeclaredField("name"),
                "name",
                String.class,
                true, false, false, false, null,
                Collections.emptyList(),
                filterable,
                null,
                false
        );

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", null,
                Collections.singletonList(fieldMetadata)
        );

        Map<String, String> params = new HashMap<>();
        params.put("name_null", "true");

        // When
        Specification<TestEntity> spec = filterResolver.buildSpecification(params, metadata);
        when(root.get("name")).thenReturn(path);
        spec.toPredicate(root, query, cb);

        // Then
        verify(cb).isNull(path);
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildSpecification_withInFilter_shouldCreatePredicate() throws Exception {
        // Given
        when(filterable.types()).thenReturn(new FilterType[]{FilterType.IN});

        FieldMetadata fieldMetadata = new FieldMetadata(
                TestEntity.class.getDeclaredField("name"),
                "name", String.class, true, false, false, false, null,
                Collections.emptyList(), filterable, null, false
        );

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", null,
                Collections.singletonList(fieldMetadata)
        );

        Map<String, String> params = new HashMap<>();
        params.put("name_in", "John,Jane");

        // When
        Specification<TestEntity> spec = filterResolver.buildSpecification(params, metadata);
        CriteriaBuilder.In<Object> inMock = mock(CriteriaBuilder.In.class);
        lenient().when(inMock.value(any())).thenReturn(inMock);
        when(root.get("name")).thenReturn((Path) path);
        when(cb.in(any())).thenReturn(inMock);
        spec.toPredicate(root, query, cb);

        // Then
        verify(cb).in(path);
        verify(inMock).value("John");
        verify(inMock).value("Jane");
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildSpecification_withBetweenFilter_shouldCreatePredicate() throws Exception {
        // Given
        when(filterable.types()).thenReturn(new FilterType[]{FilterType.BETWEEN});

        FieldMetadata fieldMetadata = new FieldMetadata(
                TestEntity.class.getDeclaredField("age"),
                "age", Integer.class, true, false, false, false, null,
                Collections.emptyList(), filterable, null, false
        );

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", null,
                Collections.singletonList(fieldMetadata)
        );

        Map<String, String> params = new HashMap<>();
        params.put("age_between", "18,65");

        // When
        Specification<TestEntity> spec = filterResolver.buildSpecification(params, metadata);
        when(root.get("age")).thenReturn((Path) path);
        spec.toPredicate(root, query, cb);

        // Then
        verify(cb).between(any(Expression.class), any(Comparable.class), any(Comparable.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildSpecification_withRangeFilter_shouldCreateGteAndLtePredicates() throws Exception {
        // Given
        when(filterable.types()).thenReturn(new FilterType[]{FilterType.RANGE});

        FieldMetadata fieldMetadata = new FieldMetadata(
                TestEntity.class.getDeclaredField("age"),
                "age", Integer.class, true, false, false, false, null,
                Collections.emptyList(), filterable, null, false
        );

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", null,
                Collections.singletonList(fieldMetadata)
        );

        Map<String, String> params = new HashMap<>();
        params.put("age_gte", "18");
        params.put("age_lte", "65");

        // When
        Specification<TestEntity> spec = filterResolver.buildSpecification(params, metadata);
        when(root.get("age")).thenReturn((Path) path);
        spec.toPredicate(root, query, cb);

        // Then
        verify(cb).greaterThanOrEqualTo(any(Expression.class), any(Comparable.class));
        verify(cb).lessThanOrEqualTo(any(Expression.class), any(Comparable.class));
    }

    @Test
    void buildSpecification_withMultipleFilters_shouldCombineWithAnd() throws Exception {
        // Given
        Filterable nameFilterable = mock(Filterable.class);
        when(nameFilterable.types()).thenReturn(new FilterType[]{FilterType.EQUALS});
        
        Filterable ageFilterable = mock(Filterable.class);
        when(ageFilterable.types()).thenReturn(new FilterType[]{FilterType.GREATER_THAN});

        FieldMetadata nameField = new FieldMetadata(
                TestEntity.class.getDeclaredField("name"),
                "name", String.class, true, false, false, false, null,
                Collections.emptyList(), nameFilterable, null, false
        );
        FieldMetadata ageField = new FieldMetadata(
                TestEntity.class.getDeclaredField("age"),
                "age", Integer.class, true, false, false, false, null,
                Collections.emptyList(), ageFilterable, null, false
        );

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", null,
                java.util.Arrays.asList(nameField, ageField)
        );

        Map<String, String> params = new HashMap<>();
        params.put("name", "John");
        params.put("age_gt", "18");

        // When
        Specification<TestEntity> spec = filterResolver.buildSpecification(params, metadata);
        when(root.get("name")).thenReturn(path);
        when(root.get("age")).thenReturn(path);
        spec.toPredicate(root, query, cb);

        // Then
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    void buildSpecification_withFetchFields_shouldAddFetchJoins() throws Exception {
        // Given
        FieldMetadata relationField = new FieldMetadata(
                TestEntity.class.getDeclaredField("category"),
                "category", Object.class, true, false, false, false, null,
                Collections.emptyList(), null, mock(io.springflow.core.metadata.RelationMetadata.class), false
        );

        EntityMetadata metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", null,
                Collections.singletonList(relationField)
        );

        Map<String, String> params = new HashMap<>();
        java.util.List<String> fetchFields = java.util.Collections.singletonList("category");

        // When
        Specification<TestEntity> spec = filterResolver.buildSpecification(params, metadata, fetchFields);
        doReturn(TestEntity.class).when(query).getResultType();
        spec.toPredicate(root, query, cb);

        // Then
        verify(root).fetch(eq("category"), any(jakarta.persistence.criteria.JoinType.class));
        verify(query).distinct(true);
    }

    static class TestEntity {
        private String name;
        private Integer age;
        private Object category;
    }
}
