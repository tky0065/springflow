# Specification: JpaSpecificationExecutor Support

## Overview
This track introduces support for Spring Data JPA `JpaSpecificationExecutor` in generated repositories. This allows for advanced dynamic filtering using JPA Specifications. When enabled via the `@AutoApi` annotation, the framework will generate repositories that extend `JpaSpecificationExecutor` and provide a new `/search` endpoint in the REST controller.

## Functional Requirements
- **Annotation Update:** Add `supportSpecification` attribute to `@AutoApi` (type: `boolean`, default: `false`).
- **Repository Generation:** If `supportSpecification = true`, the generated repository interface must extend `org.springframework.data.jpa.repository.JpaSpecificationExecutor<Entity>`.
- **Filter DTO Generation:** For each entity with specifications enabled, generate a `FilterDTO` that encapsulates possible filterable fields and operators.
- **Search Endpoint:** Add a `POST /search` endpoint to the generated REST controller.
    - It should accept the `FilterDTO` in the request body.
    - It should return a paginated and sorted list of results based on the specifications derived from the `FilterDTO`.
- **Service Layer Update:** The generated service must handle the conversion of the `FilterDTO` into a JPA `Specification` and call the repository's search methods.

## Non-Functional Requirements
- **Type Safety:** The generated `FilterDTO` and search logic must be type-safe.
- **Performance:** Ensure that specification-based queries are optimized and do not introduce significant overhead.
- **Consistency:** Follow existing naming conventions and structural patterns for generated components.

## Acceptance Criteria
- [ ] `@AutoApi` includes the `supportSpecification` attribute.
- [ ] Repositories for entities with `supportSpecification = true` extend `JpaSpecificationExecutor`.
- [ ] A `FilterDTO` is correctly generated for annotated entities.
- [ ] The `POST /search` endpoint correctly filters entities based on provided criteria.
- [ ] Unit and integration tests verify the end-to-end search functionality.

## Out of Scope
- Support for complex nested joins in the initial implementation of the `FilterDTO`.
- Integration with non-JPA data sources.
