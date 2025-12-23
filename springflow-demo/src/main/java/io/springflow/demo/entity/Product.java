package io.springflow.demo.entity;

import io.springflow.annotations.Auditable;
import io.springflow.annotations.AutoApi;
import io.springflow.annotations.FilterType;
import io.springflow.annotations.Filterable;
import io.springflow.annotations.ReadOnly;
import io.springflow.annotations.SoftDelete;
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
 * - Dynamic filtering via @Filterable
 * - Soft delete via @SoftDelete
 * - Automatic auditing via @Auditable
 * - ManyToOne relationship with Category
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
@Auditable
@SoftDelete
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    @Filterable(types = {FilterType.EQUALS, FilterType.LIKE})
    @Column(nullable = false)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be positive")
    @Filterable(types = FilterType.RANGE)
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

    private boolean deleted = false;

    private LocalDateTime deletedAt;

    @ReadOnly
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ReadOnly
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}
