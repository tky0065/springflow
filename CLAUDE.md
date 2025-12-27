# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SpringFlow is a Spring Boot library that automatically generates complete REST APIs from JPA entities using annotations. The goal is to reduce 70-90% of boilerplate code by generating repositories, services, controllers, DTOs, and documentation from a single `@AutoApi` annotation on entities.

**Current Version:** 0.4.0 (Released December 27, 2025)
**Target:** Spring Boot 4.0.1, Java 25 (backwards compatible to Java 17), Kotlin 2.2.0
**Status:** Production-ready with Phases 1-3 complete, including GraphQL support

## Build and Test Commands

### Building the Project

```bash
# Build all modules
./mvnw clean install

# Build without tests
./mvnw clean install -DskipTests

# Build specific module
cd springflow-core && ../mvnw clean install
```

### Running Tests

```bash
# Run all tests (236+ tests)
./mvnw test

# Run tests with coverage
./mvnw test jacoco:report

# Skip JaCoCo to avoid instrumentation issues
./mvnw test -Djacoco.skip=true

# Run specific test class
./mvnw test -Dtest=EntityScannerTest

# Run specific test method
./mvnw test -Dtest=MetadataResolverTest#testResolveEntityMetadata

# Run tests in specific module
cd springflow-core && ../mvnw test
```

### Running the Demo Application

```bash
cd springflow-demo
../mvnw spring-boot:run
```

The demo runs on `http://localhost:8080` with:
- Swagger UI at `/swagger-ui.html`
- GraphiQL at `/graphiql` (if GraphQL enabled)

### Code Quality

```bash
# Generate coverage report (target/site/jacoco/index.html)
./mvnw clean test jacoco:report

# Run with dev profile (default)
./mvnw spring-boot:run

# Run with prod profile
./mvnw spring-boot:run -Pprod
```

### Deployment

```bash
# Deploy to Maven Central (requires GPG setup)
./mvnw clean deploy -P release -DskipTests -Djacoco.skip=true
```

## Architecture

### Module Structure

The project uses a Maven multi-module structure with clear separation of concerns:

```
springflow/
├── springflow-annotations/   # Module 1: Core annotations (zero dependencies)
├── springflow-core/          # Module 2: Implementation engine (scanning, metadata, generation)
├── springflow-graphql/       # Module 3: GraphQL support (opt-in, NEW in v0.3.0)
├── springflow-starter/       # Module 4: Spring Boot auto-configuration
└── springflow-demo/          # Module 5: Demo/testing application
```

**Dependency Flow:** annotations ← core ← {starter, graphql} ← demo

### Code Generation Strategy

SpringFlow uses **runtime annotation processing** via Spring's bean definition registry, NOT compile-time annotation processing (APT). This approach was chosen for flexibility and Spring integration.

**Key Components:**

1. **EntityScanner** (`springflow-core/src/main/java/io/springflow/core/scanner/EntityScanner.java`)
   - Scans classpath for `@Entity + @AutoApi` annotations
   - Uses `ClassPathScanningCandidateComponentProvider`
   - Implements caching with `ConcurrentHashMap` (default max 100 entries, FIFO eviction)
   - Tracks cache statistics (hits/misses/hit rate)

2. **MetadataResolver** (`springflow-core/src/main/java/io/springflow/core/metadata/MetadataResolver.java`)
   - Introspects entities using reflection
   - Walks inheritance hierarchy (includes `@MappedSuperclass` fields)
   - Extracts: ID type, validation annotations (JSR-380), JPA relations, filterable config, nullable status
   - Returns immutable `EntityMetadata` records

3. **RepositoryGenerator** (`springflow-core/src/main/java/io/springflow/core/repository/RepositoryGenerator.java`)
   - Creates `SimpleJpaRepository` beans dynamically with `JpaSpecificationExecutor`
   - Uses `SpringFlowRepositoryFactoryBean` pattern with `@PersistenceContext` injection
   - Registers beans as "{entityName}Repository" (e.g., "productRepository")

4. **ServiceGenerator** (`springflow-core/src/main/java/io/springflow/core/service/ServiceGenerator.java`)
   - Creates `GenericCrudService` beans dynamically
   - Provides transaction management, validation, lifecycle hooks
   - Uses `SpringFlowServiceFactoryBean` pattern
   - Registers beans as "{entityName}Service"

5. **ControllerGenerator** (`springflow-core/src/main/java/io/springflow/core/controller/ControllerGenerator.java`)
   - Creates `GenericCrudController` beans dynamically
   - Generates CRUD endpoints: GET (list/single), POST, PUT, PATCH, DELETE
   - Uses `SpringFlowControllerFactoryBean` pattern
   - Registers beans as "{entityName}Controller"

6. **AutoApiRepositoryRegistrar** (`springflow-core/src/main/java/io/springflow/core/repository/AutoApiRepositoryRegistrar.java`)
   - Implements `BeanDefinitionRegistryPostProcessor`
   - Orchestrates: scan → resolve metadata → generate beans (repository, service, controller)
   - Discovers packages via `AutoConfigurationPackages.get()`
   - Runs AFTER Spring loads bean definitions but BEFORE instantiation
   - Detects custom user components and skips generation to avoid conflicts

**Generation Pipeline:**

```
Entity with @AutoApi
      ↓
EntityScanner discovers class (cached)
      ↓
MetadataResolver extracts metadata (reflection)
      ↓
RepositoryGenerator creates JpaRepository BeanDefinition
      ↓
ServiceGenerator creates GenericCrudService BeanDefinition
      ↓
ControllerGenerator creates GenericCrudController BeanDefinition
      ↓
Spring BeanDefinitionRegistry registers beans
      ↓
RequestMappingRegistrar registers request mappings
```

### Metadata Model

Uses immutable Java records for metadata:

- `EntityMetadata` - Complete entity information (class, ID type, name, table, AutoApi config, fields)
- `FieldMetadata` - Field-level details (name, type, nullable, hidden, read-only, ID flag, validations, filterable config, relations)
- `RelationMetadata` - JPA relationship configuration (type, fetch, cascade, target entity, mappedBy)

### Key Design Patterns

- **Factory Pattern**: `SpringFlowRepositoryFactoryBean`, `SpringFlowServiceFactoryBean`, `SpringFlowControllerFactoryBean`
- **Registry Pattern**: `BeanDefinitionRegistry` for dynamic bean registration
- **Strategy Pattern**: Configurable cache size, filterable types, security levels, DTO mapping depth
- **Metadata Pattern**: Record-based immutable metadata classes
- **Template Method**: `GenericCrudService` with lifecycle hooks (beforeCreate, afterCreate, beforeUpdate, etc.)
- **Specification Pattern**: JPA Specifications for dynamic filtering
- **Builder Pattern**: `DtoMappingConfig` for configuration

## Implementation Status

| Component | Status | Location |
|-----------|--------|----------|
| Annotations | ✅ Complete | springflow-annotations/src/main/java/io/springflow/annotations/ |
| Entity Scanner | ✅ Complete | core/scanner/EntityScanner.java |
| Metadata Resolver | ✅ Complete | core/metadata/MetadataResolver.java |
| Repository Generation | ✅ Complete | core/repository/RepositoryGenerator.java |
| Service Generation | ✅ Complete | core/service/ServiceGenerator.java + GenericCrudService.java |
| Controller Generation | ✅ Complete | core/controller/ControllerGenerator.java + GenericCrudController.java |
| DTO Mapping | ✅ Complete | core/mapper/EntityDtoMapper.java + DtoMapperFactory.java |
| Dynamic Filters | ✅ Complete | core/filter/FilterResolver.java |
| Security Integration | ✅ Complete | core/security/SecurityUtils.java + SecurityExpressionBuilder.java |
| Validation | ✅ Complete | core/validation/EntityValidator.java + ValidationGroups.java |
| Soft Delete | ✅ Complete | GenericCrudService with @SoftDelete support |
| Audit Trail | ⚠️ Partial | Annotation defined, metadata ready, field population pending |
| GraphQL Support | ✅ Complete | springflow-graphql/ module |
| OpenAPI/Swagger | ✅ Complete | Automatic via SpringDoc |

**Current Phase:** All core features complete, Phase 3 partially implemented

## Core Annotations

### Primary Annotation

**`@AutoApi`** - Activates automatic API generation for an entity
- Parameters: `path`, `expose`, `security`, `pagination`, `sorting`, `description`, `tags`
- Retention: `RUNTIME` (accessed via reflection)
- Location: `springflow-annotations/src/main/java/io/springflow/annotations/AutoApi.java`

Example:
```java
@Entity
@AutoApi(
    path = "/products",
    description = "Product management API",
    expose = Expose.ALL,
    security = @Security(
        enabled = true,
        level = SecurityLevel.AUTHENTICATED,
        roles = {"USER", "ADMIN"}
    )
)
public class Product { ... }
```

### Field-Level Annotations

**`@Filterable`** - Enables dynamic query filtering
- Types: EQUALS, LIKE, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL, RANGE, IN, NOT_IN, IS_NULL, BETWEEN
- Supports custom parameter names and case sensitivity
- Integrates with JPA Specifications

Example:
```java
@Filterable(
    types = {FilterType.EQUALS, FilterType.LIKE, FilterType.RANGE},
    parameterName = "productName",
    caseSensitive = false
)
private String name;
```

Query examples:
```
GET /api/products?name=Phone                    // EQUALS
GET /api/products?name_like=Phone               // LIKE %Phone%
GET /api/products?price_gte=100&price_lte=500   // RANGE
GET /api/products?category_in=Electronics,Books // IN
```

**`@Hidden`** - Excludes field from generated DTOs (marker annotation)
- Used for sensitive fields (passwords, internal codes)
- Field is never exposed in REST API responses or GraphQL

**`@ReadOnly`** - Field in output DTOs only, not input DTOs
- Field visible in GET responses
- Ignored in POST/PUT/PATCH requests
- Protected from modification via PATCH (v0.3.1+)

**`@SoftDelete`** - Enables logical deletion
- Configurable `deletedField` and `deletedAtField`
- Adds methods: `findAll(includeDeleted)`, `findDeletedOnly()`, `restore(id)`
- Works seamlessly with filtering

**`@Auditable`** - Audit trail support (⚠️ Partial implementation)
- Configurable fields: `createdAtField`, `updatedAtField`, `createdByField`, `updatedByField`
- Optional versioning for optimistic locking
- Metadata extraction complete, field population pending

### Supporting Enums

- `Expose`: ALL, READ_ONLY, CREATE_UPDATE, CUSTOM
- `SecurityLevel`: PUBLIC, AUTHENTICATED, ROLE_BASED
- `FilterType`: 12 filter operators (EQUALS, LIKE, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL, RANGE, IN, NOT_IN, IS_NULL, BETWEEN, IS_NOT_NULL)

## Core Components Reference

### springflow-core Module Structure

```
core/
├── config/
│   ├── PageableProperties.java          # Pagination configuration
│   └── SpringFlowWebConfiguration.java  # Web layer config
├── controller/
│   ├── GenericCrudController.java       # Abstract CRUD controller
│   ├── ControllerGenerator.java         # Dynamic controller generation
│   ├── GlobalExceptionHandler.java      # Exception handling
│   ├── ErrorResponse.java               # Error DTOs
│   └── support/
│       ├── SpringFlowControllerFactoryBean.java
│       └── RequestMappingRegistrar.java # Request mapping registration
├── mapper/
│   ├── EntityDtoMapper.java             # Bidirectional entity-DTO conversion
│   ├── DtoMapper.java                   # Generic mapper interface
│   ├── DtoMapperFactory.java            # Mapper caching & lifecycle
│   ├── DtoMappingConfig.java            # SHALLOW/DEFAULT/DEEP configuration
│   └── MappingContext.java              # Cycle detection for bidirectional relations
├── metadata/
│   ├── EntityMetadata.java              # Entity metadata (record)
│   ├── FieldMetadata.java               # Field metadata (record)
│   ├── RelationMetadata.java            # Relation metadata (record)
│   └── MetadataResolver.java            # Reflection-based extraction
├── repository/
│   ├── RepositoryGenerator.java         # Dynamic repository generation
│   ├── AutoApiRepositoryRegistrar.java  # Main orchestrator
│   └── support/
│       └── SpringFlowRepositoryFactoryBean.java
├── scanner/
│   ├── EntityScanner.java               # Classpath scanning
│   └── ScanException.java
├── service/
│   ├── GenericCrudService.java          # Abstract CRUD service
│   ├── ServiceGenerator.java            # Dynamic service generation
│   └── support/
│       └── SpringFlowServiceFactoryBean.java
├── filter/
│   └── FilterResolver.java              # Query parameter → JPA Specification
├── security/
│   ├── SecurityUtils.java               # Spring Security context utilities
│   ├── SecurityExpressionBuilder.java   # @PreAuthorize SpEL generation
│   └── SpringFlowAuditorAware.java      # Auditing support
├── validation/
│   ├── EntityValidator.java             # JSR-380 validation with groups
│   └── ValidationGroups.java            # Create/Update marker interfaces
└── exception/
    ├── EntityNotFoundException.java
    ├── ValidationException.java
    └── DuplicateEntityException.java
```

### springflow-graphql Module Structure (NEW in v0.3.0)

```
graphql/
├── config/
│   ├── SpringFlowGraphQLAutoConfiguration.java
│   ├── SpringFlowGraphQLProperties.java
│   ├── GraphQLControllerRegistrar.java
│   └── DataLoaderRegistrar.java
├── controller/
│   ├── GenericGraphQLController.java
│   └── GraphQLControllerGenerator.java
├── schema/
│   ├── GraphQLSchemaGenerator.java
│   └── GraphQLTypeMapper.java
├── filter/
│   ├── GraphQLFilter.java
│   └── GraphQLFilterConverter.java
├── loader/
│   └── EntityBatchLoader.java
└── support/
    └── SpringFlowGraphQLControllerFactoryBean.java
```

**GraphQL Features:**
- Automatic schema generation from entity metadata
- Queries: `{entity}s(page, size, filters)` and `{entity}(id)`
- Mutations: `create{Entity}`, `update{Entity}`, `delete{Entity}`
- DataLoader for N+1 prevention (50x query reduction: 101 queries → 2 queries)
- Full filter integration (all 10 FilterType operations)
- GraphiQL UI at `/graphiql`
- Type mappings: Java primitives/collections → GraphQL types

Configuration:
```yaml
springflow:
  graphql:
    enabled: true
    schema-location: src/main/resources/graphql
    graphiql-enabled: true
    introspection-enabled: true

spring:
  graphql:
    graphiql:
      enabled: true
      path: /graphiql
    path: /graphql
```

## Important Implementation Details

### Annotation Processing Configuration

The `springflow-core` module **disables annotation processors** to avoid conflicts:

```xml
<compilerArgs>
    <arg>-proc:none</arg>
</compilerArgs>
```

This is because the framework uses runtime processing, not compile-time APT.

### Lombok and MapStruct Integration

Both annotation processors are configured in the parent POM:

```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </path>
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
    </path>
</annotationProcessorPaths>
```

**Order matters**: Lombok runs first, then MapStruct (to see Lombok-generated getters/setters).

### JaCoCo Configuration (v0.3.2 Fix)

JaCoCo can cause instrumentation issues with Hibernate proxies and JDK classes. The demo module disables JaCoCo:

```xml
<!-- springflow-demo/pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <skip>true</skip>
    </configuration>
</plugin>
```

To run tests without JaCoCo:
```bash
./mvnw test -Djacoco.skip=true
```

### EntityValidator Bean Configuration (v0.3.2 Fix)

The `EntityValidator` bean is automatically configured in `SpringFlowAutoConfiguration`:

```java
@Bean
@ConditionalOnProperty(prefix = "springflow", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(Validator.class)
public EntityValidator entityValidator(Validator validator) {
    return new EntityValidator(validator);
}
```

No manual configuration required.

### Test Entity Conventions

Test entities in `springflow-core/src/test/java/io/springflow/core/test/entities/` demonstrate:
- Inheritance with `@MappedSuperclass`
- Composite keys with `@EmbeddedId`
- All validation annotations (NotNull, NotBlank, Size, Min, Max, Email, Pattern)
- JPA relations (OneToMany, ManyToOne, ManyToMany, OneToOne)
- Hidden and read-only fields
- Filterable configurations
- Soft delete and audit trail

### Metadata Resolution Specifics

**ID Type Resolution:**
- Supports `@Id` and `@EmbeddedId`
- ID fields are always marked `readOnly: true`

**Inheritance Handling:**
- Walks class hierarchy until `Object.class`
- Processes classes with `@Entity` or `@MappedSuperclass`
- Excludes static, transient, and `@Transient` fields

**Nullable Detection (priority order):**
1. `@Column(nullable=false)` → not nullable
2. `@NotNull` or `@NotBlank` → not nullable
3. Primitive types → not nullable
4. Otherwise → nullable

**Relation Metadata Extraction:**
- For collections (OneToMany, ManyToMany): Uses generic type if `targetEntity == void.class`
- Captures: FetchType, CascadeType[], mappedBy attribute
- Automatic N+1 prevention with smart fetch joins

## Development Guidelines

### Commit Convention

Follow conventional commits as specified in CONTRIBUTING.md:
- `feat(module): add new feature`
- `fix(module): fix bug`
- `test: add tests`
- `refactor: refactor code`
- `docs: update documentation`
- `chore: update dependencies`

### Code Quality Standards

- Java 17+ features encouraged
- Lombok for boilerplate reduction (use `@Data`, `@Builder`, `@Slf4j`)
- SLF4J for logging (via Lombok's `@Slf4j`)
- Javadoc required for all public APIs
- Test coverage target: >80%
- No SonarQube critical issues

### Testing Strategy

- **Unit tests**: Test components in isolation (EntityScanner, MetadataResolver)
- **Integration tests**: Test component interactions (RepositoryGenerator with EntityManager)
- Use `@SpringBootTest` for full application context tests
- Use H2 in-memory database for testing

**Test Statistics (v0.3.2):**
- 37 test files
- 236+ tests total
- Coverage breakdown:
  - springflow-annotations: 26 tests
  - springflow-core: 169 tests (1 skipped)
  - springflow-starter: 18 tests
  - springflow-demo: 23 tests (11 skipped for GraphQL)
- All tests passing ✅

### Logging Guidelines

Log levels used consistently:
- `log.debug()` - Method entry, cache hits, entity discovery details
- `log.info()` - Scan completion, bean registration, startup events
- `log.warn()` - Cache size limits, missing packages, non-critical failures
- `log.error()` - Scan failures, critical errors

## Roadmap Context

### Phase 1 (MVP) - ✅ COMPLETE (v0.1.x)
- ✅ Annotations and core generation
- ✅ CRUD endpoints (GET, POST, PUT, DELETE)
- ✅ Pagination and sorting
- ✅ Validation (JSR-380)
- ✅ Swagger/OpenAPI documentation
- ✅ DTO generation with @Hidden/@ReadOnly
- ✅ Repository, Service, Controller generation

### Phase 2 (Advanced Features) - ✅ 95% COMPLETE (v0.2.0-0.3.1)
- ✅ Dynamic filtering with `JpaSpecificationExecutor` (12 filter operations)
- ✅ Security and role-based authorization (Spring Security integration)
- ✅ Soft delete support (@SoftDelete)
- ⚠️ Audit trail (metadata ready, field population pending)
- ✅ Advanced DTO mapping (depth control, cycle detection)
- ✅ Custom component detection (services, controllers, repositories)
- ✅ PATCH endpoint with @Hidden/@ReadOnly protection (v0.3.1)
- ✅ Validation groups (Create/Update context-aware) (v0.3.1)

### Phase 3 (Extended Ecosystem) - 🚧 PARTIAL (v0.3.0+)
- ✅ GraphQL support (complete implementation)
- 🚧 Admin UI (React/Vue) - Planned
- 🚧 CLI tool - Planned
- 🚧 IDE plugins - Planned
- 🚧 Multi-DB support (MongoDB, etc.) - Planned
- 🚧 Monitoring & Metrics with Actuator - Planned

### Recent Releases

**v0.3.2 (2025-12-27)** - Critical Bugfixes
- Fixed `NoSuchBeanDefinitionException` for EntityValidator bean
- Upgraded JaCoCo from 0.8.11 to 0.8.12 for Hibernate compatibility
- Fixed invoice number collision with UUID suffix
- All 236+ tests passing

**v0.3.1 (2025-12-27)** - Phase 2 Enhancements
- PATCH endpoint security (@Hidden/@ReadOnly field protection)
- Validation groups (JSR-380 Create/Update)
- Advanced DTO mapping (configurable depth, cycle detection)

**v0.3.0 (2025-12-26)** - GraphQL Support
- Complete GraphQL module with schema generation
- DataLoader for N+1 prevention (50x improvement)
- GraphQL filtering integration

## Common Development Tasks

### Adding a New Annotation

1. Create annotation in `springflow-annotations/src/main/java/io/springflow/annotations/`
2. Add retention policy: `@Retention(RetentionPolicy.RUNTIME)`
3. Update `MetadataResolver` to extract annotation metadata
4. Add to `FieldMetadata` or `EntityMetadata` record
5. Write tests in `springflow-annotations/src/test/java/`

### Extending Metadata Extraction

1. Add new field to `EntityMetadata`, `FieldMetadata`, or `RelationMetadata` record
2. Update `MetadataResolver.buildFieldMetadata()` or `resolve()` method
3. Use reflection to extract data from entity class/field
4. Add corresponding tests in `MetadataResolverTest`

### Generating a New Bean Type

Follow the factory bean pattern:
1. Create generator class (e.g., `ServiceGenerator`)
2. Use `BeanDefinitionBuilder.genericBeanDefinition()`
3. Add property values for entity class, ID type, dependencies
4. Create factory bean (e.g., `SpringFlowServiceFactoryBean`)
5. Return `BeanDefinition` for registration
6. Integrate into `AutoApiRepositoryRegistrar` orchestration
7. Add tests following existing test patterns

## Configuration Reference

### SpringFlow Properties

```yaml
springflow:
  enabled: true                    # Enable/disable SpringFlow
  base-path: /api                  # Base path for all endpoints
  pagination:
    default-page-size: 20          # Default page size
    max-page-size: 100             # Maximum allowed page size
  swagger:
    enabled: true                  # Enable Swagger UI
    title: "SpringFlow API"
    description: "Auto-generated REST API"
    version: "1.0.0"
  graphql:
    enabled: false                 # Enable GraphQL module
    schema-location: classpath:graphql
    graphiql-enabled: true
    introspection-enabled: true
```

### Security Configuration Example

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/**").authenticated()
            )
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        return http.build();
    }
}
```

## Project Configuration Files

- Parent POM: `/pom.xml` (versions, dependency management, plugins, GPG signing, Maven Central)
- Module POMs: `{module}/pom.xml` (specific dependencies)
- Application config (demo): `springflow-demo/src/main/resources/application.yml`
- Logging: Uses Spring Boot defaults (Logback)
- Git: `.gitignore` excludes target/, .idea/, *.iml
- MkDocs: `/mkdocs.yml` for documentation site

## Troubleshooting

### JaCoCo Instrumentation Errors

If you see `IllegalClassFormatException` related to JaCoCo instrumenting JDK or Hibernate classes:

```bash
# Skip JaCoCo during tests
./mvnw test -Djacoco.skip=true

# Or deploy without JaCoCo
./mvnw clean deploy -P release -DskipTests -Djacoco.skip=true
```

### NoSuchBeanDefinitionException for entityValidator

Fixed in v0.3.2. The `EntityValidator` bean is now automatically configured. If using an older version, manually add:

```java
@Bean
public EntityValidator entityValidator(Validator validator) {
    return new EntityValidator(validator);
}
```

### GraphQL Not Working

Ensure GraphQL module is included and enabled:

```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-graphql</artifactId>
    <version>0.3.2</version>
</dependency>
```

```yaml
springflow:
  graphql:
    enabled: true
```

## Documentation References

- **README.md**: Complete user documentation (French) with examples, configuration, troubleshooting
- **docs/**: Comprehensive MkDocs documentation (English)
  - Getting Started guides
  - API Reference
  - Advanced topics
  - Development guides
- **CHANGELOG.md**: Version history and migration guides
- **CONTRIBUTING.md**: Development workflow, coding standards, PR process
- **RELEASE_NOTES_{version}.md**: Detailed release notes for each version

## Git Workflow

Recent commits (as of v0.3.2):
```
98a5dc9 docs: fix broken link in GraphQL guide
5a9b521 docs: update all version references from 0.2.0 to 0.3.2
49c453b docs: update version to 0.3.2 in all documentation
f83310a fix(core): critical bugfixes for v0.3.2 release
```

Main branch: `main`
Release tags: `v0.3.2`, `v0.3.1`, `v0.3.0`, etc.
GitHub Actions: Build and Test, Deploy Documentation

## Summary Statistics

| Aspect | Count |
|--------|-------|
| Modules | 5 |
| Java Source Files (main) | 48+ |
| Test Files | 37 |
| Test Cases | 236+ |
| Annotations Defined | 7 |
| Filter Operations | 12 |
| Core Packages | 14+ |
| Lines of Code | 10,000+ |
| Code Coverage | 80%+ |

SpringFlow v0.3.2 is a **production-ready** Spring Boot library that significantly reduces boilerplate code for REST and GraphQL API generation from JPA entities.
