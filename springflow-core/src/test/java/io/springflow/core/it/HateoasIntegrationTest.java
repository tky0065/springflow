package io.springflow.core.it;

import io.springflow.core.it.entity.IntegrationTestEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HateoasIntegrationTest extends AbstractSpringFlowIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    private JpaRepository<IntegrationTestEntity, Long> repository;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        String repositoryBeanName = "integrationTestEntityRepository";
        repository = (JpaRepository<IntegrationTestEntity, Long>) applicationContext.getBean(repositoryBeanName);
        repository.deleteAll();
    }

    @Test
    void shouldReturnLinksInPagedResponse() throws Exception {
        // Given: 25 entities to ensure multiple pages (default size is 20)
        for (int i = 0; i < 25; i++) {
            repository.save(new IntegrationTestEntity(null, "Entity " + i, "Desc " + i));
        }

        // When: Requesting the first page
        mockMvc.perform(get("/api/integrationTestEntitys")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then: Check for _links
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.self.href", containsString("/api/integrationTestEntitys")))
                .andExpect(jsonPath("$._links.first.href", containsString("/api/integrationTestEntitys")))
                .andExpect(jsonPath("$._links.next.href", containsString("/api/integrationTestEntitys")))
                .andExpect(jsonPath("$._links.last.href", containsString("/api/integrationTestEntitys")));
    }
}
