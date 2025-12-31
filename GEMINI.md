# SpringFlow Context

This file provides context for the SpringFlow project to assist with future tasks.

## Project Overview

**SpringFlow** is a Spring Boot library that automates the generation of complete REST APIs (Controllers, Services, Repositories, DTOs, OpenAPI documentation) from JPA entities using a single annotation: `@AutoApi`.

### Key Features
*   **Zero Boilerplate:** Auto-generates CRUD endpoints, DTOs, and OpenApi specs.
*   **Customizable:** Supports custom implementations for repositories, services, and controllers (detected by naming convention).
*   **Validation:** Full JSR-380 support.
*   **Advanced:** Pagination, Sorting, Filtering, Soft Delete, Audit.
*   **Polyglot:** Supports both Java and Kotlin.
*   **GraphQL:** Optional support via `springflow-graphql`.

## Project Structure

This is a multi-module Maven project:

*   `springflow-annotations`: Contains core annotations like `@AutoApi`, `@Hidden`, `@ReadOnly`.
*   `springflow-core`: Core framework logic (logic for generation, runtime handling).
*   `springflow-starter`: Spring Boot Starter for easy integration.
*   `springflow-graphql`: Module for GraphQL support.
*   `springflow-demo`: Example application showcasing usage.
*   `docs/`: Project documentation (MkDocs).

## Technology Stack

*   **Language:** Java 25 (configured in pom), compatible with Java 17+.
*   **Framework:** Spring Boot 4.0.1+.
*   **Build System:** Maven.
*   **Dependencies:**
    *   Spring Data JPA
    *   SpringDoc OpenAPI
    *   MapStruct (DTO mapping)
    *   Lombok
    *   Byte Buddy (Dynamic class generation)
    *   Kotlin (Support enabled)

## Build & Run Instructions

### Prerequisites
*   Java 17+ (Project configured for Java 25 source/target).
*   Maven (Wrapper `mvnw` included).

### Common Commands

**Build Project:**
```bash
./mvnw clean install
```

**Run Tests:**
```bash
./mvnw test
```

**Run Full Verification (Build + Test + Quality Checks):**
```bash
./mvnw clean verify
```

**Run Demo Application:**
```bash
cd springflow-demo
../mvnw spring-boot:run
```
*   API: `http://localhost:8080/api/products`
*   Swagger UI: `http://localhost:8080/swagger-ui.html`
*   H2 Console: `http://localhost:8080/h2-console`

## Development Conventions

### Coding Standards
*   **Style:** Follow standard Spring Boot conventions.
*   **Lombok:** Use Lombok for boilerplate reduction (Getters, Setters, Data, Slf4j).
*   **Documentation:** Javadoc required for public APIs.
*   **Logging:** Use SLF4J.

### Commit Messages
Follow **Conventional Commits**:
*   `feat(module): description`
*   `fix(module): description`
*   `docs: description`
*   `test: description`
*   `refactor: description`
*   `chore: description`

### Testing
*   **Coverage:** Aim for > 80% test coverage.
*   **Types:**
    *   Unit tests for components.
    *   `@SpringBootTest` for integration.
    *   Use H2 in-memory DB for tests.

### Contribution Workflow
1.  Fork & Clone.
2.  Branch: `feat/your-feature`.
3.  Implement & Test.
4.  Verify: `./mvnw clean verify`.
5.  Commit & Push.

## Documentation
*   Main `README.md` provides excellent usage guide.
*   `docs/` folder contains detailed MkDocs documentation.
