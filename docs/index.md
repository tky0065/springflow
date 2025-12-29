# SpringFlow Documentation

**Auto-generate complete REST APIs from JPA entities with a single annotation.**

[![Build Status](https://github.com/tky0065/springflow/workflows/Build%20and%20Test/badge.svg)](https://github.com/tky0065/springflow/actions)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.tky0065/springflow-starter.svg)](https://central.sonatype.com/artifact/io.github.tky0065/springflow-starter)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Coverage](https://codecov.io/gh/tky0065/springflow/branch/main/graph/badge.svg)](https://codecov.io/gh/tky0065/springflow)

---

## What is SpringFlow?

SpringFlow is a Spring Boot library that **reduces 70-90% of boilerplate code** by automatically generating:

- :material-cached: **Repositories** (JpaRepository with Specifications)
- :material-wrench: **Services** (CRUD operations with transaction management)
- :material-web: **REST Controllers** (Complete CRUD endpoints)
- :material-file-document: **DTOs** (Input/Output mapping with validation)
- :material-book-open-variant: **OpenAPI Documentation** (Swagger UI integration)

All from a single `@AutoApi` annotation on your JPA entities.

---

## Quick Example

```java
@Entity
@AutoApi(path = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @Min(0)
    private BigDecimal price;

    @Hidden
    private String internalCode;

    @ManyToOne
    private Category category;
}
```

**That's it!** SpringFlow automatically generates:

- :material-check-circle:{ .success } `GET /api/products` - List with pagination & sorting
- :material-check-circle:{ .success } `GET /api/products/{id}` - Get by ID
- :material-check-circle:{ .success } `POST /api/products` - Create
- :material-check-circle:{ .success } `PUT /api/products/{id}` - Update
- :material-check-circle:{ .success } `DELETE /api/products/{id}` - Delete
- :material-check-circle:{ .success } Complete OpenAPI/Swagger documentation
- :material-check-circle:{ .success } Input validation with JSR-380
- :material-check-circle:{ .success } DTO mapping (excludes `@Hidden` fields)
- :material-check-circle:{ .success } Repository, Service, and Controller beans

---

## Key Features

### :material-rocket-launch: Zero Boilerplate
Write only your domain model. SpringFlow generates everything else at runtime.

### :material-lock: Production Ready
- Transaction management
- Exception handling
- Input validation (JSR-380)
- Security integration
- Soft delete support
- Audit trail

### :material-target: Flexible & Extensible
- Override generated behavior
- Add custom endpoints
- Configure via annotations or YAML
- Works with existing Spring components

### :material-chart-bar: Advanced Filtering
```java
@Filterable(types = {FilterType.EQUALS, FilterType.LIKE, FilterType.RANGE})
private String name;
```

Enables: `GET /api/products?name_like=Phone&price_range=100,500`

### :material-shield-lock: Built-in Security
```java
@AutoApi(
    path = "users",
    security = @Security(
        enabled = true,
        roles = {"ADMIN", "USER"}
    )
)
```

### :material-delete: Soft Delete
```java
@Entity
@AutoApi
@SoftDelete
public class Article { ... }
```

Adds `GET /api/articles?includeDeleted=true` and `POST /api/articles/{id}/restore`

---

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.4.3</version>
</dependency>
```

### Gradle

```gradle
implementation 'io.github.tky0065:springflow-starter:0.4.3'
```

---

## Getting Started

1. **[Quick Start Guide](getting-started/quickstart.md)** - Get up and running in 5 minutes
2. **[Installation](getting-started/installation.md)** - Detailed setup instructions
3. **[First Project](getting-started/first-project.md)** - Build your first API

---

## Requirements

- :fontawesome-brands-java: Java 17 or higher
- :material-leaf: Spring Boot 3.2.1 or higher
- :material-package-variant: Maven 3.6+ or Gradle 7.0+

---

## Language Support

SpringFlow supports both **Java** and **Kotlin**:

=== "Java"
    ```java
    @Entity
    @AutoApi(path = "products")
    public class Product {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank
        private String name;
    }
    ```

=== "Kotlin"
    ```kotlin
    @Entity
    @AutoApi(path = "products")
    data class Product(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        @field:NotBlank
        val name: String
    )
    ```

See [Kotlin Support Guide](guide/kotlin.md) for details.

---

## What's Next?

### Core Concepts
- [Annotations Reference](api/annotations.md)
- [Configuration Properties](api/configuration.md)
- [Generated Endpoints](api/endpoints.md)

### Advanced Topics
- [Architecture Overview](advanced/architecture.md)
- [Custom Endpoints](advanced/custom-endpoints.md)
- [Performance Tuning](advanced/performance.md)

### Contributing
- [Contributing Guide](development/contributing.md)
- [Development Setup](development/building.md)

---

## Community & Support

- :material-book-open-variant: **Documentation**: [https://tky0065.github.io/springflow/](https://tky0065.github.io/springflow/)
- :material-bug: **Issues**: [GitHub Issues](https://github.com/tky0065/springflow/issues)
- :material-chat: **Discussions**: [GitHub Discussions](https://github.com/tky0065/springflow/discussions)
- :material-package-variant: **Maven Central**: [SpringFlow Artifacts](https://central.sonatype.com/artifact/io.github.tky0065/springflow-starter)

---

## License

SpringFlow is released under the [Apache License 2.0](about/license.md).
