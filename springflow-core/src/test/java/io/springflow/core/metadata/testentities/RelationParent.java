package io.springflow.core.metadata.testentities;

import io.springflow.annotations.AutoApi;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;

@Entity
@AutoApi
public class RelationParent {
    @Id
    private Long id;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<RelationChild> children;
}
