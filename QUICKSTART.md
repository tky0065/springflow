# SpringFlow - Quick Start Guide

This guide will help you get started with SpringFlow in 5 minutes.

## What is SpringFlow?

SpringFlow automatically generates complete REST APIs from your JPA entities using simple annotations. Write your entity classes, add `@AutoApi`, and SpringFlow creates repositories, services, controllers, and OpenAPI documentation automatically.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Spring Boot 3.2.1+

## Installation

Add the SpringFlow starter dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.springflow</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Quick Example

### 1. Create an Entity with @AutoApi

```java
@Entity
@Table(name = "products")
@Data
@AutoApi(
    path = "/products",
    description = "Product management API",
    tags = {"Products"}
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100)
    private String name;

    @NotNull
    @Min(0)
    private Double price;

    @ReadOnly
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### 2. Configure SpringFlow (application.yml)

```yaml
spring:
  application:
    name: my-app

  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop
    defer-datasource-initialization: true

springflow:
  enabled: true
  base-path: /api

  pagination:
    default-page-size: 20
    max-page-size: 100

  swagger:
    enabled: true
    title: My API
    description: Auto-generated REST API
    version: 1.0.0
```

### 3. Run Your Application

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

That's it! SpringFlow will automatically:
- ✅ Scan for `@AutoApi` entities
- ✅ Generate JPA repositories with full CRUD operations
- ✅ Create service layer with transaction management
- ✅ Build REST controllers with pagination and sorting
- ✅ Generate OpenAPI/Swagger documentation
- ✅ Apply JSR-380 validation
- ✅ Handle DTOs with `@Hidden` and `@ReadOnly` support

## What Gets Generated

For each `@AutoApi` entity, SpringFlow creates:

**Repository**: `{EntityName}Repository extends JpaRepository`
- Full CRUD operations
- Custom query methods support
- Transaction management

**Service**: `{EntityName}Service`
- `findAll(Pageable)` - List all with pagination
- `findById(ID)` - Find by ID
- `create(DTO)` - Create new entity
- `update(ID, DTO)` - Update existing
- `deleteById(ID)` - Delete by ID
- Transaction management with `@Transactional`
- Business logic hooks (beforeCreate, afterCreate, etc.)

**Controller**: REST endpoints at `/api/{path}`
- `GET /api/products` - List all (paginated)
- `GET /api/products/{id}` - Get one
- `POST /api/products` - Create
- `PUT /api/products/{id}` - Update
- `DELETE /api/products/{id}` - Delete

**Documentation**: OpenAPI specification
- Available at `/v3/api-docs`
- Swagger UI at `/swagger-ui.html`

## Field-Level Annotations

### @Hidden
Excludes field from all DTOs (input and output):

```java
@Hidden
@Column(nullable = false)
private String password;  // Never exposed in API
```

### @ReadOnly
Field appears in output DTOs only, not in input DTOs:

```java
@ReadOnly
@Column(updatable = false)
private LocalDateTime createdAt;  // Returned but not accepted in POST/PUT
```

### @Filterable
Enables dynamic filtering on the field:

```java
@Filterable(types = {FilterType.EQUALS, FilterType.LIKE})
private String name;
```

## Validation Support

SpringFlow fully supports JSR-380 Bean Validation:

```java
@NotBlank(message = "Name is required")
@Size(min = 3, max = 50)
private String name;

@Email
private String email;

@Min(0)
@Max(100)
private Integer stock;

@Past
private LocalDate birthDate;
```

Validation errors return detailed responses:

```json
{
  "timestamp": "2025-12-21T19:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "validationErrors": [
    {
      "field": "name",
      "message": "Name is required",
      "rejectedValue": null,
      "code": "NotBlank"
    }
  ]
}
```

## Pagination & Sorting

All list endpoints support pagination and sorting:

```bash
# Pagination
GET /api/products?page=0&size=10

# Sorting (ascending)
GET /api/products?sort=name,asc

# Multiple sort fields
GET /api/products?sort=category,asc&sort=price,desc

# Combined
GET /api/products?page=0&size=20&sort=createdAt,desc
```

## Configuration Options

```yaml
springflow:
  enabled: true                          # Enable/disable SpringFlow
  base-path: /api                        # Base path for all endpoints
  base-packages:                         # Explicit packages to scan (optional)
    - com.example.entities

  pagination:
    default-page-size: 20                # Default page size
    max-page-size: 100                   # Maximum allowed page size
    page-parameter: page                 # Query parameter name for page
    size-parameter: size                 # Query parameter name for size
    sort-parameter: sort                 # Query parameter name for sort
    one-indexed-parameters: false        # Use 1-based page indexing

  swagger:
    enabled: true                        # Enable Swagger UI
    title: My API                        # API title
    description: API Description         # API description
    version: 1.0.0                       # API version
    contact-name: Support Team           # Contact name
    contact-email: support@example.com   # Contact email
    contact-url: https://example.com     # Contact URL
    license-name: Apache 2.0             # License name
    license-url: https://...             # License URL
```

## Kotlin Support

SpringFlow works with Kotlin data classes:

```kotlin
@Entity
@AutoApi(path = "/users")
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank
    @field:Size(min = 3, max = 50)
    val name: String,

    @field:Email
    val email: String?,

    @Hidden
    val password: String? = null
) {
    constructor() : this(id = null, name = "", email = null)
}
```

**Important**: Use `@field:` prefix for validation annotations in Kotlin.

## Demo Application

Check out the full demo application in `/springflow-demo`:

```bash
cd springflow-demo
../mvnw spring-boot:run
```

The demo includes three entities (Product, Category, User) with:
- ✅ Hierarchical relationships (Category self-reference)
- ✅ Many-to-One relationships (Product → Category)
- ✅ Complex validation rules
- ✅ @Hidden fields (passwords)
- ✅ @ReadOnly timestamps
- ✅ Enum support (UserRole)
- ✅ Sample data (data.sql)

Access points:
- API Base: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs
- H2 Console: http://localhost:8080/h2-console

## Troubleshooting

### Entities not being scanned

Ensure your entities are in a package that Spring Boot can scan. By default, SpringFlow scans the same packages as your `@SpringBootApplication` class.

Explicitly configure packages if needed:

```yaml
springflow:
  base-packages:
    - com.example.entities
```

### Tables not found on startup

If using `data.sql`, set:

```yaml
spring:
  jpa:
    defer-datasource-initialization: true
```

This ensures Hibernate creates tables before `data.sql` runs.

### Lombok compatibility

SpringFlow requires Lombok 1.18.36+ for Java 21+ compatibility. Update if needed:

```xml
<lombok.version>1.18.38</lombok.version>
```

## Next Steps

- Read the full [README.md](README.md) for advanced features
- Check [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines
- See [roadmap.md](roadmap.md) for upcoming features
- Explore the [demo application](/springflow-demo) for complete examples

## Need Help?

- 📚 Documentation: See README.md
- 🐛 Bug Reports: [GitHub Issues](https://github.com/springflow/springflow/issues)
- 💬 Questions: Create a discussion on GitHub

## Current Status (v0.1.0-SNAPSHOT)

**Phase 1 MVP - Implementation Complete**:
- ✅ Core annotations (@AutoApi, @Hidden, @ReadOnly, @Filterable)
- ✅ Entity scanning and metadata resolution
- ✅ Dynamic repository generation
- ✅ Service layer with transaction management
- ✅ Controller generation (beans registered)
- ✅ DTO mapping with reflection
- ✅ JSR-380 validation with detailed errors
- ✅ Pagination and sorting configuration
- ✅ OpenAPI/Swagger integration
- ✅ Spring Boot auto-configuration
- ✅ Kotlin support
- ✅ Lombok compatibility (Java 25)
- ⚠️ Controller request mapping registration (in progress)

**Known Limitations**:
- Controller endpoints are created but HTTP mappings not fully registered yet
- Requires Spring Boot 3.2.1+, Java 17+
- Kotlin support requires Java 17 (incompatible with Java 25 due to compiler)

**Coming in Phase 2**:
- Dynamic filtering with `@Filterable`
- Security and role-based authorization
- Soft delete support
- Audit trail (createdAt, updatedAt, createdBy, updatedBy)

---

**License**: Apache 2.0
**Version**: 0.1.0-SNAPSHOT
**Minimum Java**: 17
**Spring Boot**: 3.2.1+
