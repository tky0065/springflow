package io.springflow.core.metadata.testentities;

import io.springflow.annotations.AutoApi;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@Entity
@AutoApi
public class InheritedEntity extends BaseEntity {
    private String childField;
}

@MappedSuperclass
class BaseEntity {
    @Id
    private Long id;
    
    private String baseField;
}
