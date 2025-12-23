package io.springflow.starter.config;

import io.springflow.core.security.SpringFlowAuditorAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration for Spring Data JPA Auditing.
 * <p>
 * Enables automatic filling of auditing fields like {@code @CreatedBy}, {@code @LastModifiedBy},
 * {@code @CreatedDate}, and {@code @LastModifiedDate}.
 * </p>
 *
 * @author SpringFlow
 * @since 0.2.0
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "springFlowAuditorAware")
@ConditionalOnProperty(prefix = "springflow", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JpaAuditingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JpaAuditingConfiguration.class);

    /**
     * Registers the AuditorAware bean if not already provided by the application.
     * This bean provides the current user login for auditing purposes.
     *
     * @return the SpringFlow implementation of AuditorAware
     */
    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    public AuditorAware<String> springFlowAuditorAware() {
        log.debug("Creating SpringFlowAuditorAware bean");
        return new SpringFlowAuditorAware();
    }
}
