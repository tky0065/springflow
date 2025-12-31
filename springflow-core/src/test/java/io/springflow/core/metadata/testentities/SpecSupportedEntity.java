package io.springflow.core.metadata.testentities;

import io.springflow.annotations.AutoApi;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@AutoApi(supportSpecification = true)
public class SpecSupportedEntity {
    @Id
    private Long id;
}
