# Initial Concept
Auto-generate complete CRUD REST APIs for your JPA entities with a single annotation.

# Product Guide: SpringFlow

## 1. Project Overview
SpringFlow is a sophisticated Spring Boot library designed to eliminate the repetitive boilerplate associated with building RESTful APIs. By using a single annotation, `@AutoApi`, developers can automatically generate high-quality, standardized CRUD endpoints, DTOs, and OpenAPI documentation directly from their JPA entities.

## 2. Target Users
*   **Java/Spring Boot Developers:** Individual developers looking to accelerate development by focusing on business logic rather than infrastructure.
*   **Kotlin Developers:** Developers using Kotlin with Spring Boot who want native support for data classes and standard patterns.
*   **Enterprise Teams:** Organizations that require consistency across their microservices or internal applications through standardized API generation and architecture.

## 3. Core Goals
*   **Zero-Boilerplate Productivity:** Significantly reduce the time to market for new APIs by automating the generation of the entire persistence and web layer.
*   **High Customizability:** Maintain flexibility by allowing developers to easily override default Repositories, Services, and Controllers using simple naming conventions.
*   **Robustness & Security:** Provide enterprise-grade features like JSR-380 validation, dynamic JPA filtering, and integrated security out of the box.

## 4. Key Features
*   **Annotation-Driven Generation:** Use `@AutoApi`, `@Hidden`, and `@ReadOnly` to control API behavior and data visibility at the entity level.
*   **Advanced CRUD Capabilities:** Integrated support for pagination, multi-field sorting, and complex dynamic filtering using JPA Specifications.
*   **Intelligent Extensibility:** Automatic detection and integration of custom components, ensuring that manual overrides blend seamlessly with generated logic.
*   **Polyglot Support:** First-class compatibility with both Java and Kotlin, including full support for modern Java versions (17-25+).
*   **Extended Ecosystem:** Optional modules for GraphQL support (`springflow-graphql`) and comprehensive auditing/soft-delete capabilities.
