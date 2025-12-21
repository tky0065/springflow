package io.springflow.core.config;

/**
 * Configuration properties for pagination and sorting.
 * <p>
 * These properties can be configured via application.yml under the springflow.pagination prefix.
 * </p>
 *
 * <h3>Example Configuration:</h3>
 * <pre>{@code
 * springflow:
 *   pagination:
 *     default-page-size: 20
 *     max-page-size: 100
 *     page-parameter: page
 *     size-parameter: size
 *     sort-parameter: sort
 * }</pre>
 */
public class PageableProperties {

    /**
     * Default page size when not specified in request.
     * Default: 20
     */
    private int defaultPageSize = 20;

    /**
     * Maximum allowed page size to prevent memory issues.
     * Default: 100
     */
    private int maxPageSize = 100;

    /**
     * Name of the page parameter in query string.
     * Default: "page"
     */
    private String pageParameter = "page";

    /**
     * Name of the size parameter in query string.
     * Default: "size"
     */
    private String sizeParameter = "size";

    /**
     * Name of the sort parameter in query string.
     * Default: "sort"
     */
    private String sortParameter = "sort";

    /**
     * Whether to enable one-indexed page numbers (1-based instead of 0-based).
     * Default: false (0-based)
     */
    private boolean oneIndexedParameters = false;

    // Getters and Setters

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public String getPageParameter() {
        return pageParameter;
    }

    public void setPageParameter(String pageParameter) {
        this.pageParameter = pageParameter;
    }

    public String getSizeParameter() {
        return sizeParameter;
    }

    public void setSizeParameter(String sizeParameter) {
        this.sizeParameter = sizeParameter;
    }

    public String getSortParameter() {
        return sortParameter;
    }

    public void setSortParameter(String sortParameter) {
        this.sortParameter = sortParameter;
    }

    public boolean isOneIndexedParameters() {
        return oneIndexedParameters;
    }

    public void setOneIndexedParameters(boolean oneIndexedParameters) {
        this.oneIndexedParameters = oneIndexedParameters;
    }
}
