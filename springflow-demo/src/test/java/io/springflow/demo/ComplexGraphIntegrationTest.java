package io.springflow.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.springflow.demo.entity.Category;
import io.springflow.demo.entity.Product;
import io.springflow.demo.entity.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ComplexGraphIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCircularAndManyToManyRelationships() throws Exception {
        // 1. Create Parent Category
        String parentJson = "{\"name\": \"Electronics Test\", \"description\": \"All electronics test\"}";
        MvcResult parentResult = mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(parentJson))
                .andExpect(status().isCreated())
                .andReturn();
        Category parent = objectMapper.readValue(parentResult.getResponse().getContentAsString(), Category.class);

        // 2. Create Child Category linked to Parent
        // We assume the API accepts "parent": ID
        String childJson = String.format("{\"name\": \"Laptops Test\", \"parent\": %d}", parent.getId());
        MvcResult childResult = mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(childJson))
                .andExpect(status().isCreated())
                .andReturn();
        Category child = objectMapper.readValue(childResult.getResponse().getContentAsString(), Category.class);

        // 3. Create Tag
        String tagJson = "{\"name\": \"Portable\"}";
        MvcResult tagResult = mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tagJson))
                .andExpect(status().isCreated())
                .andReturn();
        Tag tag = objectMapper.readValue(tagResult.getResponse().getContentAsString(), Tag.class);

                // 4. Create Product linked to Child Category and Tag

                // We use IDs directly because DtoMapper resolves non-Map values as References (IDs)

                String productJson = String.format("{" +

                        "\"name\": \"MacBook Pro Test\", " +

                        "\"price\": 2000.0, " +

                        "\"stock\": 10, " + 

                        "\"category\": %d, " +

                        "\"tags\": [%d]" +

                        "}", child.getId(), tag.getId());

                

                MvcResult productResult = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isCreated())
                .andReturn();
        
        // 5. Verify Product Response (should contain category and tags)
        mockMvc.perform(get("/api/products/{id}", objectMapper.readTree(productResult.getResponse().getContentAsString()).get("id").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("MacBook Pro Test")))
                .andExpect(jsonPath("$.category.name", is("Laptops Test")))
                .andExpect(jsonPath("$.tags[0].name", is("Portable")));

        // 6. Verify Category Response (should contain parent)
        // Checks if serialization handles recursion (Parent -> Child -> Parent...)
        mockMvc.perform(get("/api/categories/{id}", child.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Laptops Test")))
                .andExpect(jsonPath("$.parent.name", is("Electronics Test")));
        
        // 7. Verify Tag Response
        mockMvc.perform(get("/api/tags/{id}", tag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Portable")));
    }
}