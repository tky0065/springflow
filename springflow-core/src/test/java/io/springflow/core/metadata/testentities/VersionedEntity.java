package io.springflow.core.metadata.testentities;

import io.springflow.annotations.AutoApi;
import io.springflow.annotations.Auditable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
@AutoApi
@Auditable(versioned = true)
public class VersionedEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
