package io.springflow.core.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;

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
 *   }
 * }
 * </pre>
 *
 * @param <T> the content type
 */
public class PageResponse<T> {

    /**
     * The page content (list of items).
     */
    private final List<T> content;

    /**
     * Pagination metadata.
     */
    private final PageMetadata page;

    /**
     * Create a PageResponse from a Spring Data Page.
     *
     * @param page the Spring Data page
     */
    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.page = new PageMetadata(page);
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
     * Pagination metadata containing information about the current page.
     */
    public static class PageMetadata {

        /**
         * The size of the page (number of items per page).
         */
        private final int size;

        /**
         * The current page number (0-indexed).
         */
        private final int number;

        /**
         * The total number of elements across all pages.
         */
        private final long totalElements;

        /**
         * The total number of pages.
         */
        private final int totalPages;

        /**
         * Whether this is the first page.
         */
        private final boolean first;

        /**
         * Whether this is the last page.
         */
        private final boolean last;

        /**
         * Whether the page is empty (contains no items).
         */
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
