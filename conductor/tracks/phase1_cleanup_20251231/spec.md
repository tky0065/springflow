# Specification: Phase 1 Finalization - Integration Tests & Minor Features

## 1. Overview
This track aims to finalize Phase 1 of the SpringFlow MVP by implementing critical integration tests and adding selected polish features. The goal is to ensure the framework is robust, well-tested, and developer-friendly before moving to advanced Phase 2 features.

## 2. Functional Requirements

### 2.1 Integration Tests
*   **Repository Layer:** Implement integration tests using H2 database to verify `RepositoryGenerator` correctly creates working JPA repositories.
    *   Verify CRUD operations on generated repositories.
    *   Verify handling of multiple entities.
*   **Service Layer:** Implement integration tests to verify `GenericCrudService` interactions with generated repositories.
*   **Controller Layer:** Implement full integration tests (using `MockMvc` or `@SpringBootTest`) for `GenericCrudController`.
    *   Verify complete HTTP flow: Request -> Controller -> Service -> Repository -> DB -> Response.
    *   Cover all CRUD methods: GET (list/id), POST, PUT, PATCH, DELETE.

### 2.2 Minor Features & Polish
*   **DtoMapper Integration:** Ensure `GenericCrudController` properly utilizes `DtoMapper` for:
    *   Converting Request Body (Map/Input DTO) to Entity.
    *   Converting Entity to Response Body (Output DTO/Map).
*   **OpenAPI Enhancements:**
    *   Add example values to generated OpenAPI schemas where possible.
    *   Ensure better descriptions for auto-generated endpoints.
*   **Sort Parameter Validation:**
    *   Validate that fields provided in the `sort` query parameter actually exist on the entity.
    *   Throw a clear 400 Bad Request exception for invalid sort fields to prevent database errors.
*   **HATEOAS Support:**
    *   Enhance paged responses to include standard HATEOAS `_links` (self, first, last, next, prev) to improve API navigability.

## 3. Non-Functional Requirements
*   **Test Quality:** All new tests must pass `mvn clean verify`.
*   **Code Coverage:** Maintain or improve existing code coverage metrics.
*   **Performance:** HATEOAS link generation should be lightweight.

## 4. Out of Scope
*   Phase 2 features (Soft Delete, Audit Trail, Advanced Security).
*   Admin UI implementation.
*   CLI Tool implementation.
