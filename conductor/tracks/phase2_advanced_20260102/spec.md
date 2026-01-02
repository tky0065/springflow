# Specification: Phase 2 - Advanced Security & JPA Relationships

## 1. Overview
This phase enhances SpringFlow with production-grade security and robust handling of complex JPA entity relationships. Users will be able to secure their auto-generated APIs using declarative annotations and navigate entity graphs without circular reference issues.

## 2. Functional Requirements

### 2.1 Declarative API Security
*   **Security Annotations:** Introduce `@SecuredApi` (or enhance `@AutoApi`) to define role-based access control (RBAC) at the entity level.
*   **Method-Level Security:** Allow defining different roles for different CRUD operations (e.g., `READ` allowed for `ROLE_USER`, `WRITE` only for `ROLE_ADMIN`).
*   **Spring Security Integration:** Automatically apply these constraints to the generated controllers using Spring Security's `AccessDecisionManager` or `@PreAuthorize` logic during dynamic bean registration.

### 2.2 Advanced Relationship Mapping
*   **JPA Relationship Support:** Ensure `DtoMapper` correctly handles `@OneToMany`, `@ManyToOne`, `@OneToOne`, and `@ManyToMany`.
*   **Circular Reference Prevention:** Implement a robust mechanism (e.g., recursion depth limit or `@JsonIdentityInfo` equivalent) to prevent `StackOverflowError` or infinite JSON loops.
*   **Lazy Loading Handling:** Prevent `LazyInitializationException` during DTO mapping by either forcing initialization or allowing users to exclude specific relation fields from DTOs via annotations.
*   **Relationship DTOs:** Allow basic DTO representation of related entities (e.g., just the ID or a few key fields) instead of the full object.

### 2.3 Custom Endpoint Merging
*   **Method Detection:** Improve the `RequestMappingRegistrar` to detect custom methods in user-provided controller subclasses and ensure they don't conflict with generated CRUD methods.
*   **Override Support:** Allow users to override specific CRUD methods by simply implementing them in their controller subclass.

## 3. Non-Functional Requirements
*   **Performance:** DTO mapping for deep hierarchies should be optimized.
*   **Developer Experience:** Security configuration should be intuitive and minimal.

## 4. Out of Scope
*   GraphQL automation (Phase 3).
*   Admin UI (Phase 3).
