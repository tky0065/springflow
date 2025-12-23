package io.springflow.core.security;

import io.springflow.annotations.Security;
import io.springflow.annotations.SecurityLevel;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds Spring Security SpEL expressions from {@link Security} annotations.
 *
 * @author SpringFlow
 * @since 0.1.0
 */
public class SecurityExpressionBuilder {

    /**
     * Builds a SpEL expression for @PreAuthorize based on security configuration and method name.
     *
     * @param security   the security configuration from annotation
     * @param methodName the name of the method to secure
     * @return the SpEL expression
     */
    public String buildExpression(Security security, String methodName) {
        if (security == null || !security.enabled()) {
            return "permitAll()";
        }

        SecurityLevel effectiveLevel = security.level() != null ? security.level() : SecurityLevel.PUBLIC;
        
        // Apply granular overrides
        if (isReadMethod(methodName) && security.readLevel() != null && security.readLevel() != SecurityLevel.UNDEFINED) {
            effectiveLevel = security.readLevel();
        } else if (isWriteMethod(methodName) && security.writeLevel() != null && security.writeLevel() != SecurityLevel.UNDEFINED) {
            effectiveLevel = security.writeLevel();
        }

        if (effectiveLevel == SecurityLevel.PUBLIC) {
            return "permitAll()";
        }

        if (effectiveLevel == SecurityLevel.AUTHENTICATED) {
            return "isAuthenticated()";
        }

        if (effectiveLevel == SecurityLevel.ROLE_BASED) {
            StringBuilder sb = new StringBuilder();
            
            boolean hasRoles = security.roles().length > 0;
            boolean hasAuthorities = security.authorities().length > 0;

            if (hasRoles) {
                String roles = Stream.of(security.roles())
                        .map(role -> "'" + role + "'")
                        .collect(Collectors.joining(","));
                sb.append("hasAnyRole(").append(roles).append(")");
            }

            if (hasAuthorities) {
                if (hasRoles) {
                    sb.append(" or ");
                }
                String authorities = Stream.of(security.authorities())
                        .map(auth -> "'" + auth + "'")
                        .collect(Collectors.joining(","));
                sb.append("hasAnyAuthority(").append(authorities).append(")");
            }

            return sb.length() > 0 ? sb.toString() : "isAuthenticated()";
        }

        return "permitAll()";
    }

    private boolean isReadMethod(String methodName) {
        return methodName.equals("findAll") || methodName.equals("findById");
    }

    private boolean isWriteMethod(String methodName) {
        return methodName.equals("create") || methodName.equals("update") || methodName.equals("delete");
    }
}
