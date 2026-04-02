# User Guide

Complete reference for SpringFlow features and how to use them.

## :material-book-open-variant: Contents

### Core Features

<div class="grid cards" markdown>

-   :material-label: **[Annotations](annotations.md)**

    ---

    Complete reference for all SpringFlow annotations

-   :material-cog: **[Configuration](configuration.md)**

    ---

    Configuration options via YAML and annotations

-   :material-sync: **[DTO Mapping](dto-mapping.md)**

    ---

    Automatic entity-to-DTO mapping

</div>

### Data Management

<div class="grid cards" markdown>

-   :material-magnify: **[Filtering](filtering.md)**

    ---

    Dynamic query filtering with @Filterable

-   :material-file-document: **[Pagination & Sorting](pagination.md)**

    ---

    Paginate and sort results

-   :material-check-circle: **[Validation](validation.md)**

    ---

    Data validation with JSR-380

</div>

### Advanced Features

<div class="grid cards" markdown>

-   :material-lock: **[Security](security.md)**

    ---

    Spring Security integration and access control

-   :material-delete: **[Soft Delete](soft-delete.md)**

    ---

    Logical deletion with restore support

-   :material-clipboard-text: **[Auditing](auditing.md)**

    ---

    Automatic audit trail (createdAt, updatedAt, etc.)

-   :material-language-kotlin: **[Kotlin Support](kotlin.md)**

    ---

    Using SpringFlow with Kotlin and data classes

</div>

## :material-target: Where to start?

If you are new to SpringFlow, we recommend reading in this order:

1. **[Annotations](annotations.md)** — understand `@AutoApi` and field-level annotations
2. **[Configuration](configuration.md)** — customize the behavior
3. **[DTO Mapping](dto-mapping.md)** — understand automatic mapping
4. **[Pagination](pagination.md)** — handle large collections

Then explore the advanced features based on your needs.

## :material-magnify: Quick Reference

**I want to...**

- Create a simple API → [Annotations](annotations.md)
- Filter results → [Filtering](filtering.md)
- Paginate results → [Pagination](pagination.md)
- Validate data → [Validation](validation.md)
- Secure endpoints → [Security](security.md)
- Track changes → [Auditing](auditing.md)
- Support restore → [Soft Delete](soft-delete.md)
- Use Kotlin → [Kotlin Support](kotlin.md)

---

Need help? Check the [advanced topics](../advanced/architecture.md) or the [API reference](../api/annotations.md).
