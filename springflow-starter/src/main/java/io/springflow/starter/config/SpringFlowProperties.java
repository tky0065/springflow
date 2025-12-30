package io.springflow.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties for SpringFlow framework.
 * <p>
 * These properties can be configured in application.yml or application.properties
 * under the "springflow" prefix.
 * </p>
 *
 * <p>Example configuration:</p>
 * <pre>
 * springflow:
 *   enabled: true
 *   base-path: /api
 *   base-packages:
 *     - com.example.domain
 *   pagination:
 *     default-page-size: 20
 *     max-page-size: 100
 *   swagger:
 *     enabled: true
 *     title: My API
 *   logging:
 *     log-bot-requests: false
 *     bot-patterns:
 *       - .php
 *       - wp-admin
 * </pre>
 */
@ConfigurationProperties(prefix = "springflow")
public class SpringFlowProperties {

    /**
     * Whether SpringFlow is enabled.
     */
    private boolean enabled = true;

    /**
     * Base path for all generated REST endpoints.
     */
    private String basePath = "/api";

    /**
     * Base packages to scan for @AutoApi annotated entities.
     * If not specified, auto-configuration packages will be used.
     */
    private String[] basePackages = new String[0];

    /**
     * Pagination configuration properties.
     */
    private Pagination pagination = new Pagination();

    /**
     * Swagger/OpenAPI configuration properties.
     */
    private Swagger swagger = new Swagger();

    /**
     * Logging configuration properties.
     */
    private Logging logging = new Logging();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String[] getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(String[] basePackages) {
        this.basePackages = basePackages;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public Swagger getSwagger() {
        return swagger;
    }

    public void setSwagger(Swagger swagger) {
        this.swagger = swagger;
    }

    public Logging getLogging() {
        return logging;
    }

    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    /**
     * Pagination configuration.
     */
    public static class Pagination {
        /**
         * Default page size for paginated endpoints.
         */
        private int defaultPageSize = 20;

        /**
         * Maximum allowed page size.
         */
        private int maxPageSize = 100;

        /**
         * Name of the page parameter.
         */
        private String pageParameter = "page";

        /**
         * Name of the size parameter.
         */
        private String sizeParameter = "size";

        /**
         * Name of the sort parameter.
         */
        private String sortParameter = "sort";

        /**
         * Whether to use 1-indexed page numbers (default is 0-indexed).
         */
        private boolean oneIndexedParameters = false;

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

    /**
     * Swagger/OpenAPI documentation configuration.
     */
    public static class Swagger {
        /**
         * Whether Swagger/OpenAPI documentation is enabled.
         */
        private boolean enabled = true;

        /**
         * API title for Swagger documentation.
         */
        private String title = "SpringFlow API";

        /**
         * API description for Swagger documentation.
         */
        private String description = "Auto-generated REST API documentation";

        /**
         * API version for Swagger documentation.
         */
        private String version = "1.0.0";

        /**
         * Contact name for API documentation.
         */
        private String contactName;

        /**
         * Contact email for API documentation.
         */
        private String contactEmail;

        /**
         * Contact URL for API documentation.
         */
        private String contactUrl;

        /**
         * License name for API documentation.
         */
        private String licenseName;

        /**
         * License URL for API documentation.
         */
        private String licenseUrl;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getContactName() {
            return contactName;
        }

        public void setContactName(String contactName) {
            this.contactName = contactName;
        }

        public String getContactEmail() {
            return contactEmail;
        }

        public void setContactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
        }

        public String getContactUrl() {
            return contactUrl;
        }

        public void setContactUrl(String contactUrl) {
            this.contactUrl = contactUrl;
        }

        public String getLicenseName() {
            return licenseName;
        }

        public void setLicenseName(String licenseName) {
            this.licenseName = licenseName;
        }

        public String getLicenseUrl() {
            return licenseUrl;
        }

        public void setLicenseUrl(String licenseUrl) {
            this.licenseUrl = licenseUrl;
        }
    }

    /**
     * Logging configuration for exception handling and bot detection.
     */
    public static class Logging {
        /**
         * Whether to log bot requests (at DEBUG level).
         * When false, bot requests are logged at DEBUG level.
         * When true, bot requests are logged at INFO level.
         */
        private boolean logBotRequests = false;

        /**
         * List of path patterns that identify bot/scanner requests.
         * These requests will be logged at DEBUG level instead of ERROR.
         */
        private List<String> botPatterns = Arrays.asList(
                ".php",
                "wp-admin",
                "wp-content",
                "wp-includes",
                ".asp",
                ".aspx",
                "phpmyadmin",
                "admin/",
                "cgi-bin",
                ".env",
                ".git"
        );

        public boolean isLogBotRequests() {
            return logBotRequests;
        }

        public void setLogBotRequests(boolean logBotRequests) {
            this.logBotRequests = logBotRequests;
        }

        public List<String> getBotPatterns() {
            return botPatterns;
        }

        public void setBotPatterns(List<String> botPatterns) {
            this.botPatterns = botPatterns;
        }
    }
}
