package io.springflow.core.repository.testentities;

import io.springflow.annotations.AutoApi;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Test entity for fully custom component scenarios.
 * Used to test that SpringFlow detects all custom components (repo, service, controller) and skips all generation.
 */
@Entity
@AutoApi
public class FullyCustomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    public FullyCustomEntity() {
    }

    public FullyCustomEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }

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
