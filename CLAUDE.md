# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SpringFlow is a Spring Boot library that automatically generates complete REST APIs from JPA entities using annotations. The goal is to reduce 70-90% of boilerplate code by generating repositories, services, controllers, DTOs, and documentation from a single `@AutoApi` annotation on entities.

**Current Version:** 0.1.0-SNAPSHOT
**Target:** Spring Boot 3.2.1, Java 17+, Kotlin 1.9.22

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
# Run all tests
./mvnw test

# Run tests with coverage
./mvnw test jacoco:report

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

The demo runs on `http://localhost:8080` with Swagger UI at `/swagger-ui.html`.

### Code Quality

```bash
# Generate coverage report (target/site/jacoco/index.html)
./mvnw clean test jacoco:report

# Run with dev profile (default)
./mvnw spring-boot:run

# Run with prod profile
./mvnw spring-boot:run -Pprod
```

## Architecture

### Module Structure

The project uses a Maven multi-module structure with clear separation of concerns:

```
springflow/
├── springflow-annotations/   # Module 1: Core annotations (zero dependencies)
├── springflow-core/          # Module 2: Implementation (scanning, metadata, generation)
├── springflow-starter/       # Module 3: Spring Boot auto-configuration
└── springflow-demo/          # Module 4: Demo/testing application
```

**Dependency Flow:** annotations ← core ← starter ← demo

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
   - Creates `SimpleJpaRepository` beans dynamically
   - Uses `SpringFlowRepositoryFactoryBean` pattern with `@PersistenceContext` injection
   - Registers beans as "{entityName}Repository" (e.g., "productRepository")

4. **AutoApiRepositoryRegistrar** (`springflow-core/src/main/java/io/springflow/core/repository/AutoApiRepositoryRegistrar.java`)
   - Implements `BeanDefinitionRegistryPostProcessor`
   - Orchestrates: scan → resolve metadata → generate beans
   - Discovers packages via `AutoConfigurationPackages.get()`
   - Runs AFTER Spring loads bean definitions but BEFORE instantiation

**Generation Pipeline:**

```
Entity with @AutoApi
      ↓
EntityScanner discovers class (cached)
      ↓
MetadataResolver extracts metadata (reflection)
      ↓
RepositoryGenerator creates BeanDefinition (factory bean pattern)
      ↓
Spring BeanDefinitionRegistry registers bean
```

### Metadata Model

Uses immutable Java records for metadata:

- `EntityMetadata` - Complete entity information (class, ID type, name, table, AutoApi config, fields)
- `FieldMetadata` - Field-level details (name, type, nullable, hidden, read-only, ID flag, validations, filterable config, relations)
- `RelationMetadata` - JPA relationship configuration (type, fetch, cascade, target entity, mappedBy)

### Key Design Patterns

- **Factory Pattern**: `SpringFlowRepositoryFactoryBean` creates repository instances
- **Registry Pattern**: `BeanDefinitionRegistry` for dynamic bean registration
- **Strategy Pattern**: Configurable cache size, filterable types, security levels
- **Metadata Pattern**: Record-based immutable metadata classes
- **Template Method** (implicit): Generic scanning/processing templates reused per entity

## Implementation Status

| Component | Status | Location |
|-----------|--------|----------|
| Annotations | Complete | springflow-annotations/src/main/java/io/springflow/annotations/ |
| Entity Scanner | Complete | core/scanner/EntityScanner.java |
| Metadata Resolver | Complete | core/metadata/MetadataResolver.java |
| Repository Generation | Complete | core/repository/RepositoryGenerator.java |
| Service Generation | Scaffolded | core/service/ |
| Controller Generation | Scaffolded | core/controller/ |
| DTO Mapping | Scaffolded | core/mapper/ |
| Dynamic Filters | Scaffolded | core/filter/ |

**Current Phase:** Module 5 completion (service/controller/DTO generation)

## Core Annotations

### Primary Annotation

**`@AutoApi`** - Activates automatic API generation for an entity
- Parameters: `path`, `expose`, `security`, `pagination`, `sorting`, `description`, `tags`
- Retention: `RUNTIME` (accessed via reflection)
- Location: `springflow-annotations/src/main/java/io/springflow/annotations/AutoApi.java`

### Field-Level Annotations

**`@Filterable`** - Enables dynamic query filtering
- Types: EQUALS, LIKE, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL, RANGE, IN, NOT_IN, IS_NULL, BETWEEN
- Supports custom parameter names and case sensitivity

**`@Hidden`** - Excludes field from generated DTOs (marker annotation)

**`@ReadOnly`** - Field in output DTOs only, not input DTOs

**`@SoftDelete`**, **`@Auditable`** - Phase 2 features

### Supporting Enums

- `Expose`: ALL, READ_ONLY, CREATE_UPDATE, CUSTOM
- `SecurityLevel`: PUBLIC, AUTHENTICATED, ROLE_BASED
- `FilterType`: 12 filter operators

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

### Test Entity Conventions

Test entities in `springflow-core/src/test/java/io/springflow/core/test/entities/` demonstrate:
- Inheritance with `@MappedSuperclass`
- Composite keys with `@EmbeddedId`
- All validation annotations (NotNull, NotBlank, Size, Min, Max, Email, Pattern)
- JPA relations (OneToMany, ManyToOne, ManyToMany, OneToOne)
- Hidden and read-only fields
- Filterable configurations

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

### Logging Guidelines

Log levels used consistently:
- `log.debug()` - Method entry, cache hits, entity discovery details
- `log.info()` - Scan completion, bean registration, startup events
- `log.warn()` - Cache size limits, missing packages, non-critical failures
- `log.error()` - Scan failures, critical errors

## Roadmap Context

**Phase 1 (Current - MVP):**
- Modules 1-5: Annotations, scanning, metadata resolution, repository generation, service/controller/DTO generation
- CRUD endpoints, pagination, sorting, validation, Swagger documentation

**Phase 2 (Advanced Features):**
- Dynamic filtering with `JpaSpecificationExecutor`
- Security and role-based authorization
- Soft delete support
- Audit trail (createdAt, updatedAt, createdBy, updatedBy)

**Phase 3 (Extended Ecosystem):**
- GraphQL support
- Admin UI (React/Vue)
- CLI tool
- IDE plugins

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

### Generating a New Bean Type (Service, Controller)

Follow the `RepositoryGenerator` pattern:
1. Create generator class (e.g., `ServiceGenerator`)
2. Use `BeanDefinitionBuilder.genericBeanDefinition()`
3. Add property values for entity class, ID type, dependencies
4. Return `BeanDefinition` for registration
5. Integrate into `AutoApiRepositoryRegistrar` orchestration
6. Add tests following `RepositoryGeneratorTest` structure

## Project Configuration Files

- Parent POM: `/pom.xml` (versions, dependency management, plugins)
- Module POMs: `{module}/pom.xml` (specific dependencies)
- Application config (demo): `springflow-demo/src/main/resources/application.yml`
- Logging: Uses Spring Boot defaults (Logback)
- Git: `.gitignore` excludes target/, .idea/, *.iml

## Documentation References

- README.md: Complete user documentation (French) with examples, configuration, troubleshooting
- spec.md: Initial design concepts and architectural ideas
- roadmap.md: Three-phase development plan with milestones
- CONTRIBUTING.md: Development workflow, coding standards, PR process
