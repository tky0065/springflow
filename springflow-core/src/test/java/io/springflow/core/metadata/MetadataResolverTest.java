package io.springflow.core.metadata;

import io.springflow.core.metadata.testentities.*;
import jakarta.persistence.FetchType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link MetadataResolver}.
 */
class MetadataResolverTest {

    private MetadataResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new MetadataResolver();
    }

    @Test
    @DisplayName("Should resolve simple entity metadata")
    void testResolveSimpleEntity() {
        EntityMetadata metadata = resolver.resolve(ValidatedEntity.class);

        assertThat(metadata).isNotNull();
        assertThat(metadata.entityClass()).isEqualTo(ValidatedEntity.class);
        assertThat(metadata.entityName()).isEqualTo("ValidatedEntity");
        assertThat(metadata.idType()).isEqualTo(Long.class);
        assertThat(metadata.autoApiConfig()).isNotNull();
    }

    @Test
    @DisplayName("Should extract ID field correctly")
    void testExtractionIdSimple() {
        EntityMetadata metadata = resolver.resolve(ValidatedEntity.class);
        
        Optional<FieldMetadata> idField = metadata.getIdField();
        assertThat(idField).isPresent();
        assertThat(idField.get().name()).isEqualTo("id");
        assertThat(idField.get().isId()).isTrue();
        assertThat(idField.get().type()).isEqualTo(Long.class);
    }

    @Test
    @DisplayName("Should extract embedded ID correctly")
    void testExtractionIdComposite() {
        EntityMetadata metadata = resolver.resolve(CompositeIdEntity.class);
        
        Optional<FieldMetadata> idField = metadata.getIdField();
        assertThat(idField).isPresent();
        assertThat(idField.get().name()).isEqualTo("id");
        assertThat(idField.get().isId()).isTrue();
        assertThat(idField.get().type()).isEqualTo(CompositeIdEntity.CompositeKey.class);
    }

    @Test
    @DisplayName("Should extract validations")
    void testExtractionValidations() {
        EntityMetadata metadata = resolver.resolve(ValidatedEntity.class);
        
        Optional<FieldMetadata> nameField = metadata.getFieldByName("name");
        assertThat(nameField).isPresent();
        assertThat(nameField.get().validations()).hasSize(2); // @NotBlank, @Size
        
        Optional<FieldMetadata> emailField = metadata.getFieldByName("email");
        assertThat(emailField).isPresent();
        assertThat(emailField.get().validations()).hasSize(2); // @Email, @NotNull
        
        Optional<FieldMetadata> ageField = metadata.getFieldByName("age");
        assertThat(ageField).isPresent();
        assertThat(ageField.get().validations()).hasSize(2); // @Min, @Max
    }

    @Test
    @DisplayName("Should extract relations")
    void testExtractionRelations() {
        EntityMetadata metadata = resolver.resolve(RelationParent.class);
        
        Optional<FieldMetadata> childrenField = metadata.getFieldByName("children");
        assertThat(childrenField).isPresent();
        assertThat(childrenField.get().isRelation()).isTrue();
        
        RelationMetadata relation = childrenField.get().relation();
        assertThat(relation).isNotNull();
        assertThat(relation.type()).isEqualTo(RelationMetadata.RelationType.ONE_TO_MANY);
        assertThat(relation.fetchType()).isEqualTo(FetchType.EAGER);
        assertThat(relation.targetEntity()).isEqualTo(RelationChild.class);
        assertThat(relation.mappedBy()).isEqualTo("parent");
    }

    @Test
    @DisplayName("Should handle hidden and transient fields")
    void testHiddenFields() {
        EntityMetadata metadata = resolver.resolve(HiddenFieldsEntity.class);
        
        assertThat(metadata.getFieldByName("id")).isPresent();
        assertThat(metadata.getFieldByName("visible")).isPresent();
        
        // Static field should be ignored
        assertThat(metadata.getFieldByName("staticField")).isEmpty();
        
        // Transient field should be ignored
        assertThat(metadata.getFieldByName("transientField")).isEmpty();
        
        // @Hidden field should be present but marked as hidden
        Optional<FieldMetadata> hiddenField = metadata.getFieldByName("hiddenField");
        assertThat(hiddenField).isPresent();
        assertThat(hiddenField.get().hidden()).isTrue();
        
        assertThat(metadata.getFieldByName("visible").get().hidden()).isFalse();
    }

    @Test
    @DisplayName("Should support inheritance")
    void testInheritance() {
        EntityMetadata metadata = resolver.resolve(InheritedEntity.class);
        
        // Should have fields from BaseEntity
        assertThat(metadata.getFieldByName("id")).isPresent();
        assertThat(metadata.getFieldByName("baseField")).isPresent();
        
        // And fields from InheritedEntity
        assertThat(metadata.getFieldByName("childField")).isPresent();
        
        // ID should be resolved from BaseEntity
        assertThat(metadata.getIdField()).isPresent();
        assertThat(metadata.getIdField().get().name()).isEqualTo("id");
    }

    @Test
    @DisplayName("Should extract version field correctly")
    void testExtractionVersion() {
        EntityMetadata metadata = resolver.resolve(VersionedEntity.class);
        
        Optional<FieldMetadata> versionField = metadata.getFieldByName("version");
        assertThat(versionField).isPresent();
        assertThat(versionField.get().isVersion()).isTrue();
        assertThat(versionField.get().readOnly()).isTrue();
        assertThat(metadata.isVersioned()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception if @AutoApi is missing")
    void testMissingAutoApi() {
        class NonAutoApiEntity {}
        assertThatThrownBy(() -> resolver.resolve(NonAutoApiEntity.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be annotated with");
    }
}
