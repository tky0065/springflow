package io.springflow.core.controller;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HateoasTest {

    @Test
    void shouldGenerateLinksForPagedResponse() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        request.setQueryString("page=1&size=10");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Page<String> page = new PageImpl<>(List.of("item"), PageRequest.of(1, 10), 50);

        // When
        PageResponse<String> response = new PageResponse<>(page);

        // Then
        assertThat(response.getLinks()).isNotNull();
        assertThat(response.getLinks()).containsKey("self");
        assertThat(response.getLinks()).containsKey("first");
        assertThat(response.getLinks()).containsKey("last");
        assertThat(response.getLinks()).containsKey("next");
        assertThat(response.getLinks()).containsKey("prev");
        
        assertThat(response.getLinks().get("self").getHref()).contains("page=1");
        assertThat(response.getLinks().get("first").getHref()).contains("page=0");
        assertThat(response.getLinks().get("last").getHref()).contains("page=4");
        assertThat(response.getLinks().get("next").getHref()).contains("page=2");
        assertThat(response.getLinks().get("prev").getHref()).contains("page=0");
        
        RequestContextHolder.resetRequestAttributes();
    }
}
