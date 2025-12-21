package io.springflow.core.kotlin

import io.springflow.annotations.AutoApi
import io.springflow.annotations.Hidden
import io.springflow.annotations.ReadOnly
import jakarta.persistence.*
import jakarta.validation.constraints.*

/**
 * Kotlin data class entity for testing SpringFlow Kotlin support.
 *
 * This entity tests:
 * - Kotlin data class support
 * - Nullable types (String?)
 * - Non-nullable types
 * - Default values
 * - Validation annotations with Kotlin annotation targets
 * - @Hidden and @ReadOnly annotations
 */
@Entity
@AutoApi(path = "/kotlin-entities")
@Table(name = "kotlin_entities")
data class KotlinEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "Name must not be blank")
    @field:Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    val name: String,

    @field:Email(message = "Email must be valid")
    val email: String?,

    @field:Min(value = 0, message = "Age must be positive")
    @field:Max(value = 150, message = "Age must be less than 150")
    val age: Int = 0,

    val description: String? = null,

    @Hidden
    val secretToken: String? = null,

    @ReadOnly
    val createdAt: String? = null,

    @field:NotNull(message = "Active status must not be null")
    val active: Boolean = true
) {
    // Secondary constructor for JPA
    constructor() : this(
        id = null,
        name = "",
        email = null,
        age = 0,
        description = null,
        secretToken = null,
        createdAt = null,
        active = true
    )
}

/**
 * Simple Kotlin entity without nullable types for testing.
 */
@Entity
@AutoApi(path = "/simple-kotlin")
data class SimpleKotlinEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank
    val title: String,

    val count: Int = 0
) {
    constructor() : this(id = null, title = "", count = 0)
}
