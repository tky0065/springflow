package io.springflow.core.service;

import io.springflow.core.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenericCrudServiceTest {

    @Mock
    private JpaRepository<TestEntity, Long> repository;

    private GenericCrudService<TestEntity, Long> service;

    @BeforeEach
    void setUp() {
        service = new GenericCrudService<>(repository, TestEntity.class) {
            // Concrete implementation for testing
        };
    }

    @Test
    void findAll_withPageable_shouldReturnPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<TestEntity> entities = Arrays.asList(new TestEntity(1L), new TestEntity(2L));
        Page<TestEntity> page = new PageImpl<>(entities, pageable, entities.size());
        when(repository.findAll(pageable)).thenReturn(page);

        // When
        Page<TestEntity> result = service.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(repository).findAll(pageable);
    }

    @Test
    void findAll_shouldReturnList() {
        // Given
        List<TestEntity> entities = Arrays.asList(new TestEntity(1L), new TestEntity(2L));
        when(repository.findAll()).thenReturn(entities);

        // When
        List<TestEntity> result = service.findAll();

        // Then
        assertThat(result).hasSize(2);
        verify(repository).findAll();
    }

    @Test
    void findById_whenExists_shouldReturnEntity() {
        // Given
        TestEntity entity = new TestEntity(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        // When
        TestEntity result = service.findById(1L);

        // Then
        assertThat(result).isEqualTo(entity);
        verify(repository).findById(1L);
    }

    @Test
    void findById_whenNotExists_shouldThrowException() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("TestEntity")
                .hasMessageContaining("1");
        verify(repository).findById(1L);
    }

    @Test
    void existsById_shouldReturnTrue() {
        // Given
        when(repository.existsById(1L)).thenReturn(true);

        // When
        boolean result = service.existsById(1L);

        // Then
        assertThat(result).isTrue();
        verify(repository).existsById(1L);
    }

    @Test
    void save_shouldPersistEntity() {
        // Given
        TestEntity entity = new TestEntity(null);
        TestEntity saved = new TestEntity(1L);
        when(repository.save(entity)).thenReturn(saved);

        // When
        TestEntity result = service.save(entity);

        // Then
        assertThat(result).isEqualTo(saved);
        verify(repository).save(entity);
    }

    @Test
    void update_whenExists_shouldUpdateEntity() {
        // Given
        TestEntity existing = new TestEntity(1L);
        TestEntity updated = new TestEntity(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(updated)).thenReturn(updated);

        // When
        TestEntity result = service.update(1L, updated);

        // Then
        assertThat(result).isEqualTo(updated);
        verify(repository).findById(1L);
        verify(repository).save(updated);
    }

    @Test
    void update_whenNotExists_shouldThrowException() {
        // Given
        TestEntity updated = new TestEntity(1L);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.update(1L, updated))
                .isInstanceOf(EntityNotFoundException.class);
        verify(repository).findById(1L);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteById_whenExists_shouldDelete() {
        // Given
        TestEntity entity = new TestEntity(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        doNothing().when(repository).deleteById(1L);

        // When
        service.deleteById(1L);

        // Then
        verify(repository).findById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void deleteById_whenNotExists_shouldThrowException() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.deleteById(1L))
                .isInstanceOf(EntityNotFoundException.class);
        verify(repository).findById(1L);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void count_shouldReturnCount() {
        // Given
        when(repository.count()).thenReturn(5L);

        // When
        long result = service.count();

        // Then
        assertThat(result).isEqualTo(5L);
        verify(repository).count();
    }

    @Test
    void save_shouldCallHooks() {
        // Given
        TestEntity entity = new TestEntity(null);
        TestEntity saved = new TestEntity(1L);
        when(repository.save(entity)).thenReturn(saved);

        // Use array to capture hook calls (workaround for final variable requirement)
        final boolean[] hooksCalled = {false, false}; // [beforeCreate, afterCreate]
        final GenericCrudService<TestEntity, Long> serviceWithHooks = new GenericCrudService<>(repository, TestEntity.class) {
            @Override
            protected void beforeCreate(TestEntity e) {
                hooksCalled[0] = true;
            }

            @Override
            protected void afterCreate(TestEntity e) {
                hooksCalled[1] = true;
            }
        };

        // When
        serviceWithHooks.save(entity);

        // Then
        assertThat(hooksCalled[0]).as("beforeCreate hook should be called").isTrue();
        assertThat(hooksCalled[1]).as("afterCreate hook should be called").isTrue();
        verify(repository).save(entity);
    }

    // Test entity class
    static class TestEntity {
        private Long id;

        public TestEntity(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
