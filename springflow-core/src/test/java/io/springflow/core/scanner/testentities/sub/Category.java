package io.springflow.core.scanner.testentities.sub;

import io.springflow.annotations.AutoApi;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Test entity in sub-package with @AutoApi annotation.
 */
@Entity
@Table(name = "categories")
@AutoApi(path = "/categories")
public class Category {

    @Id
    private Long id;

    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

