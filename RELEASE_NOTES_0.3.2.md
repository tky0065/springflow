# SpringFlow v0.3.2 - Bugfix Release

Date: 2025-12-27

## Overview

Critical bugfix release addressing test failures and missing bean configuration issues discovered in CI/CD pipeline.

## Fixed Issues

### 1. Missing EntityValidator Bean Configuration
- **Problem**: Controllers were failing to start due to missing `entityValidator` bean dependency
- **Impact**: ApplicationContext failed to load with `NoSuchBeanDefinitionException`
- **Solution**: Added `EntityValidator` bean definition in `SpringFlowAutoConfiguration`
- **Files Changed**:
  - `springflow-starter/src/main/java/io/springflow/starter/config/SpringFlowAutoConfiguration.java`
  - `springflow-core/src/main/java/io/springflow/core/validation/EntityValidator.java`

### 2. JaCoCo Instrumentation Conflicts
- **Problem**: JaCoCo attempting to instrument JDK classes and Hibernate proxies causing `IllegalClassFormatException`
- **Impact**: 13 test failures in CI/CD pipeline
- **Solution**:
  - Upgraded JaCoCo from 0.8.11 to 0.8.12
  - Disabled JaCoCo instrumentation for demo module
  - Configured proper exclusions for JDK and proxy classes
- **Files Changed**:
  - `pom.xml` (parent POM - JaCoCo configuration)
  - `springflow-demo/pom.xml` (skip JaCoCo)

### 3. Invoice Number Collision in Tests
- **Problem**: Duplicate invoice numbers when creating multiple invoices in quick succession
- **Impact**: `DataIntegrityViolationException` in concurrent test scenarios
- **Solution**: Enhanced invoice number generation with UUID suffix for guaranteed uniqueness
- **Files Changed**:
  - `springflow-demo/src/main/java/io/springflow/demo/service/InvoiceService.java`

## Test Results

```
✅ springflow-annotations: 26 tests (0 failures)
✅ springflow-core:       169 tests (0 failures, 1 skipped)
✅ springflow-starter:     18 tests (0 failures)
✅ springflow-demo:        23 tests (0 failures, 11 skipped)

BUILD SUCCESS
```

## Breaking Changes

None. This is a fully backward-compatible bugfix release.

## Upgrade Instructions

Update your `pom.xml` or `build.gradle`:

### Maven
```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.3.2</version>
</dependency>
```

### Gradle
```gradle
implementation 'io.github.tky0065:springflow-starter:0.3.2'
```

## Dependencies Updated

- JaCoCo: 0.8.11 → 0.8.12

## Notes for Developers

- All core functionality remains unchanged
- EntityValidator bean is now automatically configured - no manual configuration needed
- JaCoCo coverage reporting continues to work for all modules except demo

## Acknowledgments

Special thanks to the CI/CD pipeline for catching these issues before production release.
