package io.springflow.core.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.springflow.core.it.entity.IntegrationTestEntity;
import io.springflow.core.repository.RepositoryGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GenericCrudControllerIntegrationTest extends AbstractSpringFlowIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationContext applicationContext;

    private JpaRepository<IntegrationTestEntity, Long> repository;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // Resolve repository from context
        String repositoryBeanName = "integrationTestEntityRepository";
        repository = (JpaRepository<IntegrationTestEntity, Long>) applicationContext.getBean(repositoryBeanName);
        repository.deleteAll();
    }

    @Test
    void shouldListEntities() throws Exception {
        repository.save(new IntegrationTestEntity(null, "Test 1", "Desc 1"));
        repository.save(new IntegrationTestEntity(null, "Test 2", "Desc 2"));

        mockMvc.perform(get("/api/integrationTestEntitys")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("Test 1")))
                .andExpect(jsonPath("$.content[1].name", is("Test 2")));
    }

    @Test
    void shouldCreateEntity() throws Exception {
        IntegrationTestEntity entity = new IntegrationTestEntity(null, "New Entity", "New Desc");

        mockMvc.perform(post("/api/integrationTestEntitys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("New Entity")));
    }

    @Test
    void shouldGetEntityById() throws Exception {
        IntegrationTestEntity saved = repository.save(new IntegrationTestEntity(null, "Get Me", "Desc"));

        mockMvc.perform(get("/api/integrationTestEntitys/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Get Me")));
    }

    @Test
    void shouldReturn404ForNonExistentId() throws Exception {
        mockMvc.perform(get("/api/integrationTestEntitys/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateEntity() throws Exception {
        IntegrationTestEntity saved = repository.save(new IntegrationTestEntity(null, "Update Me", "Desc"));
        saved.setName("Updated");

        mockMvc.perform(put("/api/integrationTestEntitys/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated")));
    }

    @Test
    void shouldPatchEntity() throws Exception {
        IntegrationTestEntity saved = repository.save(new IntegrationTestEntity(null, "Patch Me", "Original Desc"));
        // Create partial update
        IntegrationTestEntity patch = new IntegrationTestEntity();
        patch.setName("Patched Name");
        // Description is null, so it should not be updated

        mockMvc.perform(patch("/api/integrationTestEntitys/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Patched Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Patched Name")))
                .andExpect(jsonPath("$.description", is("Original Desc")));
    }

    @Test
    void shouldDeleteEntity() throws Exception {
        IntegrationTestEntity saved = repository.save(new IntegrationTestEntity(null, "Delete Me", "Desc"));

        mockMvc.perform(delete("/api/integrationTestEntitys/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/integrationTestEntitys/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }
}
