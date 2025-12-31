package io.springflow.annotations;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JpaSpecificationSupportTest {

    @AutoApi(supportSpecification = true)
    static class SpecEnabledEntity {}

    @AutoApi
    static class SpecDisabledEntity {}

    @Test
    void shouldHaveSupportSpecificationAttribute() {
        AutoApi enabled = SpecEnabledEntity.class.getAnnotation(AutoApi.class);
        AutoApi disabled = SpecDisabledEntity.class.getAnnotation(AutoApi.class);

        assertTrue(enabled.supportSpecification(), "Should be enabled when explicitly set");
        assertFalse(disabled.supportSpecification(), "Should be disabled by default");
    }
}
