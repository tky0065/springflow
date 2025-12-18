package io.springflow.core.metadata.testentities;

import io.springflow.annotations.AutoApi;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@AutoApi
public class ValidatedEntity {

    @Id
    private Long id;

    @NotBlank
    @Size(min = 5, max = 50)
    private String name;

    @Email
    @NotNull
    private String email;

    @Min(18)
    @Max(100)
    private Integer age;
}
