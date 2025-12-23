# SpringFlow Context for Gemini

This file provides context and instructions for Gemini when working on the SpringFlow project.

## Project Overview

**SpringFlow** is a Spring Boot library that automatically generates complete REST APIs (Controller, Service, Repository, DTOs, Swagger) from JPA entities using a single annotation: `@AutoApi`.

- **Goal**: Reduce boilerplate by 70-90% for standard CRUD operations.
- **Key Features**: Runtime generation, Pagination/Sorting, Validation (JSR-380), Auto DTOs (with `@Hidden`/`@ReadOnly`), OpenAPI documentation.
- **Tech Stack**: Java 17+, Spring Boot 3.2+, Spring Data JPA, Lombok, MapStruct, Kotlin support.

## Project Structure (Maven Multi-Module)

```
springflow/
├── springflow-annotations/  # Core annotations (Zero dependencies)
├── springflow-core/         # Core logic (Scanning, Metadata, Generation)
├── springflow-starter/      # Spring Boot Auto-configuration
└── springflow-demo/         # Demo application & Integration tests
```

**Dependency Flow**: `springflow-demo` -> `springflow-starter` -> `springflow-core` -> `springflow-annotations`

## Core Architecture

SpringFlow uses **runtime annotation processing** via Spring's `BeanDefinitionRegistryPostProcessor`, NOT compile-time APT.

1.  **EntityScanner**: Scans classpath for `@AutoApi` entities.
2.  **MetadataResolver**: Introspects entities via reflection to build immutable `EntityMetadata`.
3.  **Generators**:
    *   `RepositoryGenerator`: Creates `SimpleJpaRepository` beans.
    *   `ServiceGenerator`, `ControllerGenerator`, `DtoMapperFactory`: Scaffolded/Implemented to generate respective beans dynamically.
4.  **Registrar**: `AutoApiRepositoryRegistrar` orchestrates the process during Spring startup.

## Build & Run Commands

### Building
*   **Build All**: `./mvnw clean install`
*   **Skip Tests**: `./mvnw clean install -DskipTests`

### Testing
*   **Run All Tests**: `./mvnw test`
*   **Coverage**: `./mvnw test jacoco:report` (Report at `target/site/jacoco/index.html`)

### Running Demo
*   **Start**: `cd springflow-demo && ../mvnw spring-boot:run`
*   **Access**:
    *   API: `http://localhost:8080/api/{entity}`
    *   Swagger: `http://localhost:8080/swagger-ui.html`
    *   H2 Console: `http://localhost:8080/h2-console`

## Development Conventions

*   **Java Version**: Java 17+.
*   **Style**: Follow Spring Boot conventions.
*   **Lombok**: Use `@Data`, `@Builder`, `@Slf4j` to reduce boilerplate.
*   **MapStruct**: Used for Entity <-> DTO mapping.
*   **Logging**: SLF4J (via Lombok). Levels: `DEBUG` (internals), `INFO` (startup), `WARN` (config issues), `ERROR` (failures).
*   **Testing**:
    *   Unit tests for core logic (`EntityScanner`, `MetadataResolver`).
    *   Integration tests (`@SpringBootTest`) for the full flow.
    *   Use H2 in-memory DB.
    *   Target >80% coverage.

## Git & Commits

Follow **Conventional Commits**:
*   `feat(module): ...`
*   `fix(module): ...`
*   `test: ...`
*   `docs: ...`
*   `refactor: ...`
*   `chore: ...`

## Implementation Details

*   **Runtime Processing**: The core module explicitly disables annotation processing (`-proc:none`) to avoid conflicts, as it relies on runtime reflection.
*   **Metadata**: Uses Java Records (`EntityMetadata`, `FieldMetadata`) for immutability.
*   **Bean Registration**: Uses `GenericBeanDefinition` to register generated components programmatically.

## Current Status (MVP Phase 1)

*   [x] Annotations & Metadata
*   [x] Repository Generation
*   [x] Service/Controller/DTO Generation
*   [x] Validation & Pagination
*   [x] Swagger Integration
*   [ ] Phase 2: Dynamic Filters (`@Filterable`), Security, Soft Delete.
