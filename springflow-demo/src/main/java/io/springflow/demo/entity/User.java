package io.springflow.demo.entity;

import io.springflow.annotations.AutoApi;
import io.springflow.annotations.Hidden;
import io.springflow.annotations.ReadOnly;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User entity demonstrating field-level annotations.
 * <p>
 * Showcases:
 * - @Hidden annotation (password field excluded from DTOs)
 * - @ReadOnly annotation (timestamps)
 * - Complex validation rules (@Email, @Pattern, @Past)
 * - Enum support
 * </p>
 */
@Entity
@Table(name = "users")
@Data
@AutoApi(
        path = "/users",
        description = "User management API",
        tags = {"Users"}
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username must contain only letters, numbers and underscores")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @Hidden
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private Boolean active = true;

    @ReadOnly
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ReadOnly
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum UserRole {
        USER,
        ADMIN,
        MODERATOR
    }
}
