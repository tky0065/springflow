# Roadmap

## Overview

SpringFlow is an actively developed open-source library. This page outlines what has been delivered and what is planned for future releases.

---

## What's Available Now

### Core Features (stable)
- `@AutoApi` annotation for automatic REST API generation
- Full CRUD endpoints (GET list, GET by id, POST, PUT, PATCH, DELETE)
- Pagination and sorting
- Automatic DTO generation (`@Hidden`, `@ReadOnly`)
- JSR-380 validation with create/update groups
- OpenAPI/Swagger documentation (via SpringDoc)
- Dynamic filtering with `@Filterable` (12 filter operations)
- Spring Security integration with role-based access control
- Soft delete with `@SoftDelete` and restore support
- Advanced DTO mapping (configurable depth, cycle detection)
- Custom component detection (bring your own repository, service, or controller)
- GraphQL support (schema generation, mutations, DataLoader for N+1 prevention)
- Java 17+ and Kotlin support

---

## Upcoming Features

### Near-term
- **Audit trail** — automatic population of `createdAt`, `updatedAt`, `createdBy`, `updatedBy` fields (metadata extraction is already done; field population is pending)
- **Monitoring & metrics** — Spring Boot Actuator integration

### Longer-term
- **Admin UI** — auto-generated management interface (React/Vue)
- **CLI tool** — project scaffolding helper
- **Multi-database support** — MongoDB and other non-relational stores
- **IDE plugins** — IntelliJ IDEA and VS Code extensions

---

## Release History

See [Changelog](changelog.md) for the full list of releases and what changed in each version.

---

## Contributing

Want to help? See [Contributing](../development/contributing.md) to get started. Feature requests and bug reports are welcome on [GitHub Issues](https://github.com/tky0065/springflow/issues).
