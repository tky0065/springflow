# SpringFlow v0.4.1 - GraphQL Bugfixes Release

**Release Date**: December 28, 2025
**Type**: PATCH (Bugfixes)
**Previous Version**: 0.4.0

---

## Overview

SpringFlow v0.4.1 is a bugfix release that resolves critical GraphQL compatibility issues with Spring Boot 4.0.1 and Java 25. This release ensures full GraphQL functionality following the framework upgrade in v0.4.0.

---

## Fixed Issues

### 🐛 GraphQL Module - Spring Boot 4.0.1 Compatibility

**Issue**: GraphQL queries failed with parameter resolution errors after Spring Boot 4.0.1 migration.

**Root Causes**:
1. ByteBuddy 1.17.0 incompatibility with Java 25 and Hibernate 7.2.0
2. Missing GraphQLFilterConverter bean registration
3. GraphQL schema generator creating malformed schema structure
4. ByteBuddy not preserving @Argument parameter annotations
5. Conflicts with custom service implementations

**Fixes**:
- ✅ Updated ByteBuddy from 1.17.0 to 1.17.8 for Java 25 compatibility
- ✅ Added GraphQLFilterConverter bean in SpringFlowGraphQLAutoConfiguration
- ✅ Implemented custom service detection to skip conflicting controller generation
- ✅ Corrected GraphQL schema generation to properly structure Query and Page types
- ✅ Preserved parameter annotations using MethodAttributeAppender in ByteBuddy
- ✅ Migrated GenericGraphQLController to DtoMapperFactory architecture
- ✅ Regenerated demo schema with correct structure

**Impact**: GraphQL module now fully functional with Spring Boot 4.0.1 + Java 25

---

## Test Results

**Total Tests**: 236+ tests (all passing ✅)

**GraphQL Validation**:
```graphql
# Simple query - ✅ Working
{ products { content { name price } } }

# Paginated query - ✅ Working
{ products(page: 0, size: 3) {
  content { name price }
  pageInfo { totalElements totalPages hasNext }
} }
```

**Test Environment**:
- Spring Boot 4.0.1
- Java 25
- ByteBuddy 1.17.8

---

## Breaking Changes

**None** - This is a backward-compatible bugfix release.

---

## Upgrade Instructions

### Maven

Update your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.4.1</version>
</dependency>

<!-- If using GraphQL -->
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-graphql</artifactId>
    <version>0.4.1</version>
</dependency>
```

### Gradle

Update your `build.gradle`:

```gradle
implementation 'io.github.tky0065:springflow-starter:0.4.1'

// If using GraphQL
implementation 'io.github.tky0065:springflow-graphql:0.4.1'
```

---

## Dependencies Updated

| Dependency | Previous | New | Reason |
|------------|----------|-----|--------|
| ByteBuddy | 1.17.0 | 1.17.8 | Java 25 + Spring Boot 4.0.1 compatibility |

---

## Files Modified

**Total**: 7 files across 2 modules

### springflow-parent
- `pom.xml` - ByteBuddy version update

### springflow-graphql
- `config/SpringFlowGraphQLAutoConfiguration.java` - Added GraphQLFilterConverter bean
- `config/GraphQLControllerRegistrar.java` - Custom service detection
- `schema/GraphQLSchemaGenerator.java` - Schema generation fix
- `generator/GraphQLControllerGenerator.java` - Parameter annotation preservation
- `controller/GenericGraphQLController.java` - DtoMapperFactory migration

### springflow-demo
- `resources/graphql/schema.graphqls` - Regenerated corrected schema

---

## Notes for Developers

### If You're Extending GraphQL

1. **Custom Services**: If you have custom service implementations (e.g., `CustomerService extends GenericCrudService`), GraphQL controller generation will automatically skip those entities to avoid conflicts.

2. **Schema Regeneration**: The GraphQL schema is auto-generated at startup. Delete `schema.graphqls` to force regeneration if needed.

3. **ByteBuddy Version**: Do not override ByteBuddy version in your project - use the version managed by SpringFlow (1.17.8).

### If You Encounter Issues

1. Clear Maven cache: `./mvnw dependency:purge-local-repository`
2. Clean build: `./mvnw clean install`
3. Check logs for GraphQL controller registration messages
4. Verify GraphQL schema at `src/main/resources/graphql/schema.graphqls`

---

## What's Next?

**Version 0.4.2-SNAPSHOT** is now under development.

**Roadmap**:
- Admin UI development
- MongoDB support exploration
- Enhanced monitoring with Actuator

---

## Links

- **GitHub Release**: https://github.com/tky0065/springflow/releases/tag/v0.4.1
- **Maven Central**: https://central.sonatype.com/artifact/io.github.tky0065/springflow-starter/0.4.1
- **Documentation**: https://tky0065.github.io/springflow/
- **Changelog**: https://github.com/tky0065/springflow/blob/main/CHANGELOG.md

---

**Full Changelog**: https://github.com/tky0065/springflow/compare/v0.4.0...v0.4.1
