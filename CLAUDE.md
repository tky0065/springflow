# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SpringFlow is a Spring Boot library that automatically generates complete REST APIs from JPA entities using annotations. A single `@AutoApi` annotation on an entity generates a full CRUD stack (repository, service, controller, DTOs, OpenAPI docs) at **runtime** — no code generation, no APT.

**Current version:** 0.5.1  
**Stack:** Spring Boot 4.0.1, Java 25 (compatible Java 17+), Kotlin 2.2.0  
**Remaining work:** see `tasks.md`

---

## Build & Test Commands

```bash
# Build all modules
./mvnw clean install

# Build without tests
./mvnw clean install -DskipTests

# Run all tests
./mvnw test

# Run tests, skip JaCoCo (avoids Hibernate proxy instrumentation errors)
./mvnw test -Djacoco.skip=true

# Run a specific test class
./mvnw test -Dtest=EntityScannerTest

# Run a specific test method
./mvnw test -Dtest=MetadataResolverTest#testResolveEntityMetadata

# Run tests in one module only
cd springflow-core && ../mvnw test

# Run the demo app (http://localhost:8080)
cd springflow-demo && ../mvnw spring-boot:run

# Generate JaCoCo coverage report → target/site/jacoco/index.html
./mvnw clean test jacoco:report

# Publish to Maven Central (requires GPG)
./mvnw clean deploy -P release -DskipTests -Djacoco.skip=true
```

---

## Module Structure

```
springflow/
├── springflow-annotations/   # Core annotations — zero external dependencies
├── springflow-core/          # Implementation engine (scanning, metadata, generation)
├── springflow-graphql/       # Optional GraphQL module (opt-in via dependency)
├── springflow-starter/       # Spring Boot auto-configuration
└── springflow-demo/          # Integration / demo application (H2, Swagger, GraphiQL)
```

**Dependency chain:** `annotations ← core ← {starter, graphql} ← demo`

`springflow-core` compiles with `-proc:none` to disable annotation processors — SpringFlow is runtime-only.

---

## Architecture: How Bean Generation Works

SpringFlow generates beans **after** Spring loads bean definitions but **before** instantiation, via `AutoApiRepositoryRegistrar` (`BeanDefinitionRegistryPostProcessor`):

```
Entity with @AutoApi
  → EntityScanner          discovers class (ConcurrentHashMap cache, FIFO eviction at 100)
  → MetadataResolver       extracts metadata via reflection (ID, fields, relations, validations)
  → RepositoryGenerator    registers JpaRepository BeanDefinition
  → ServiceGenerator       registers GenericCrudService BeanDefinition
  → ControllerGenerator    registers GenericCrudController BeanDefinition
  → RequestMappingRegistrar registers HTTP request mappings
```

`AutoApiRepositoryRegistrar` also detects existing user-defined beans for an entity (custom repo/service/controller) and skips generation to avoid conflicts.

**Factory bean pattern:** each generator produces a `*FactoryBean` (`SpringFlowRepositoryFactoryBean`, `SpringFlowServiceFactoryBean`, `SpringFlowControllerFactoryBean`) that holds `entityClass` and `idType` as property values, allowing Spring to manage lifecycle normally.

---

## Key Classes by Package

### `springflow-annotations`
| Class | Purpose |
|-------|---------|
| `@AutoApi` | Activates generation; params: `path`, `expose`, `security`, `pagination`, `sorting` |
| `@Filterable` | Enables dynamic filtering on a field; `types` (FilterType[]), `parameterName`, `caseSensitive` |
| `@Hidden` | Excludes field from all DTOs (input + output) |
| `@ReadOnly` | Field visible in output DTOs, rejected in input DTOs and PATCH |
| `@SoftDelete` | Enables logical deletion; `deletedField`, `deletedAtField` |
| `@Auditable` | Marks entity for audit trail; field population **not yet implemented** |
| `@SecuredApi` | Fine-grained per-operation security (per HTTP method level) |
| `@Summary` | Marks fields included in summary/projection responses |
| `Expose` | ALL, READ_ONLY, CREATE_UPDATE, CUSTOM |
| `FilterType` | 12 operators: EQUALS, LIKE, GT, LT, GTE, LTE, RANGE, IN, NOT_IN, IS_NULL, IS_NOT_NULL, BETWEEN |
| `SecurityLevel` | PUBLIC, AUTHENTICATED, ROLE_BASED |

### `springflow-core/scanner`
- `EntityScanner` — classpath scanning with `ClassPathScanningCandidateComponentProvider`; caches results

### `springflow-core/metadata`
- `MetadataResolver` — reflection-based extraction; walks `@MappedSuperclass` hierarchy; extracts ID, fields, JSR-380 annotations, JPA relations
- `EntityMetadata`, `FieldMetadata`, `RelationMetadata` — immutable records

### `springflow-core/repository`
- `AutoApiRepositoryRegistrar` — main orchestrator (`BeanDefinitionRegistryPostProcessor`)
- `RepositoryGenerator` — produces `SimpleJpaRepository` + `JpaSpecificationExecutor` BeanDefinition
- `SpecificationBuilder` — utility for building JPA Specifications from criteria

### `springflow-core/service`
- `GenericCrudService<T,ID>` — abstract base with full CRUD + lifecycle hooks (`beforeCreate`, `afterCreate`, `beforeUpdate`, `afterUpdate`, `beforeDelete`, `afterDelete`)
- `ServiceGenerator` — wires service to its repository bean

### `springflow-core/controller`
- `GenericCrudController<T,ID>` — abstract REST controller; applies validation groups (Create on POST, Update on PUT/PATCH); protects `@Hidden`/`@ReadOnly` fields on PATCH
- `ControllerGenerator` + `RequestMappingRegistrar` — register controller beans and HTTP mappings

### `springflow-core/filter`
- `FilterResolver` — parses query parameters → JPA `Specification`; reads `@Filterable` config per field
- `GenericSpecification<T>` — `Specification` implementation using `SearchCriteria`
- `SpecificationBuilder<T>` — fluent builder for combining specifications

### `springflow-core/mapper`
- `EntityDtoMapper` — reflection-based entity ↔ `Map<String, Object>` conversion
- `DtoMapperFactory` — creates and caches mappers per entity class
- `DtoMappingConfig` — depth control: SHALLOW (0), DEFAULT (1), DEEP (3)
- `MappingContext` — cycle detection for bidirectional relations

### `springflow-core/security`
- `SecurityExpressionBuilder` — generates `@PreAuthorize` SpEL from `@Security` annotation config
- `SecurityUtils` — `SecurityContext` access helpers
- `SpringFlowAuditorAware` — `AuditorAware<String>` bean for audit support

### `springflow-core/validation`
- `EntityValidator` — JSR-380 validation with group support (Create/Update)
- `ValidationGroups` — `Create` and `Update` marker interfaces

### `springflow-graphql`
- Auto-configuration, schema generation (`GraphQLSchemaGenerator`), `GenericGraphQLController`, DataLoader batching (`EntityBatchLoader`) for N+1 prevention

### `springflow-starter`
- `SpringFlowAutoConfiguration` — wires all beans conditionally; registers `EntityValidator`
- `SpringFlowProperties` — `@ConfigurationProperties("springflow")`

---

## Important Implementation Details

**JaCoCo:** Can cause `IllegalClassFormatException` when instrumenting Hibernate proxies or JDK classes. `springflow-demo` disables JaCoCo via POM. Use `-Djacoco.skip=true` when hitting this issue.

**Annotation processors:** `springflow-core` uses `-proc:none`. Lombok and MapStruct run in other modules. Order matters: Lombok must run before MapStruct.

**ID resolution:** supports `@Id` and `@EmbeddedId`. ID fields are always `readOnly: true` in metadata.

**Nullable detection priority:** `@Column(nullable=false)` → `@NotNull`/`@NotBlank` → primitive type → nullable.

**Relation generic types:** for `OneToMany`/`ManyToMany` collections, if `targetEntity == void.class`, the generic type parameter is used.

**Custom component detection:** `AutoApiRepositoryRegistrar` checks if a bean name like `{entityName}Repository` (or service/controller) already exists. If so, generation is skipped entirely for that layer.

---

## Conventions

- Conventional commits: `feat(module):`, `fix(module):`, `test:`, `refactor:`, `docs:`, `chore:`
- Logging: `debug` for cache/scan details, `info` for startup events, `warn` for non-critical issues, `error` for failures
- Test entities live in `springflow-core/src/test/java/io/springflow/core/test/entities/`
- Bean naming: `{entityName}Repository`, `{entityName}Service`, `{entityName}Controller` (camelCase entity name)
