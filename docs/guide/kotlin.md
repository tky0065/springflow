# Kotlin Support in SpringFlow

## Overview

SpringFlow includes comprehensive Kotlin support, allowing you to use Kotlin data classes as JPA entities with all SpringFlow features.

## Configuration

Kotlin support is fully configured in:
- **Parent POM**: Kotlin version 1.9.22, kotlin-stdlib, kotlin-reflect
- **springflow-core POM**: Kotlin Maven plugin for test compilation
- **Test entities**: Example Kotlin data classes in `src/test/kotlin/`

## Features Supported

### :material-check-circle:{ .success } Kotlin Data Classes
- Full support for Kotlin `data class` as JPA entities
- Support for `@AutoApi` annotation on data classes
- All SpringFlow annotations work with Kotlin

### :material-check-circle:{ .success } Nullable Types
- Kotlin nullable types (`String?`, `Int?`) are fully supported
- Proper detection of nullable/non-nullable fields
- Validation annotations work with nullable types

### :material-check-circle:{ .success } Annotation Targets
- Support for `@field:NotBlank`, `@field:Email`, etc.
- Support for `@get:NotBlank` annotation targets
- All JSR-380 validation annotations compatible

### :material-check-circle:{ .success } Default Values
- Kotlin default parameter values are preserved
- Works with JPA no-arg constructor requirement

### :material-check-circle:{ .success } SpringFlow Features
- Metadata resolution from Kotlin classes
- DTO mapping (entity ↔ Map)
- Repository generation
- Service generation
- Controller generation
- OpenAPI documentation

## Java Version Compatibility

**IMPORTANT**: Kotlin 1.9.22 is optimized for **Java 17**.

### Recommended Setup
```xml
<properties>
    <java.version>17</java.version>
    <kotlin.version>1.9.22</kotlin.version>
</properties>
```

### Java 21+ Compatibility Note
If using Java 21, you may encounter Kotlin compiler issues with JRT filesystem. In this case:
1. Use Java 17 for maximum compatibility
2. OR upgrade to Kotlin 2.0+ when available
3. OR use Java entities instead of Kotlin for now

## Example Kotlin Entity

```kotlin
@Entity
@AutoApi(path = "/products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank
    @field:Size(min = 3, max = 100)
    val name: String,

    @field:Min(0)
    val price: Double = 0.0,

    val description: String? = null,

    @Hidden
    val secretKey: String? = null,

    @ReadOnly
    val createdAt: String? = null
) {
    // No-arg constructor required by JPA
    constructor() : this(id = null, name = "", price = 0.0)
}
```

## Testing

Kotlin test entities and tests are provided in:
- `src/test/kotlin/io/springflow/core/kotlin/KotlinEntity.kt`
- `src/test/kotlin/io/springflow/core/kotlin/KotlinSupportTest.kt`

To run Kotlin tests:
```bash
# Ensure you're using Java 17
java -version

# Run tests
./mvnw test -Dtest=KotlinSupportTest
```

## Limitations

### Phase 1 MVP
- Kotlin support tested with Java 17
- Some Java 21 users may need to use Java 17 for Kotlin compilation
- Extension functions and DSL features deferred to Phase 2
- Coroutines support deferred to Phase 2

### Future Enhancements (Phase 2)
- Kotlin extension functions for repositories/services
- Suspend function support
- Coroutines for reactive operations
- Kotlin DSL for configuration

## Migration from Java

Converting Java entities to Kotlin is straightforward:

```java
// Java
@Entity
@AutoApi
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    private String name;

    // ... getters/setters
}
```

```kotlin
// Kotlin
@Entity
@AutoApi
data class User(
    @Id
    @GeneratedValue
    val id: Long? = null,

    @field:NotBlank
    val name: String
) {
    constructor() : this(id = null, name = "")
}
```

## Troubleshooting

### Kotlin compilation fails
- Check Java version (`java -version` should show 17.x)
- Verify Kotlin version in parent POM
- Ensure kotlin-maven-plugin is configured correctly

### Validation annotations not working
- Use `@field:` target: `@field:NotBlank`
- Don't use `@get:` or `@param:` for JPA fields

### JPA errors with data classes
- Ensure no-arg constructor is provided
- Use `var` instead of `val` if JPA requires mutability
- Mark JPA properties as `open` if using inheritance

## Conclusion

Kotlin support in SpringFlow is production-ready for Java 17 environments. All core features work seamlessly with Kotlin data classes, providing a more concise and expressive way to define JPA entities with auto-generated REST APIs.
