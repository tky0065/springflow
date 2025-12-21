package io.springflow.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC configuration for SpringFlow pagination and sorting.
 * <p>
 * This configuration customizes Spring Data's pagination and sorting support
 * with SpringFlow-specific defaults and limits.
 * </p>
 */
@Configuration
public class SpringFlowWebConfiguration implements WebMvcConfigurer {

    private final PageableProperties pageableProperties;

    public SpringFlowWebConfiguration() {
        this.pageableProperties = new PageableProperties(); // Default properties
    }

    public SpringFlowWebConfiguration(PageableProperties pageableProperties) {
        this.pageableProperties = pageableProperties;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver pageableResolver = createPageableResolver();
        resolvers.add(pageableResolver);
    }

    /**
     * Create a customized PageableHandlerMethodArgumentResolver with SpringFlow defaults.
     *
     * @return configured resolver
     */
    private PageableHandlerMethodArgumentResolver createPageableResolver() {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver(
                createSortResolver()
        );

        // Set default page size
        resolver.setFallbackPageable(
                PageRequest.of(0, pageableProperties.getDefaultPageSize())
        );

        // Set maximum page size to prevent abuse
        resolver.setMaxPageSize(pageableProperties.getMaxPageSize());

        // Set parameter names
        resolver.setPageParameterName(pageableProperties.getPageParameter());
        resolver.setSizeParameterName(pageableProperties.getSizeParameter());

        // Configure one-indexed parameters if enabled
        resolver.setOneIndexedParameters(pageableProperties.isOneIndexedParameters());

        return resolver;
    }

    /**
     * Create a SortHandlerMethodArgumentResolver for sorting support.
     *
     * @return configured resolver
     */
    private SortHandlerMethodArgumentResolver createSortResolver() {
        SortHandlerMethodArgumentResolver sortResolver = new SortHandlerMethodArgumentResolver();
        sortResolver.setSortParameter(pageableProperties.getSortParameter());
        return sortResolver;
    }

    @Bean
    public PageableProperties pageableProperties() {
        return pageableProperties;
    }
}
