package io.springflow.core.it;

import io.springflow.core.exception.EntityNotFoundException;
import io.springflow.core.it.entity.IntegrationTestEntity;
import io.springflow.core.service.GenericCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GenericCrudServiceIntegrationTest extends AbstractSpringFlowIntegrationTest {

    @Autowired
    @Qualifier("integrationTestEntityService")
    private GenericCrudService<IntegrationTestEntity, Long> service;

    @BeforeEach
    void setUp() {
        // Ensure clean state (repository is cleared by H2 drop/create usually, but explicit delete is safer if context is reused)
        service.findAll().forEach(e -> service.deleteById(e.getId()));
    }

    @Test
    void shouldPerformCompleteCrudLifecycle() {
        // 1. Create
        IntegrationTestEntity entity = new IntegrationTestEntity(null, "Service Test", "Description");
        IntegrationTestEntity saved = service.save(entity);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Service Test");

        // 2. Read (FindById)
        IntegrationTestEntity found = service.findById(saved.getId());
        assertThat(found).usingRecursiveComparison().isEqualTo(saved);

        // 3. Read (FindAll)
        List<IntegrationTestEntity> all = service.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getName()).isEqualTo("Service Test");

        // 4. Read (Pageable)
        Page<IntegrationTestEntity> page = service.findAll(PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Service Test");

        // 5. Update
        found.setName("Updated Service Test");
        IntegrationTestEntity updated = service.update(found.getId(), found);
        assertThat(updated.getName()).isEqualTo("Updated Service Test");
        
        IntegrationTestEntity foundAfterUpdate = service.findById(updated.getId());
        assertThat(foundAfterUpdate.getName()).isEqualTo("Updated Service Test");

        // 6. Delete
        service.deleteById(saved.getId());
        
        assertThatThrownBy(() -> service.findById(saved.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentEntity() {
        IntegrationTestEntity entity = new IntegrationTestEntity(999L, "Non Existent", "Desc");
        
        assertThatThrownBy(() -> service.update(999L, entity))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentEntity() {
        assertThatThrownBy(() -> service.deleteById(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
