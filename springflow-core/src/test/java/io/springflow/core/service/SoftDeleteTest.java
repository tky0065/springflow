package io.springflow.core.service;

import io.springflow.annotations.SoftDelete;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SoftDeleteTest {

    @Mock
    private JpaRepository<TestEntity, Long> repository;

    @Mock
    private SoftDelete softDelete;

    private GenericCrudService<TestEntity, Long> service;
    private EntityMetadata metadata;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        // Mock repository with Specification support
        repository = mock(JpaRepository.class, withSettings().extraInterfaces(JpaSpecificationExecutor.class));

        lenient().when(softDelete.deletedField()).thenReturn("deleted");
        lenient().when(softDelete.deletedAtField()).thenReturn("deletedAt");

        FieldMetadata deletedField = new FieldMetadata(
                TestEntity.class.getDeclaredField("deleted"),
                "deleted", boolean.class, false, false, false, false, false, null,
                Collections.emptyList(), null, null, false
        );
        FieldMetadata deletedAtField = new FieldMetadata(
                TestEntity.class.getDeclaredField("deletedAt"),
                "deletedAt", java.time.LocalDateTime.class, true, false, false, false, false, null,
                Collections.emptyList(), null, null, false
        );

        metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", null, softDelete,
                Arrays.asList(deletedField, deletedAtField)
        );

        service = new GenericCrudService<TestEntity, Long>(repository, TestEntity.class, metadata) {
        };
    }

    @Test
    void deleteById_shouldPerformSoftDelete() {
        // Given
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setDeleted(false);
        when(((JpaSpecificationExecutor<TestEntity>) repository).findOne(any())).thenReturn(Optional.of(entity));

        // When
        service.deleteById(1L);

        // Then
        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getDeletedAt()).isNotNull();
        verify(repository).save(entity);
        verify(repository, never()).deleteById(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_shouldApplySoftDeleteFilter() {
        // When
        service.findAll();

        // Then
        verify((JpaSpecificationExecutor<TestEntity>) repository).findAll(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findDeletedOnly_shouldApplyDeletedOnlyFilter() {
        // When
        service.findDeletedOnly(org.springframework.data.domain.Pageable.unpaged());

        // Then
        verify((JpaSpecificationExecutor<TestEntity>) repository).findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void hardDeleteById_shouldPerformPhysicalDelete() {
        // Given
        when(repository.existsById(1L)).thenReturn(true);

        // When
        service.hardDeleteById(1L);

        // Then
        verify(repository).deleteById(1L);
    }

    @Test
    void restoreById_shouldResetDeletedFlag() {
        // Given
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setDeleted(true);
        entity.setDeletedAt(java.time.LocalDateTime.now());
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        // When
        TestEntity restored = service.restoreById(1L);

        // Then
        assertThat(restored.isDeleted()).isFalse();
        assertThat(restored.getDeletedAt()).isNull();
        verify(repository).save(entity);
    }

    static class TestEntity {
        private Long id;
        private boolean deleted;
        private java.time.LocalDateTime deletedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public boolean isDeleted() { return deleted; }
        public void setDeleted(boolean deleted) { this.deleted = deleted; }
        public java.time.LocalDateTime getDeletedAt() { return deletedAt; }
        public void setDeletedAt(java.time.LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    }
}
