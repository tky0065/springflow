package io.springflow.demo;

import io.springflow.demo.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuditingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createProduct_shouldPopulateAuditFields() {
        // Given
        Map<String, Object> productDto = new HashMap<>();
        productDto.put("name", "New Audited Product");
        productDto.put("price", 99.99);
        productDto.put("stock", 10);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/products", productDto, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<String, Object> created = response.getBody();
        assertThat(created).isNotNull();
        assertThat(created.get("name")).isEqualTo("New Audited Product");
        
        // Verify audit fields
        assertThat(created.get("createdAt")).isNotNull();
        assertThat(created.get("updatedAt")).isNotNull();
        assertThat(created.get("createdBy")).isEqualTo("system"); // Default fallback
        assertThat(created.get("updatedBy")).isEqualTo("system");
    }
}
