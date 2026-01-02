package io.springflow.core.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom page response DTO to avoid Spring Data's PageImpl serialization warning.
 * <p>
 * This class wraps Spring Data's {@link Page} interface to provide a stable,
 * explicit JSON structure for paginated responses. Using this wrapper instead of
 * serializing PageImpl directly eliminates the warning about unstable JSON structure.
 * </p>
 * <p>
 * The JSON structure is designed to maintain backward compatibility with the
 * default PageImpl serialization format.
 * </p>
 *
 * <p>Example JSON output:</p>
 * <pre>
 * {
 *   "content": [
 *     { "id": 1, "name": "Item 1" },
 *     { "id": 2, "name": "Item 2" }
 *   ],
 *   "page": {
 *     "size": 20,
 *     "number": 0,
 *     "totalElements": 100,
 *     "totalPages": 5,
 *     "first": true,
 *     "last": false,
 *     "empty": false
 *   },
 *   "_links": {
 *     "self": { "href": "..." },
 *     "first": { "href": "..." }
 *   }
 * }
 * </pre>
 *
 * @param <T> the content type
 */
@Schema(description = "Paginated response wrapper")
public class PageResponse<T> {

    /**
     * The page content (list of items).
     */
    @Schema(description = "List of items in the current page")
    private final List<T> content;

    /**
     * Pagination metadata.
     */
    @Schema(description = "Pagination metadata")
    private final PageMetadata page;

    /**
     * HATEOAS links.
     */
    @Schema(description = "HATEOAS links")
    @JsonProperty("_links")
    private final Map<String, Link> links;

    /**
     * Create a PageResponse from a Spring Data Page.
     *
     * @param page the Spring Data page
     */
    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.page = new PageMetadata(page);
        this.links = generateLinks(page);
    }

    /**
     * Get the page content.
     *
     * @return the list of items in this page
     */
    public List<T> getContent() {
        return content;
    }

    /**
     * Get the pagination metadata.
     *
     * @return the page metadata
     */
    public PageMetadata getPage() {
        return page;
    }

    /**
     * Get the HATEOAS links.
     *
     * @return the map of links
     */
    public Map<String, Link> getLinks() {
        return links;
    }

    private Map<String, Link> generateLinks(Page<T> page) {
        Map<String, Link> links = new HashMap<>();

        try {
            // Check if RequestContext is available to avoid issues in non-web contexts
            ServletUriComponentsBuilder.fromCurrentRequest();
        } catch (Exception e) {
            return links;
        }

        // Self
        links.put("self", new Link(createUri(page.getNumber(), page.getSize())));

        // First
        links.put("first", new Link(createUri(0, page.getSize())));

        // Last
        int lastPage = Math.max(0, page.getTotalPages() - 1);
        links.put("last", new Link(createUri(lastPage, page.getSize())));

        // Next
        if (page.hasNext()) {
            links.put("next", new Link(createUri(page.getNumber() + 1, page.getSize())));
        }

        // Prev
        if (page.hasPrevious()) {
            links.put("prev", new Link(createUri(page.getNumber() - 1, page.getSize())));
        }

        return links;
    }

    private String createUri(int page, int size) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("page", page)
                .replaceQueryParam("size", size)
                .toUriString();
    }

    /**
     * Link object.
     */
    @Schema(description = "Link object")
    public static class Link {
        @Schema(description = "The target URI", example = "http://localhost:8080/api/products?page=0&size=20")
        private final String href;

        public Link(String href) {
            this.href = href;
        }

        public String getHref() {
            return href;
        }
    }

    /**
     * Pagination metadata containing information about the current page.
     */
    @Schema(description = "Metadata about the pagination state")
    public static class PageMetadata {

        /**
         * The size of the page (number of items per page).
         */
        @Schema(description = "Number of items per page", example = "20")
        private final int size;

        /**
         * The current page number (0-indexed).
         */
        @Schema(description = "Current page number (0-based)", example = "0")
        private final int number;

        /**
         * The total number of elements across all pages.
         */
        @Schema(description = "Total number of items across all pages", example = "100")
        private final long totalElements;

        /**
         * The total number of pages.
         */
        @Schema(description = "Total number of pages", example = "5")
        private final int totalPages;

        /**
         * Whether this is the first page.
         */
        @Schema(description = "Is this the first page?", example = "true")
        private final boolean first;

        /**
         * Whether this is the last page.
         */
        @Schema(description = "Is this the last page?", example = "false")
        private final boolean last;

        /**
         * Whether the page is empty (contains no items).
         */
        @Schema(description = "Is the page empty?", example = "false")
        private final boolean empty;

        /**
         * Create PageMetadata from a Spring Data Page.
         *
         * @param page the Spring Data page
         */
        public PageMetadata(Page<?> page) {
            this.size = page.getSize();
            this.number = page.getNumber();
            this.totalElements = page.getTotalElements();
            this.totalPages = page.getTotalPages();
            this.first = page.isFirst();
            this.last = page.isLast();
            this.empty = page.isEmpty();
        }

        public int getSize() {
            return size;
        }

        public int getNumber() {
            return number;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public boolean isFirst() {
            return first;
        }

        public boolean isLast() {
            return last;
        }

        public boolean isEmpty() {
            return empty;
        }
    }
}
