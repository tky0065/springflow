package io.springflow.core.security;

import io.springflow.annotations.SecuredApi;
import io.springflow.annotations.Security;
import io.springflow.annotations.SecurityLevel;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds Spring Security SpEL expressions from {@link Security} and {@link SecuredApi} annotations.
 *
 * @author SpringFlow
 * @since 0.1.0
 */
public class SecurityExpressionBuilder {

    /**
     * Builds a SpEL expression for @PreAuthorize based on security configuration and method name.
     *
     * @param security   the security configuration from @Security annotation
     * @param methodName the name of the method to secure
     * @return the SpEL expression
     */
    public String buildExpression(Security security, String methodName) {
        if (security == null || !security.enabled()) {
            return "permitAll()";
        }

        return buildExpression(
                security.level(),
                security.roles(),
                security.authorities(),
                security.readLevel(),
                security.writeLevel(),
                methodName
        );
    }

    /**
     * Builds a SpEL expression for @PreAuthorize based on @SecuredApi configuration and method name.
     *
     * @param securedApi the security configuration from @SecuredApi annotation
     * @param methodName the name of the method to secure
     * @return the SpEL expression
     * @since 0.5.1
     */
    public String buildExpression(SecuredApi securedApi, String methodName) {
        if (securedApi == null) {
            return "permitAll()";
        }

        return buildExpression(
                securedApi.level(),
                securedApi.roles(),
                securedApi.authorities(),
                securedApi.readLevel(),
                securedApi.writeLevel(),
                methodName
        );
    }

    private String buildExpression(SecurityLevel level, String[] roles, String[] authorities,
                                   SecurityLevel readLevel, SecurityLevel writeLevel, String methodName) {
        
        SecurityLevel effectiveLevel = level != null ? level : SecurityLevel.PUBLIC;

        // Apply granular overrides
        if (isReadMethod(methodName) && readLevel != null && readLevel != SecurityLevel.UNDEFINED) {
            effectiveLevel = readLevel;
        } else if (isWriteMethod(methodName) && writeLevel != null && writeLevel != SecurityLevel.UNDEFINED) {
            effectiveLevel = writeLevel;
        }

        if (effectiveLevel == SecurityLevel.PUBLIC) {
            return "permitAll()";
        }

        if (effectiveLevel == SecurityLevel.AUTHENTICATED) {
            return "isAuthenticated()";
        }

        if (effectiveLevel == SecurityLevel.ROLE_BASED) {
            StringBuilder sb = new StringBuilder();

            boolean hasRoles = roles != null && roles.length > 0;
            boolean hasAuthorities = authorities != null && authorities.length > 0;

            if (hasRoles) {
                String rolesStr = Stream.of(roles)
                        .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                        .map(role -> "'" + role + "'")
                        .collect(Collectors.joining(","));
                sb.append("hasAnyRole(").append(rolesStr).append(")");
            }

            if (hasAuthorities) {
                if (hasRoles) {
                    sb.append(" or ");
                }
                String authoritiesStr = Stream.of(authorities)
                        .map(auth -> "'" + auth + "'")
                        .collect(Collectors.joining(","));
                sb.append("hasAnyAuthority(").append(authoritiesStr).append(")");
            }

            return sb.length() > 0 ? sb.toString() : "isAuthenticated()";
        }

        return "permitAll()";
    }

    private boolean isReadMethod(String methodName) {
        return methodName.equals("findAll") || methodName.equals("findById") || methodName.equals("search");
    }

    private boolean isWriteMethod(String methodName) {
        return methodName.equals("create") || methodName.equals("update") || 
               methodName.equals("patch") || methodName.equals("delete") || 
               methodName.equals("restore") || methodName.equals("hardDelete");
    }
}
