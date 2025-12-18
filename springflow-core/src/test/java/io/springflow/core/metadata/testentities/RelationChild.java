package io.springflow.core.metadata.testentities;

import io.springflow.annotations.AutoApi;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
@AutoApi
public class RelationChild {
    @Id
    private Long id;

    @ManyToOne
    private RelationParent parent;
}
