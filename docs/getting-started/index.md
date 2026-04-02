# Getting Started

Welcome to SpringFlow! This section guides you through setting up and using SpringFlow for the first time.

## :material-book-open-variant: In this section

<div class="grid cards" markdown>

-   :material-rocket-launch: **[Quick Start](quickstart.md)**

    ---

    Create your first API in 5 minutes with SpringFlow

-   :material-download: **[Installation](installation.md)**

    ---

    Detailed installation and configuration guide

-   :material-hammer-wrench: **[First Project](first-project.md)**

    ---

    Build your first complete project with examples

</div>

## :material-target: Prerequisites

Before you begin, make sure you have:

- :fontawesome-brands-java: **Java 17** or higher
- :material-leaf: **Spring Boot 3.2.1** or higher
- :material-package-variant: **Maven 3.6+** or **Gradle 7.0+**

## :material-flash: Quick Installation

=== "Maven"

    ```xml
    <dependency>
        <groupId>io.github.tky0065</groupId>
        <artifactId>springflow-starter</artifactId>
        <version>0.5.1</version>
    </dependency>
    ```

=== "Gradle"

    ```gradle
    implementation 'io.github.tky0065:springflow-starter:0.5.1'
    ```

## :material-rocket-launch: Minimal Example

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
}
```

**That's it!** SpringFlow automatically generates:

:material-check-circle:{ .success } `GET /api/products` — paginated list
:material-check-circle:{ .success } `GET /api/products/{id}` — single entity
:material-check-circle:{ .success } `POST /api/products` — create
:material-check-circle:{ .success } `PUT /api/products/{id}` — full update
:material-check-circle:{ .success } `PATCH /api/products/{id}` — partial update
:material-check-circle:{ .success } `DELETE /api/products/{id}` — delete

## :material-format-list-numbered: Recommended path

1. **[Quick Start](quickstart.md)** — start here to create your first API
2. **[Installation](installation.md)** — detailed configuration
3. **[First Project](first-project.md)** — complete project with examples

---

Need help? See the [full documentation](../guide/annotations.md) or [open an issue](https://github.com/tky0065/springflow/issues).
