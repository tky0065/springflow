package io.springflow.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.springflow.annotations.AutoApi;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Category entity with hierarchical structure.
 * <p>
 * Demonstrates:
 * - Self-referencing relationship (parent/children)
 * - OneToMany bidirectional relationship with Product
 * - Simple validation rules
 * </p>
 */
@Entity
@Table(name = "categories")
@Data
@AutoApi(
        path = "/categories",
        description = "Product category management API",
        tags = {"Categories"}
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    @Column(nullable = false, unique = true)
    private String name;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @ToString.Exclude
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonIgnoreProperties("parent")
    private List<Category> children = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    @ToString.Exclude
    @JsonIgnoreProperties("category")
    private List<Product> products = new ArrayList<>();
}
