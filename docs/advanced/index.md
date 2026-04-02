# Advanced Topics

Deep-dive topics for optimizing and extending SpringFlow in your applications.

## :material-book-open-variant: In this section

<div class="grid cards" markdown>

-   :material-chart-tree: **[Architecture](architecture.md)**

    ---

    Understand SpringFlow's internal architecture

-   :material-cog-outline: **[Custom Components](custom-components.md)**

    ---

    Custom repositories, services, and controllers — 4 override scenarios

-   :material-api: **[Custom Endpoints](custom-endpoints.md)**

    ---

    Add your own endpoints alongside generated ones

-   :material-speedometer: **[Performance](performance.md)**

    ---

    Performance optimization and production tuning

-   :material-star-check: **[Best Practices](best-practices.md)**

    ---

    Patterns and recommendations

</div>

## :material-target: Who is this for?

This section is for developers who want to:

- **Understand** how SpringFlow works under the hood
- **Override** repositories, services, and controllers with custom implementations
- **Extend** the default behavior with custom endpoints
- **Optimize** performance for production workloads
- **Apply** recommended patterns

## :material-wrench: Key Concepts

### Runtime Architecture

SpringFlow generates components **at runtime** via:

- `BeanDefinitionRegistryPostProcessor` for bean registration
- Reflection API for entity introspection
- Dynamic proxies for generated repositories

### Extensibility

SpringFlow is designed to be extended with your own components:

- **Auto-detection** — SpringFlow detects and respects your custom repositories, services, and controllers
- **4 override scenarios** — repository only, service only, controller only, or all three
- **Base classes** — extend `GenericCrudService` and `GenericCrudController` to inherit built-in behavior
- **Lifecycle hooks** — `beforeCreate`, `afterCreate`, `beforeUpdate`, `afterUpdate`, `beforeDelete`, `afterDelete`
- **JPA Specifications** — custom repositories can add their own query methods
- **Non-invasive** — fully compatible with your existing Spring architecture

### Performance

Key performance characteristics:

- Automatic fetch joins to prevent N+1 queries
- Entity metadata caching
- Efficient pagination with optimized COUNT queries
- Lightweight DTOs for responses

---

**Prerequisites:** Familiarity with Spring Boot, JPA, and SpringFlow basics.

Start with [Architecture](architecture.md) to understand the foundations.
