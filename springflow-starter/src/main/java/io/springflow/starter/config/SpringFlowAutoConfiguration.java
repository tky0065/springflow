package io.springflow.starter.config;

import io.springflow.core.config.PageableProperties;
import io.springflow.core.config.SpringFlowWebConfiguration;
import io.springflow.core.controller.GlobalExceptionHandler;
import io.springflow.core.controller.support.RequestMappingRegistrar;
import io.springflow.core.filter.FilterResolver;
import io.springflow.core.mapper.DtoMapperFactory;
import io.springflow.core.repository.AutoApiRepositoryRegistrar;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;

/**
 * Auto-configuration for SpringFlow framework.
 * <p>
 * This configuration is automatically activated when:
 * <ul>
 *   <li>JPA EntityManager is on the classpath</li>
 *   <li>springflow.enabled property is true (default)</li>
 * </ul>
 * </p>
 *
 * <p>
 * The auto-configuration sets up:
 * <ul>
 *   <li>Dynamic repository, service, and controller generation via {@link AutoApiRepositoryRegistrar}</li>
 *   <li>Request mapping registration via {@link RequestMappingRegistrar}</li>
 *   <li>Global exception handling via {@link GlobalExceptionHandler}</li>
 *   <li>Pagination and sorting configuration via {@link SpringFlowWebConfiguration}</li>
 * </ul>
 * </p>
 *
 * <p>Configuration can be customized via {@link SpringFlowProperties}</p>
 */
@AutoConfiguration
@ConditionalOnClass(EntityManager.class)
@ConditionalOnProperty(prefix = "springflow", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SpringFlowProperties.class)
@Import({
    AutoApiRepositoryRegistrar.class,
    RequestMappingRegistrar.class,
    GlobalExceptionHandler.class,
    SpringFlowWebConfiguration.class,
    OpenApiConfiguration.class
})
public class SpringFlowAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SpringFlowAutoConfiguration.class);

    public SpringFlowAutoConfiguration(SpringFlowProperties properties) {
        log.info("SpringFlow auto-configuration activated");
        log.info("Base path: {}", properties.getBasePath());
        log.info("Pagination - default page size: {}, max page size: {}",
                properties.getPagination().getDefaultPageSize(),
                properties.getPagination().getMaxPageSize());

        if (properties.getBasePackages().length > 0) {
            log.info("Scanning packages: {}", String.join(", ", properties.getBasePackages()));
        } else {
            log.info("Using auto-configuration packages for entity scanning");
        }
    }

    /**
     * Creates PageableProperties bean from SpringFlowProperties configuration.
     * This allows pagination settings to be configured via application.yml.
     */
    @Bean
    @ConditionalOnProperty(prefix = "springflow", name = "enabled", havingValue = "true", matchIfMissing = true)
    public PageableProperties pageableProperties(SpringFlowProperties properties) {
        PageableProperties pageableProps = new PageableProperties();
        SpringFlowProperties.Pagination pagination = properties.getPagination();

        pageableProps.setDefaultPageSize(pagination.getDefaultPageSize());
        pageableProps.setMaxPageSize(pagination.getMaxPageSize());
        pageableProps.setPageParameter(pagination.getPageParameter());
        pageableProps.setSizeParameter(pagination.getSizeParameter());
        pageableProps.setSortParameter(pagination.getSortParameter());
        pageableProps.setOneIndexedParameters(pagination.isOneIndexedParameters());

        log.debug("PageableProperties configured with default page size: {}, max page size: {}",
                pageableProps.getDefaultPageSize(), pageableProps.getMaxPageSize());

        return pageableProps;
    }

    /**
     * Creates DtoMapperFactory bean for entity-DTO conversions.
     * Used by controllers to handle @Hidden and @ReadOnly fields.
     */
    @Bean
    @ConditionalOnProperty(prefix = "springflow", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DtoMapperFactory dtoMapperFactory() {
        log.debug("Creating DtoMapperFactory bean");
        return new DtoMapperFactory();
    }

    /**
     * Creates FilterResolver bean for dynamic query filtering.
     * Uses the application's ConversionService for type conversion.
     */
    @Bean
    @ConditionalOnProperty(prefix = "springflow", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterResolver filterResolver(ConversionService conversionService) {
        log.debug("Creating FilterResolver bean");
        return new FilterResolver(conversionService);
    }
}
