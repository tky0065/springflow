package io.springflow.demo.entity;

import io.springflow.annotations.AutoApi;
import io.springflow.annotations.ReadOnly;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Product entity demonstrating SpringFlow auto-generated REST API.
 * <p>
 * This entity showcases:
 * - Full CRUD operations via @AutoApi
 * - JSR-380 validation constraints
 * - @ReadOnly fields (createdAt, updatedAt)
 * - ManyToOne relationship with Category
 * </p>
 * <p>
 * Auto-generated endpoints:
 * - GET    /api/products (list with pagination)
 * - GET    /api/products/{id}
 * - POST   /api/products
 * - PUT    /api/products/{id}
 * - DELETE /api/products/{id}
 * </p>
 */
@Entity
@Table(name = "products")
@Data
@AutoApi(
        path = "/products",
        description = "Product management API",
        tags = {"Products"}
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be positive")
    @Column(nullable = false)
    private Double price;

    @Min(value = 0, message = "Stock must be positive")
    @Column(nullable = false)
    private Integer stock = 0;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private Boolean active = true;

    @ReadOnly
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ReadOnly
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
