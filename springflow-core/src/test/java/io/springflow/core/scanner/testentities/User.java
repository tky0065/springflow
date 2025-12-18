package io.springflow.core.scanner.testentities;

import io.springflow.annotations.AutoApi;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Test entity with @AutoApi annotation.
 */
@Entity
@Table(name = "users")
@AutoApi(path = "/users")
public class User {

    @Id
    private Long id;

    private String name;

    private String email;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

