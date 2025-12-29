# Installation

This guide covers detailed installation and configuration of SpringFlow in your Spring Boot application.

## Prerequisites

- **Java**: 17 or higher
- **Spring Boot**: 3.2.1 or higher
- **Build Tool**: Maven 3.6+ or Gradle 7.0+

## Maven Installation

Add the SpringFlow starter dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.4.3</version>
</dependency>
```

## Gradle Installation

Add the dependency to your `build.gradle`:

```gradle
implementation 'io.github.tky0065:springflow-starter:0.4.3'
```

For Gradle Kotlin DSL (`build.gradle.kts`):

```kotlin
implementation("io.github.tky0065:springflow-starter:0.4.3")
```

## Configuration

SpringFlow works with **zero configuration** by default, but you can customize behavior in `application.yml`:

```yaml
springflow:
  enabled: true
  base-path: /api
  base-packages: com.example.myapp
  pagination:
    default-page-size: 20
    max-page-size: 100
  swagger:
    enabled: true
```

See [Configuration Guide](../guide/configuration.md) for all available options.

## Verification

After adding the dependency, start your application:

```bash
./mvnw spring-boot:run
```

Check the logs for SpringFlow initialization:

```
INFO  AutoApiRepositoryRegistrar - Starting AutoApi Repository, Service, and Controller Registration...
INFO  AutoApiRepositoryRegistrar - AutoApi registration completed. Registered X entities.
```

Visit Swagger UI at: `http://localhost:8080/swagger-ui.html`

## Next Steps

- [Create Your First API](first-project.md)
- [Explore Annotations](../guide/annotations.md)
- [Configure Pagination](../guide/pagination.md)
