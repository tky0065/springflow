package io.springflow.core.it;

import io.springflow.core.it.entity.IntegrationTestEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RepositoryGeneratorIntegrationTest extends AbstractSpringFlowIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @SuppressWarnings("unchecked")
    void shouldPerformCrudOperationsOnGeneratedRepository() {
        // Given
        String repositoryBeanName = "integrationTestEntityRepository";
        assertThat(applicationContext.containsBean(repositoryBeanName)).isTrue();
        
        JpaRepository<IntegrationTestEntity, Long> repository = 
                (JpaRepository<IntegrationTestEntity, Long>) applicationContext.getBean(repositoryBeanName);
        
        IntegrationTestEntity entity = new IntegrationTestEntity(null, "CRUD Test", "Testing CRUD");

        // When - Create
        IntegrationTestEntity saved = repository.save(entity);
        assertThat(saved.getId()).isNotNull();

        // When - Read
        Optional<IntegrationTestEntity> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("CRUD Test");

        // When - Update
        found.get().setName("Updated Name");
        repository.save(found.get());
        
        IntegrationTestEntity updated = repository.findById(saved.getId()).get();
        assertThat(updated.getName()).isEqualTo("Updated Name");

        // When - Delete
        repository.deleteById(saved.getId());
        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldRegisterRepositoriesForMultipleEntities() {
        assertThat(applicationContext.containsBean("integrationTestEntityRepository")).isTrue();
        assertThat(applicationContext.containsBean("relatedEntityRepository")).isTrue();
    }
}
