package io.springflow.core.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * AuditorAware implementation using {@link SecurityUtils} to get the current user login.
 * <p>
 * Used by Spring Data JPA Auditing to automatically fill {@code @CreatedBy} and {@code @LastModifiedBy} fields.
 * </p>
 *
 * @author SpringFlow
 * @since 0.2.0
 */
public class SpringFlowAuditorAware implements AuditorAware<String> {

    @Override
    @NonNull
    public Optional<String> getCurrentAuditor() {
        return Optional.of(SecurityUtils.getCurrentUserLogin().orElse("system"));
    }
}
