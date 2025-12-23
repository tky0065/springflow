# Changelog

All notable changes to SpringFlow will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned for Phase 2 (v0.2.0)
- Dynamic filters with JPA Specifications
- Advanced security (JWT, roles, permissions)
- Soft delete with restoration
- Audit trail (createdBy, updatedBy, createdAt, updatedAt)
- GraphQL support

## [0.1.1] - 2025-12-23

### Fixed
- **Swagger UI paths duplication** - Fixed duplicated paths `/api/api/categories` → `/api/categories`
- **OpenAPI server URL configuration** - Changed server URL from `/api` to `/` to prevent path concatenation issues
- **"No operations defined in spec!" error** - Resolved issue where dynamically generated endpoints weren't appearing in Swagger UI
- **Metadata retrieval from factory beans** - Fixed SpringFlowOpenApiCustomizer to retrieve EntityMetadata from factory beans using reflection

### Changed
- **OpenAPI customizer integration** - Switched from automatic bean detection to direct integration with GroupedOpenApi.builder()
- **Controller path generation** - Modified ControllerGenerator to return entity-specific paths without global base path (e.g., `/products` instead of `/api/products`)
- Improved OpenAPI documentation with proper operation IDs, summaries, and descriptions

### Technical Details
- OpenApiConfiguration.java:98 - Server URL changed from `properties.getBasePath()` to `/`
- SpringFlowOpenApiCustomizer.java - Added `getMetadataFromFactory()` method to access factory bean metadata
- ControllerGenerator.java:85 - Base path generation now returns `"/" + plural` instead of `"/api/" + plural`

## [0.1.0] - 2025-12-22

### 🎉 Phase 1 MVP - Initial Release

First public release of SpringFlow - a Spring Boot library that automatically generates complete REST APIs from JPA entities with a single annotation.

### Added

#### Core Features
- **@AutoApi annotation** - Main annotation to activate automatic API generation for JPA entities
- **@Hidden annotation** - Exclude sensitive fields from DTOs (both input and output)
- **@ReadOnly annotation** - Mark fields as read-only (visible in GET, ignored in POST/PUT)
- **@Filterable annotation** - Prepare fields for dynamic filtering (implementation in Phase 2)
- **@SoftDelete annotation** - Mark entities for soft delete support (implementation in Phase 2)
- **@Auditable annotation** - Mark entities for audit trail (implementation in Phase 2)

#### Module 1: Project Setup & Architecture
- Multi-module Maven project structure (annotations, core, starter, demo)
- Spring Boot 3.2.1 compatibility
- Java 17+ support
- Kotlin 1.9.22 support
- GitHub Actions CI/CD workflow
- JaCoCo code coverage configuration

#### Module 2: Core Annotations
- `@AutoApi` with configurable path, expose mode, pagination, sorting, description, tags
- `Expose` enum: ALL, CREATE_UPDATE, READ_ONLY, CUSTOM
- `FilterType` enum: 12 filter operators (EQUALS, LIKE, GT, LT, GTE, LTE, RANGE, IN, NOT_IN, IS_NULL, BETWEEN)
- `SecurityLevel` enum: PUBLIC, AUTHENTICATED, ROLE_BASED

#### Module 3: Entity Scanner
- Automatic entity discovery via classpath scanning
- `ClassPathScanningCandidateComponentProvider` integration
- Entity cache with configurable max size (default 100 entries, FIFO eviction)
- Cache statistics tracking (hits, misses, hit rate)
- Support for custom base packages configuration

#### Module 4: Metadata Resolver
- Complete entity introspection using reflection
- ID type resolution (supports @Id and @EmbeddedId)
- Field metadata extraction (name, type, nullable, hidden, read-only, validations)
- JPA relationship metadata (OneToMany, ManyToOne, ManyToMany, OneToOne)
- Inheritance hierarchy walking (includes @MappedSuperclass fields)
- JSR-380 validation annotation extraction
- Nullable detection (Column.nullable, @NotNull, @NotBlank, primitive types)

#### Module 5: Repository Generation
- Dynamic repository creation using Spring Data JPA
- `SimpleJpaRepository` bean generation
- `SpringFlowRepositoryFactoryBean` pattern implementation
- Automatic bean registration in Spring context
- Repository naming convention: "{entityName}Repository"

#### Module 6: Service Generation
- `GenericCrudService` abstract class with full CRUD operations
- Transaction management with @Transactional annotations
- Business logic hooks: beforeCreate, afterCreate, beforeUpdate, afterUpdate, beforeDelete, afterDelete
- Exception handling: EntityNotFoundException, DuplicateEntityException, ValidationException
- `ServiceGenerator` for dynamic service bean registration
- `SpringFlowServiceFactoryBean` for service instantiation

#### Module 7: Generic CRUD Controller
- `GenericCrudController` with RESTful endpoints:
  - GET / - List all with pagination
  - GET /{id} - Get by ID
  - POST / - Create new entity
  - PUT /{id} - Update existing entity
  - DELETE /{id} - Delete entity
- HTTP status codes: 200 OK, 201 Created, 204 No Content, 404 Not Found, 400 Bad Request
- Location header on POST responses
- Map-based DTOs for input/output
- Proper error handling and validation

#### Module 8: Pagination & Sorting
- Spring Data Pageable integration
- Configurable default page size (default: 20)
- Configurable max page size (default: 100)
- Multi-field sorting support
- Customizable parameter names (page, size, sort)
- 0-indexed or 1-indexed pagination support
- Page response format with totalElements, totalPages, first, last, etc.

#### Module 9: Controller Registration
- `RequestMappingRegistrar` for dynamic controller registration
- Spring MVC RequestMappingHandlerMapping integration
- Global base path configuration (default: /api)
- Entity-specific path combination
- ApplicationListener<ContextRefreshedEvent> pattern for proper initialization timing
- Request mapping metadata attributes (@RestController, @RequestMapping)

#### Module 10: Spring Boot Auto-Configuration
- `SpringFlowAutoConfiguration` with META-INF auto-configuration
- Conditional activation based on EntityManager presence
- `springflow.enabled` property (default: true)
- `SpringFlowProperties` configuration class
- `PageableProperties` bean creation
- `DtoMapperFactory` bean creation
- Zero-configuration activation (no @Enable annotation required)

#### Module 11: OpenAPI/Swagger Integration
- SpringDoc OpenAPI 2.3.0 integration
- Automatic Swagger UI generation
- Operation annotations (@Operation, @ApiResponse, @ApiResponses, @Parameter)
- Customizable API title, description, version, contact info
- Tags support for endpoint grouping
- Swagger UI available at /swagger-ui.html

#### Module 12: DTO Generation
- `DtoMapper` interface for entity-DTO conversions
- Map-based InputDTO and OutputDTO (phase 1 approach)
- `EntityDtoMapper` implementation using reflection
- `DtoMapperFactory` with caching
- Field exclusion based on @Hidden annotation
- Read-only field handling (@ReadOnly excluded from input, included in output)
- Type conversion support (String, Number, Date, etc.)
- Collection mapping (List, Page)
- Null value handling

#### Module 13: Validation
- JSR-380 validation support
- Automatic validation on POST/PUT operations
- Supported annotations: @NotNull, @NotBlank, @NotEmpty, @Size, @Min, @Max, @Email, @Pattern, @Past, @Future, @PastOrPresent, @FutureOrPresent
- Validation error response format with field-level details
- Global exception handler for validation errors
- HTTP 400 Bad Request on validation failure

#### Module 14: Kotlin Support
- Full compatibility with Kotlin data classes
- Support for nullable types (?)
- Kotlin annotation targets (@field:, @get:)
- Default parameter values
- Extension function compatibility
- Comprehensive KOTLIN.md documentation
- Test coverage with Kotlin entities

#### Module 15: Demo Application & Documentation
- `springflow-demo` module with working examples
- 3 demo entities: Product, Category, User
- Lombok @Data integration
- Sample data via data.sql (7 categories, 10 products, 5 users)
- H2 in-memory database configuration
- Auto-increment sequence reset to avoid conflicts
- Circular reference fixes with @JsonIgnoreProperties
- QUICKSTART.md guide
- Complete README.md with examples

### Technical Details

#### Dependencies
- Spring Boot: 3.2.1
- Spring Data JPA: 3.2.1
- Jakarta Persistence API: 3.1.0
- Hibernate Validator: 8.0.1.Final
- SpringDoc OpenAPI: 2.3.0
- Lombok: 1.18.38
- MapStruct: 1.5.5.Final
- Kotlin: 1.9.22
- JUnit 5: 5.10.1
- H2 Database: 2.2.224

#### Build & Test
- Maven 3.9.6
- Java 17 minimum, tested with Java 25
- 140+ unit and integration tests
- Test coverage: >80%
- GitHub Actions CI/CD

### Fixed

- Primary key conflicts in data.sql by adding sequence reset statements
- Circular reference infinite loops in JSON serialization with @JsonIgnoreProperties
- Lombok compatibility with Java 25 (upgraded to 1.18.38)
- Request mapping registration timing (changed from BeanPostProcessor to ApplicationListener)
- Global base path combination with entity-specific paths

### Security

- @Hidden annotation properly excludes sensitive fields like passwords from API responses
- No hardcoded credentials in source code
- Validation prevents SQL injection via prepared statements
- Secure defaults (authentication disabled by default, users must opt-in)

### Known Limitations (Phase 1 MVP)

- Dynamic filtering not yet implemented (annotation exists, implementation in Phase 2)
- Soft delete not yet implemented (annotation exists, implementation in Phase 2)
- Audit trail not yet implemented (annotation exists, implementation in Phase 2)
- Security integration basic (advanced features in Phase 2)
- DTO generation uses Map-based approach (typed DTOs in Phase 2)
- No GraphQL support yet (Phase 3)

### Documentation

- README.md with quick start guide
- QUICKSTART.md with detailed examples
- KOTLIN.md for Kotlin-specific usage
- CONTRIBUTING.md for contributors
- Comprehensive Javadoc on all public APIs
- Swagger UI for generated APIs

### Migration Guide

This is the first release, no migration needed.

### Contributors

- Initial implementation by SpringFlow team
- Generated with Claude Code (Anthropic)

---

## Version History

- **0.1.0-SNAPSHOT** (2025-12-21) - Phase 1 MVP - Initial Release
- **0.2.0** (Planned) - Phase 2 - Advanced Features
- **1.0.0** (Planned) - Phase 3 - Production Ready

---

For detailed roadmap, see [roadmap.md](roadmap.md)
