package io.springflow.core.metadata.testentities;

import io.springflow.annotations.AutoApi;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import java.io.Serializable;
import jakarta.persistence.Embeddable;

@Entity
@AutoApi
public class CompositeIdEntity {

    @EmbeddedId
    private CompositeKey id;

    private String data;

    @Embeddable
    public static class CompositeKey implements Serializable {
        private String key1;
        private String key2;
    }
}
