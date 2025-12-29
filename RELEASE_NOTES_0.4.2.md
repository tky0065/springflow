# SpringFlow v0.4.2 Release Notes

**Release Date:** December 29, 2025
**Focus:** GraphQL UX Improvement & Documentation Enhancement

---

## 🎯 Overview

Version 0.4.2 is a **bugfix and documentation release** that significantly improves the user experience when using SpringFlow's GraphQL module. This release addresses a critical UX issue where users would encounter startup failures when adding the `springflow-graphql` dependency without the proper Spring Boot GraphQL configuration.

---

## ✨ What's New

### 🔧 Fixed: GraphQL Startup Behavior

**Problem:**
Users adding `springflow-graphql` to their project would encounter this error:

```
org.springframework.beans.factory.NoSuchBeanDefinitionException:
No qualifying bean of type 'org.springframework.graphql.execution.BatchLoaderRegistry' available
```

**Solution:**
- Made `DataLoaderRegistrar` bean conditional on `BatchLoaderRegistry` existence
- Application now starts successfully even if Spring GraphQL is not fully configured
- REST API functionality works normally; only GraphQL DataLoaders are unavailable if configuration is incomplete

### 📚 Enhanced Documentation

Comprehensive documentation updates across all guides:

1. **README.md**
   - Added warning about mandatory Spring GraphQL configuration
   - Clear example of required `spring.graphql.graphiql.enabled=true` setting

2. **GraphQL Guide** (`docs/guide/graphql.md`)
   - Prominent warning in Quick Start section
   - New troubleshooting section for `NoSuchBeanDefinitionException`
   - Detailed explanation of required Spring GraphQL configuration
   - Enhanced Configuration Options with technical details

3. **Configuration Guide** (`docs/guide/configuration.md`)
   - New comprehensive GraphQL configuration section
   - Table of all GraphQL properties
   - Clear distinction between SpringFlow and Spring Boot settings
   - Best practices and examples

---

## 🔍 Technical Details

### Code Changes

**File Modified:** `springflow-graphql/src/main/java/io/springflow/graphql/config/DataLoaderRegistrar.java`

**Change:**
```java
@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(BatchLoaderRegistry.class)  // ← Added this
public class DataLoaderRegistrar {
    // ...
}
```

**Impact:**
- Bean only created when `BatchLoaderRegistry` is available
- Prevents startup failures
- Enables graceful degradation
- No breaking changes for existing users

### Root Cause Analysis

**Why did this happen?**

SpringFlow GraphQL depends on Spring Boot's GraphQL infrastructure (`BatchLoaderRegistry`, `GraphQlSource`, etc.). These beans are only created when Spring Boot's GraphQL auto-configuration activates.

The auto-configuration trigger (`spring.graphql.graphiql.enabled=true`) was not clearly documented as **mandatory**, leading users to add only the `springflow-graphql` dependency without the required Spring configuration.

**Why is the Spring configuration needed?**

Spring Boot uses conditional bean creation. GraphQL infrastructure beans like `BatchLoaderRegistry` are only created when certain conditions are met (e.g., GraphiQL is enabled). SpringFlow GraphQL legitimately depends on these beans - it doesn't create its own infrastructure, it builds on top of Spring's.

---

## 📋 Migration Guide

### For Existing Users

**No action required!** If your GraphQL is currently working, your configuration is already correct.

### For New Users

When adding `springflow-graphql` to your project, you **must** also configure Spring Boot GraphQL:

**Maven:**
```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-graphql</artifactId>
    <version>0.4.2</version>
</dependency>
```

**Configuration (application.yml):**
```yaml
springflow:
  graphql:
    enabled: true
    graphiql-enabled: true
    schema-location: src/main/resources/graphql

# MANDATORY: Activates Spring Boot GraphQL infrastructure
spring:
  graphql:
    graphiql:
      enabled: true
```

---

## 🧪 Testing

**All Tests Passing:** ✅

- **236+ unit and integration tests** pass successfully
- SpringFlow GraphQL module tests: ✅ Passing
- Demo application tests: ✅ Passing
- No regressions detected

**Tested Scenarios:**
1. ✅ New project with GraphQL properly configured → Works perfectly
2. ✅ New project without Spring GraphQL config → Graceful degradation (REST works, GraphQL disabled)
3. ✅ Existing GraphQL projects → No impact, continues working
4. ✅ REST-only projects → No impact

---

## 📖 Documentation

**Updated Files:**
- `README.md` - GraphQL configuration warning added
- `docs/guide/graphql.md` - Complete overhaul with warnings and troubleshooting
- `docs/guide/configuration.md` - New GraphQL configuration section
- `CHANGELOG.md` - Detailed entry for v0.4.2

**New Content:**
- GraphQL troubleshooting guide
- Configuration best practices
- Technical explanation of Spring GraphQL dependency

---

## 🔗 Related Resources

- **GraphQL Guide:** [docs/guide/graphql.md](docs/guide/graphql.md)
- **Configuration Guide:** [docs/guide/configuration.md](docs/guide/configuration.md)
- **CHANGELOG:** [CHANGELOG.md](CHANGELOG.md#042---2025-12-29)

---

## 🙏 Acknowledgments

This release was driven by real user feedback highlighting a confusing onboarding experience. Special thanks to users who reported the `NoSuchBeanDefinitionException` issue!

---

## 🚀 What's Next?

**Upcoming in v0.5.0:**
- GraphQL relation field resolvers (automatic loading of JPA relationships)
- GraphQL subscriptions for real-time updates
- Enhanced filtering with complex query support
- Performance optimizations

**Stay tuned!**

---

## 📦 Installation

### Maven

```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.4.2</version>
</dependency>

<!-- Optional: GraphQL Support -->
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-graphql</artifactId>
    <version>0.4.2</version>
</dependency>
```

### Gradle

```gradle
implementation 'io.github.tky0065:springflow-starter:0.4.2'

// Optional: GraphQL Support
implementation 'io.github.tky0065:springflow-graphql:0.4.2'
```

---

**Full Changelog:** [CHANGELOG.md](CHANGELOG.md)
**GitHub Release:** [v0.4.2](https://github.com/tky0065/springflow/releases/tag/v0.4.2)
