package io.springflow.core.it;

import io.springflow.core.it.entity.IntegrationTestEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InfrastructureTest extends AbstractSpringFlowIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void shouldRegisterControllerForEntity() throws Exception {
        // The endpoint should be /api/integrationTestEntitys
        // We verify by trying to POST and then GET
        
        String json = "{\"name\": \"Test Entity\", \"description\": \"Test Description\"}";
        
        mockMvc.perform(post("/api/integrationTestEntitys")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Entity"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        mockMvc.perform(get("/api/integrationTestEntitys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Entity"));
    }
}
