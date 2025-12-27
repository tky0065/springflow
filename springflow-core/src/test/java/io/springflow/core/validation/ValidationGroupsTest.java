package io.springflow.core.validation;

import io.springflow.core.validation.ValidationGroups.Create;
import io.springflow.core.validation.ValidationGroups.Update;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for JSR-380 validation groups support.
 */
class ValidationGroupsTest {

    private Validator validator;
    private EntityValidator entityValidator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        entityValidator = new EntityValidator(validator);
    }

    @Test
    void validateForCreate_shouldApplyCreateGroupValidations() {
        // Given
        TestProduct product = new TestProduct();
        product.name = "Test Product";
        // Missing initialCategory (required for Create group only)

        // When/Then
        assertThatThrownBy(() -> entityValidator.validateForCreate(product))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("initialCategory");
    }

    @Test
    void validateForCreate_shouldPassWithAllRequiredFields() {
        // Given
        TestProduct product = new TestProduct();
        product.name = "Test Product";
        product.initialCategory = "Electronics";
        product.price = 29.99;

        // When/Then - should not throw
        entityValidator.validateForCreate(product);
    }

    @Test
    void validateForUpdate_shouldNotRequireCreateOnlyFields() {
        // Given
        TestProduct product = new TestProduct();
        product.name = "Test Product";
        product.price = 19.99;
        // Missing initialCategory - but it's OK for Update group

        // When/Then - should not throw
        entityValidator.validateForUpdate(product);
    }

    @Test
    void validateForUpdate_shouldApplyUpdateGroupValidations() {
        // Given
        TestProduct product = new TestProduct();
        product.name = "Test Product";
        product.price = 29.99;
        product.supplierEmail = "invalid-email"; // Invalid email for Update group

        // When/Then
        assertThatThrownBy(() -> entityValidator.validateForUpdate(product))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("supplierEmail");
    }

    @Test
    void validateForUpdate_shouldPassWithValidUpdateFields() {
        // Given
        TestProduct product = new TestProduct();
        product.name = "Test Product";
        product.price = 39.99;
        product.supplierEmail = "supplier@example.com";

        // When/Then - should not throw
        entityValidator.validateForUpdate(product);
    }

    @Test
    void validate_withMultipleGroups_shouldApplyAllGroupValidations() {
        // Given
        TestProduct product = new TestProduct();
        product.name = "Test Product";
        // Missing initialCategory (Create), price (Create+Update), and invalid email (Update)
        product.supplierEmail = "invalid";

        // When
        Set<ConstraintViolation<TestProduct>> violations =
                validator.validate(product, Create.class, Update.class);

        // Then
        assertThat(violations).hasSize(3);
        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("initialCategory", "supplierEmail", "price");
    }

    @Test
    void validate_withNoGroups_shouldUseDefaultGroup() {
        // Given
        TestProduct product = new TestProduct();
        // Missing name (required in Default group)

        // When
        Set<ConstraintViolation<TestProduct>> violations = validator.validate(product);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    void validateProperty_shouldValidateSpecificField() {
        // Given
        TestProduct product = new TestProduct();
        product.name = "Test";
        product.initialCategory = null; // Invalid for Create group

        // When/Then
        assertThatThrownBy(() ->
                entityValidator.validateProperty(product, "initialCategory", Create.class))
                .isInstanceOf(ConstraintViolationException.class);
    }

    // Test entity with validation groups
    static class TestProduct {
        @NotBlank // Default group (always validated unless specific groups are used)
        String name;

        @NotBlank(groups = Create.class) // Required only on creation
        String initialCategory;

        @Email(groups = Update.class) // Validated only on update
        String supplierEmail;

        @NotNull(groups = {Create.class, Update.class}) // Required on both
        @Min(value = 0, groups = {Create.class, Update.class})
        Double price;
    }
}
