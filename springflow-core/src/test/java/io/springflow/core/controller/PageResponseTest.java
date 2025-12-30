package io.springflow.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PageResponse DTO.
 */
class PageResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void constructor_withNonEmptyPage_shouldInitializeCorrectly() {
        // Given
        List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
        Pageable pageable = PageRequest.of(0, 20);
        Page<String> page = new PageImpl<>(items, pageable, 100);

        // When
        PageResponse<String> response = new PageResponse<>(page);

        // Then
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getContent()).containsExactly("Item 1", "Item 2", "Item 3");
        assertThat(response.getPage()).isNotNull();
        assertThat(response.getPage().getSize()).isEqualTo(20);
        assertThat(response.getPage().getNumber()).isEqualTo(0);
        assertThat(response.getPage().getTotalElements()).isEqualTo(100);
        assertThat(response.getPage().getTotalPages()).isEqualTo(5);
        assertThat(response.getPage().isFirst()).isTrue();
        assertThat(response.getPage().isLast()).isFalse();
        assertThat(response.getPage().isEmpty()).isFalse();
    }

    @Test
    void constructor_withEmptyPage_shouldInitializeCorrectly() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<String> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

        // When
        PageResponse<String> response = new PageResponse<>(page);

        // Then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getPage()).isNotNull();
        assertThat(response.getPage().getSize()).isEqualTo(20);
        assertThat(response.getPage().getNumber()).isEqualTo(0);
        assertThat(response.getPage().getTotalElements()).isEqualTo(0);
        assertThat(response.getPage().getTotalPages()).isEqualTo(0);
        assertThat(response.getPage().isFirst()).isTrue();
        assertThat(response.getPage().isLast()).isTrue();
        assertThat(response.getPage().isEmpty()).isTrue();
    }

    @Test
    void constructor_withLastPage_shouldSetLastFlag() {
        // Given
        List<String> items = Arrays.asList("Item 1", "Item 2");
        Pageable pageable = PageRequest.of(4, 20);
        Page<String> page = new PageImpl<>(items, pageable, 82); // Last page (5 pages total)

        // When
        PageResponse<String> response = new PageResponse<>(page);

        // Then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getPage().getNumber()).isEqualTo(4);
        assertThat(response.getPage().getTotalPages()).isEqualTo(5);
        assertThat(response.getPage().isFirst()).isFalse();
        assertThat(response.getPage().isLast()).isTrue();
    }

    @Test
    void constructor_withMiddlePage_shouldSetFlagsCorrectly() {
        // Given
        List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
        Pageable pageable = PageRequest.of(2, 20);
        Page<String> page = new PageImpl<>(items, pageable, 100);

        // When
        PageResponse<String> response = new PageResponse<>(page);

        // Then
        assertThat(response.getPage().getNumber()).isEqualTo(2);
        assertThat(response.getPage().isFirst()).isFalse();
        assertThat(response.getPage().isLast()).isFalse();
    }

    @Test
    void constructor_withSinglePageResult_shouldSetBothFirstAndLast() {
        // Given
        List<String> items = Arrays.asList("Item 1", "Item 2");
        Pageable pageable = PageRequest.of(0, 20);
        Page<String> page = new PageImpl<>(items, pageable, 2); // Only 1 page

        // When
        PageResponse<String> response = new PageResponse<>(page);

        // Then
        assertThat(response.getPage().getTotalPages()).isEqualTo(1);
        assertThat(response.getPage().isFirst()).isTrue();
        assertThat(response.getPage().isLast()).isTrue();
    }

    @Test
    void serialization_shouldProduceCorrectJsonStructure() throws JsonProcessingException {
        // Given
        Map<String, Object> item1 = new HashMap<>();
        item1.put("id", 1);
        item1.put("name", "Product 1");

        Map<String, Object> item2 = new HashMap<>();
        item2.put("id", 2);
        item2.put("name", "Product 2");

        List<Map<String, Object>> items = Arrays.asList(item1, item2);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Map<String, Object>> page = new PageImpl<>(items, pageable, 100);
        PageResponse<Map<String, Object>> response = new PageResponse<>(page);

        // When
        String json = objectMapper.writeValueAsString(response);

        // Then
        assertThat(json).contains("\"content\"");
        assertThat(json).contains("\"page\"");
        assertThat(json).contains("\"size\":20");
        assertThat(json).contains("\"number\":0");
        assertThat(json).contains("\"totalElements\":100");
        assertThat(json).contains("\"totalPages\":5");
        assertThat(json).contains("\"first\":true");
        assertThat(json).contains("\"last\":false");
        assertThat(json).contains("\"empty\":false");
        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Product 1\"");
    }

    @Test
    void pageMetadata_shouldContainAllExpectedFields() {
        // Given
        List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
        Pageable pageable = PageRequest.of(1, 10);
        Page<String> page = new PageImpl<>(items, pageable, 25);

        // When
        PageResponse<String> response = new PageResponse<>(page);
        PageResponse.PageMetadata metadata = response.getPage();

        // Then
        assertThat(metadata.getSize()).isEqualTo(10);
        assertThat(metadata.getNumber()).isEqualTo(1);
        assertThat(metadata.getTotalElements()).isEqualTo(25);
        assertThat(metadata.getTotalPages()).isEqualTo(3);
        assertThat(metadata.isFirst()).isFalse();
        assertThat(metadata.isLast()).isFalse();
        assertThat(metadata.isEmpty()).isFalse();
    }

    @Test
    void constructor_withDifferentContentTypes_shouldWork() {
        // Given - Test with Integer content
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        Pageable pageable = PageRequest.of(0, 5);
        Page<Integer> page = new PageImpl<>(numbers, pageable, 20);

        // When
        PageResponse<Integer> response = new PageResponse<>(page);

        // Then
        assertThat(response.getContent()).hasSize(5);
        assertThat(response.getContent()).containsExactly(1, 2, 3, 4, 5);
        assertThat(response.getPage().getTotalElements()).isEqualTo(20);
    }
}
