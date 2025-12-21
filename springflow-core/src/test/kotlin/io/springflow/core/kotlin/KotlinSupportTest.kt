package io.springflow.core.kotlin

import io.springflow.core.metadata.MetadataResolver
import io.springflow.core.mapper.DtoMapperFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for Kotlin data class support in SpringFlow.
 *
 * This test suite verifies that SpringFlow works correctly with:
 * - Kotlin data classes
 * - Nullable types (String?, Int?)
 * - Non-nullable types
 * - Default values
 * - Kotlin annotation targets (@field, @get)
 */
class KotlinSupportTest {

    private lateinit var metadataResolver: MetadataResolver
    private lateinit var mapperFactory: DtoMapperFactory

    @BeforeEach
    fun setUp() {
        metadataResolver = MetadataResolver()
        mapperFactory = DtoMapperFactory()
    }

    @Test
    fun `should resolve metadata from Kotlin data class`() {
        // When
        val metadata = metadataResolver.resolve(KotlinEntity::class.java)

        // Then
        assertThat(metadata).isNotNull
        assertThat(metadata.entityClass).isEqualTo(KotlinEntity::class.java)
        assertThat(metadata.entityName).isEqualTo("KotlinEntity")
        assertThat(metadata.tableName).isEqualTo("kotlin_entities")
    }

    @Test
    fun `should detect ID field in Kotlin data class`() {
        // When
        val metadata = metadataResolver.resolve(KotlinEntity::class.java)

        // Then
        val idField = metadata.fields().find { it.isId }
        assertThat(idField).isNotNull
        assertThat(idField?.name).isEqualTo("id")
        assertThat(idField?.type).isEqualTo(Long::class.java)
    }

    @Test
    fun `should detect all fields in Kotlin data class`() {
        // When
        val metadata = metadataResolver.resolve(KotlinEntity::class.java)

        // Then
        val fieldNames = metadata.fields().map { it.name }
        assertThat(fieldNames).contains(
            "id", "name", "email", "age", "description",
            "secretToken", "createdAt", "active"
        )
    }

    @Test
    fun `should detect @field annotation targets on Kotlin properties`() {
        // When
        val metadata = metadataResolver.resolve(KotlinEntity::class.java)

        // Then - Validation annotations with @field: target should be detected
        val nameField = metadata.fields().find { it.name == "name" }
        assertThat(nameField?.validations).isNotEmpty

        val emailField = metadata.fields().find { it.name == "email" }
        assertThat(emailField?.validations).isNotEmpty
    }

    @Test
    fun `should handle nullable types in Kotlin`() {
        // When
        val metadata = metadataResolver.resolve(KotlinEntity::class.java)

        // Then
        val emailField = metadata.fields().find { it.name == "email" }
        assertThat(emailField).isNotNull
        // Email is nullable (String?) but has @Email validation
        assertThat(emailField?.nullable).isTrue

        val descriptionField = metadata.fields().find { it.name == "description" }
        assertThat(descriptionField?.nullable).isTrue
    }

    @Test
    fun `should handle non-nullable types in Kotlin`() {
        // When
        val metadata = metadataResolver.resolve(KotlinEntity::class.java)

        // Then
        val nameField = metadata.fields().find { it.name == "name" }
        assertThat(nameField).isNotNull
        // name is non-nullable (String) and has @NotBlank
        assertThat(nameField?.nullable).isFalse
    }

    @Test
    fun `should detect @Hidden annotation in Kotlin data class`() {
        // When
        val metadata = metadataResolver.resolve(KotlinEntity::class.java)

        // Then
        val secretField = metadata.fields().find { it.name == "secretToken" }
        assertThat(secretField?.hidden).isTrue
    }

    @Test
    fun `should detect @ReadOnly annotation in Kotlin data class`() {
        // When
        val metadata = metadataResolver.resolve(KotlinEntity::class.java)

        // Then
        val createdAtField = metadata.fields().find { it.name == "createdAt" }
        assertThat(createdAtField?.readOnly).isTrue
    }

    @Test
    fun `should map Kotlin entity to DTO`() {
        // Given
        val metadata = metadataResolver.resolve(KotlinEntity::class.java)
        val mapper = mapperFactory.getMapper(KotlinEntity::class.java, metadata)

        val entity = KotlinEntity(
            id = 1L,
            name = "Test Entity",
            email = "test@example.com",
            age = 25,
            description = "Test description",
            secretToken = "secret",
            createdAt = "2025-01-01",
            active = true
        )

        // When
        val outputDto = mapper.toOutputDto(entity)

        // Then
        assertThat(outputDto).isNotNull
        assertThat(outputDto["id"]).isEqualTo(1L)
        assertThat(outputDto["name"]).isEqualTo("Test Entity")
        assertThat(outputDto["email"]).isEqualTo("test@example.com")
        assertThat(outputDto["age"]).isEqualTo(25)
        assertThat(outputDto["description"]).isEqualTo("Test description")
        assertThat(outputDto["createdAt"]).isEqualTo("2025-01-01")
        assertThat(outputDto["active"]).isEqualTo(true)
        // @Hidden field should be excluded
        assertThat(outputDto).doesNotContainKey("secretToken")
    }

    @Test
    fun `should map DTO to Kotlin entity`() {
        // Given
        val metadata = metadataResolver.resolve(KotlinEntity::class.java)
        val mapper = mapperFactory.getMapper(KotlinEntity::class.java, metadata)

        val inputDto = mapOf(
            "name" to "New Entity",
            "email" to "new@example.com",
            "age" to 30,
            "description" to "New description",
            "active" to false
        )

        // When
        val entity = mapper.toEntity(inputDto)

        // Then
        assertThat(entity).isNotNull
        assertThat(entity.name).isEqualTo("New Entity")
        assertThat(entity.email).isEqualTo("new@example.com")
        assertThat(entity.age).isEqualTo(30)
        assertThat(entity.description).isEqualTo("New description")
        assertThat(entity.active).isEqualTo(false)
        // ID should not be set from input
        assertThat(entity.id).isNull()
    }

    @Test
    fun `should handle Kotlin data class with default values`() {
        // Given
        val metadata = metadataResolver.resolve(KotlinEntity::class.java)

        // Then - Fields with default values should be detected
        val ageField = metadata.fields().find { it.name == "age" }
        assertThat(ageField).isNotNull

        val activeField = metadata.fields().find { it.name == "active" }
        assertThat(activeField).isNotNull
    }

    @Test
    fun `should work with simple Kotlin data class`() {
        // When
        val metadata = metadataResolver.resolve(SimpleKotlinEntity::class.java)

        // Then
        assertThat(metadata).isNotNull
        assertThat(metadata.entityClass).isEqualTo(SimpleKotlinEntity::class.java)
        assertThat(metadata.fields()).hasSize(3) // id, title, count
    }

    @Test
    fun `should support Kotlin data class copy method`() {
        // Given
        val original = KotlinEntity(
            id = 1L,
            name = "Original",
            email = "original@example.com",
            age = 25
        )

        // When - Using Kotlin data class copy method
        val copied = original.copy(name = "Modified")

        // Then
        assertThat(copied.id).isEqualTo(original.id)
        assertThat(copied.name).isEqualTo("Modified")
        assertThat(copied.email).isEqualTo(original.email)
        assertThat(copied.age).isEqualTo(original.age)
    }
}
