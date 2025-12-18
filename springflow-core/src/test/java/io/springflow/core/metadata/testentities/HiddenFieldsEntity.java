package io.springflow.core.metadata.testentities;

import io.springflow.annotations.AutoApi;
import io.springflow.annotations.Hidden;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@Entity
@AutoApi
public class HiddenFieldsEntity {

    @Id
    private Long id;

    private String visible;

    @Hidden
    private String hiddenField;

    @Transient
    private String transientField;

    private static String staticField;
}
