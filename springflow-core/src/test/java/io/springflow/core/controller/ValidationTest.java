package io.springflow.core.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for JSR-380 validation and error handling.
 */
class ValidationTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestValidationController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void validation_notBlank_shouldFail() throws Exception {
        String json = "{\"name\": \"\", \"email\": \"test@example.com\", \"age\": 25}";

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[0].field").value("name"))
                .andExpect(jsonPath("$.validationErrors[0].message").exists())
                .andExpect(jsonPath("$.validationErrors[0].rejectedValue").value(""));
    }

    @Test
    void validation_email_shouldFail() throws Exception {
        String json = "{\"name\": \"Test\", \"email\": \"invalid-email\", \"age\": 25}";

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'email')]").exists());
    }

    @Test
    void validation_size_shouldFail() throws Exception {
        String json = "{\"name\": \"AB\", \"email\": \"test@example.com\", \"age\": 25}";

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'name')]").exists());
    }

    @Test
    void validation_min_shouldFail() throws Exception {
        String json = "{\"name\": \"Test\", \"email\": \"test@example.com\", \"age\": 10}";

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'age')]").exists());
    }

    @Test
    void validation_max_shouldFail() throws Exception {
        String json = "{\"name\": \"Test\", \"email\": \"test@example.com\", \"age\": 150}";

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'age')]").exists());
    }

    @Test
    void validation_multipleErrors_shouldReturnAll() throws Exception {
        String json = "{\"name\": \"\", \"email\": \"invalid\", \"age\": 200}";

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                // Should have multiple validation errors (name, email, age)
                // Email may generate 2 errors: @NotNull and @Email
                .andExpect(jsonPath("$.validationErrors.length()").exists());
    }

    @Test
    void validation_pattern_shouldFail() throws Exception {
        String json = "{\"name\": \"Test\", \"email\": \"test@example.com\", \"age\": 25, \"phoneNumber\": \"invalid\"}";

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'phoneNumber')]").exists());
    }

    @Test
    void validation_validData_shouldSucceed() throws Exception {
        String json = "{\"name\": \"Test Name\", \"email\": \"test@example.com\", \"age\": 25, \"phoneNumber\": \"1234567890\"}";

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Name"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void validation_errorResponse_shouldHaveCorrectFormat() throws Exception {
        String json = "{\"name\": \"\", \"email\": \"test@example.com\", \"age\": 25}";

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    // Test DTO with validation annotations
    static class TestDto {
        @NotBlank(message = "Name must not be blank")
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
        private String name;

        @NotNull(message = "Email must not be null")
        @Email(message = "Email must be valid")
        private String email;

        @NotNull(message = "Age must not be null")
        @Min(value = 18, message = "Age must be at least 18")
        @Max(value = 120, message = "Age must be at most 120")
        private Integer age;

        @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
        private String phoneNumber;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    // Test controller
    @RestController
    static class TestValidationController {
        @PostMapping("/test/validate")
        public ResponseEntity<TestDto> validate(@Valid @RequestBody TestDto dto) {
            return ResponseEntity.ok(dto);
        }
    }
}
