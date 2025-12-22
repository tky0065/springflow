package io.springflow.starter.openapi;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import io.swagger.v3.oas.models.media.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates OpenAPI schemas for entity DTOs based on EntityMetadata.
 * <p>
 * Creates separate schemas for input (POST/PUT) and output (GET) DTOs,
 * respecting @Hidden, @ReadOnly, and ID field annotations.
 * </p>
 */
public class SchemaGenerator {

    private static final Logger log = LoggerFactory.getLogger(SchemaGenerator.class);

    /**
     * Generate an output schema (GET responses).
     * Includes all fields except @Hidden.
     *
     * @param metadata the entity metadata
     * @return OpenAPI schema for output DTOs
     */
    public Schema<?> generateOutputSchema(EntityMetadata metadata) {
        log.debug("Generating output schema for {}", metadata.entityName());

        ObjectSchema schema = new ObjectSchema();
        schema.setType("object");
        schema.setName(metadata.entityName() + "Output");

        Map<String, Schema> properties = new LinkedHashMap<>();

        for (FieldMetadata field : metadata.fields()) {
            // Skip hidden fields
            if (field.hidden()) {
                continue;
            }

            Schema<?> fieldSchema = mapJavaTypeToSchema(field.type());
            fieldSchema.setDescription(buildFieldDescription(field));

            // Mark read-only fields and ID fields as readOnly
            if (field.readOnly() || field.isId()) {
                fieldSchema.setReadOnly(true);
            }

            // Apply validation constraints
            applyValidationConstraints(fieldSchema, field);

            // Set nullable
            if (field.nullable()) {
                fieldSchema.setNullable(true);
            }

            properties.put(field.name(), fieldSchema);
        }

        schema.setProperties(properties);
        return schema;
    }

    /**
     * Generate an input schema (POST/PUT request bodies).
     * Excludes ID, @Hidden, and @ReadOnly fields.
     *
     * @param metadata the entity metadata
     * @return OpenAPI schema for input DTOs
     */
    public Schema<?> generateInputSchema(EntityMetadata metadata) {
        log.debug("Generating input schema for {}", metadata.entityName());

        ObjectSchema schema = new ObjectSchema();
        schema.setType("object");
        schema.setName(metadata.entityName() + "Input");

        Map<String, Schema> properties = new LinkedHashMap<>();

        for (FieldMetadata field : metadata.fields()) {
            // Skip ID, hidden, and read-only fields
            if (field.isId() || field.hidden() || field.readOnly()) {
                continue;
            }

            Schema<?> fieldSchema = mapJavaTypeToSchema(field.type());
            fieldSchema.setDescription(buildFieldDescription(field));

            // Apply validation constraints
            applyValidationConstraints(fieldSchema, field);

            // Set nullable
            if (field.nullable()) {
                fieldSchema.setNullable(true);
            }

            properties.put(field.name(), fieldSchema);
        }

        schema.setProperties(properties);
        return schema;
    }

    /**
     * Map Java type to OpenAPI schema type.
     *
     * @param type the Java class type
     * @return corresponding OpenAPI schema
     */
    private Schema<?> mapJavaTypeToSchema(Class<?> type) {
        // String types
        if (type == String.class) {
            return new StringSchema();
        }

        // Integer types
        if (type == Integer.class || type == int.class) {
            return new IntegerSchema().format("int32");
        }
        if (type == Long.class || type == long.class) {
            return new IntegerSchema().format("int64");
        }
        if (type == Short.class || type == short.class) {
            return new IntegerSchema();
        }
        if (type == Byte.class || type == byte.class) {
            return new IntegerSchema();
        }

        // Number types
        if (type == Double.class || type == double.class) {
            return new NumberSchema().format("double");
        }
        if (type == Float.class || type == float.class) {
            return new NumberSchema().format("float");
        }
        if (type == BigDecimal.class) {
            return new NumberSchema();
        }

        // Boolean
        if (type == Boolean.class || type == boolean.class) {
            return new BooleanSchema();
        }

        // Date/Time types
        if (type == LocalDate.class) {
            return new DateSchema();
        }
        if (type == LocalDateTime.class || type == Date.class) {
            return new DateTimeSchema();
        }
        if (type == LocalTime.class) {
            return new StringSchema().format("time");
        }

        // Arrays/Collections
        if (type.isArray()) {
            ArraySchema arraySchema = new ArraySchema();
            arraySchema.setItems(mapJavaTypeToSchema(type.getComponentType()));
            return arraySchema;
        }

        // For collections and other complex types, use generic object
        if (Iterable.class.isAssignableFrom(type)) {
            return new ArraySchema().items(new ObjectSchema());
        }

        // Default: object type for unknown/complex types
        log.debug("Unknown type {}, using ObjectSchema", type.getName());
        return new ObjectSchema();
    }

    /**
     * Apply JSR-380 validation constraints to the schema.
     *
     * @param schema the schema to modify
     * @param field  the field metadata containing validation info
     */
    private void applyValidationConstraints(Schema<?> schema, FieldMetadata field) {
        if (field.validations() == null || field.validations().isEmpty()) {
            return;
        }

        // Process each validation annotation
        for (java.lang.annotation.Annotation annotation : field.validations()) {
            String annotationType = annotation.annotationType().getSimpleName();

            try {
                switch (annotationType) {
                    case "Size":
                        if (schema instanceof StringSchema stringSchema) {
                            int min = (int) annotation.annotationType().getMethod("min").invoke(annotation);
                            int max = (int) annotation.annotationType().getMethod("max").invoke(annotation);
                            if (min > 0) stringSchema.setMinLength(min);
                            if (max < Integer.MAX_VALUE) stringSchema.setMaxLength(max);
                        }
                        break;

                    case "Min":
                        if (schema instanceof NumberSchema numberSchema) {
                            long value = (long) annotation.annotationType().getMethod("value").invoke(annotation);
                            numberSchema.setMinimum(BigDecimal.valueOf(value));
                        }
                        break;

                    case "Max":
                        if (schema instanceof NumberSchema numberSchema) {
                            long value = (long) annotation.annotationType().getMethod("value").invoke(annotation);
                            numberSchema.setMaximum(BigDecimal.valueOf(value));
                        }
                        break;

                    case "Email":
                        if (schema instanceof StringSchema stringSchema) {
                            stringSchema.setFormat("email");
                        }
                        break;

                    case "Pattern":
                        if (schema instanceof StringSchema stringSchema) {
                            String pattern = (String) annotation.annotationType().getMethod("regexp").invoke(annotation);
                            stringSchema.setPattern(pattern);
                        }
                        break;
                }
            } catch (Exception e) {
                log.debug("Could not extract validation constraint from {}: {}", annotationType, e.getMessage());
            }
        }
    }

    /**
     * Build a description for a field based on its metadata.
     *
     * @param field the field metadata
     * @return description string
     */
    private String buildFieldDescription(FieldMetadata field) {
        StringBuilder description = new StringBuilder();

        // Add basic info
        description.append(field.type().getSimpleName()).append(" field");

        // Add validation info
        if (field.validations() != null && !field.validations().isEmpty()) {
            description.append(" with validations: ");
            for (java.lang.annotation.Annotation annotation : field.validations()) {
                description.append(annotation.annotationType().getSimpleName()).append(" ");
            }
        }

        return description.toString();
    }
}
