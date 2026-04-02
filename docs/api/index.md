# API Reference

Complete technical reference for the SpringFlow API.

## :material-book-open-variant: References

<div class="grid cards" markdown>

-   :material-label: **[Annotations API](annotations.md)**

    ---

    All annotations with detailed parameters

-   :material-cog: **[Configuration Properties](configuration.md)**

    ---

    `springflow.*` YAML properties

-   :material-api: **[Generated Endpoints](endpoints.md)**

    ---

    Automatically generated REST endpoints

</div>

## :material-target: Overview

### Core Annotations

| Annotation | Description | Target |
|------------|-------------|--------|
| `@AutoApi` | Enables API generation | Entity |
| `@Filterable` | Enables dynamic filtering | Field |
| `@Hidden` | Excludes field from DTO | Field |
| `@ReadOnly` | Output only, not accepted in input | Field |
| `@SoftDelete` | Enables logical deletion | Entity |
| `@Auditable` | Automatic audit trail | Entity |

### Configuration Properties

```yaml
springflow:
  enabled: true
  base-path: /api
  base-packages: com.example.myapp
  pagination:
    default-page-size: 20
    max-page-size: 100
  swagger:
    enabled: true
```

### Generated Endpoints

For each entity annotated with `@AutoApi`, SpringFlow generates:

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/{path}` | Paginated list |
| `GET` | `/api/{path}/{id}` | Single entity |
| `POST` | `/api/{path}` | Create |
| `PUT` | `/api/{path}/{id}` | Full update |
| `PATCH` | `/api/{path}/{id}` | Partial update |
| `DELETE` | `/api/{path}/{id}` | Delete |

## :material-book-open-variant: Detailed Documentation

- **[Annotations API](annotations.md)** — parameters, examples, use cases
- **[Configuration Properties](configuration.md)** — all configuration options
- **[Generated Endpoints](endpoints.md)** — request and response formats

---

For practical examples, see the [User Guide](../guide/index.md).
