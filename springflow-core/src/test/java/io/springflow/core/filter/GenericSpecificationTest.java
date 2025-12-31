package io.springflow.core.filter;

import io.springflow.annotations.FilterType;
import io.springflow.core.metadata.testentities.ValidatedEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenericSpecificationTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreatePredicateForEquals() {
        SearchCriteria criteria = new SearchCriteria("name", FilterType.EQUALS, "John");
        GenericSpecification<ValidatedEntity> spec = new GenericSpecification<>(criteria);

        Root<ValidatedEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        Path<Object> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("name")).thenReturn(path);
        when(builder.equal(path, "John")).thenReturn(predicate);

        Predicate result = spec.toPredicate(root, query, builder);

        assertNotNull(result);
        assertEquals(predicate, result);
        verify(builder).equal(path, "John");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreatePredicateForLike() {
        SearchCriteria criteria = new SearchCriteria("name", FilterType.LIKE, "John");
        GenericSpecification<ValidatedEntity> spec = new GenericSpecification<>(criteria);

        Root<ValidatedEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        Path<String> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<String>get("name")).thenReturn(path);
        when(builder.like(path, "%John%")).thenReturn(predicate);

        Predicate result = spec.toPredicate(root, query, builder);

        assertNotNull(result);
        assertEquals(predicate, result);
        verify(builder).like(path, "%John%");
    }
}
