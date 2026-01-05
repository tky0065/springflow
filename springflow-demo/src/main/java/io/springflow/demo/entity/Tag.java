package io.springflow.demo.entity;

import io.springflow.annotations.AutoApi;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
@Data
@AutoApi(
        path = "/tags",
        description = "Tag management API",
        tags = {"Tags"}
)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    @ToString.Exclude
    private List<Product> products = new ArrayList<>();
}
