package io.springflow.annotations;

import org.junit.jupiter.api.Test;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SecuredApi} annotation.
 */
class SecuredApiTest {

    @SecuredApi(
        level = SecurityLevel.ROLE_BASED,
        roles = {"ADMIN"},
        authorities = {"user:write"},
        readLevel = SecurityLevel.AUTHENTICATED,
        writeLevel = SecurityLevel.ROLE_BASED
    )
    static class SecuredEntity {
    }

    @SecuredApi
    static class DefaultSecuredEntity {
    }

    @Test
    void securedApiShouldBePresent() {
        SecuredApi annotation = SecuredEntity.class.getAnnotation(SecuredApi.class);
        assertNotNull(annotation, "SecuredApi annotation should be present");
    }

    @Test
    void securedApiShouldHaveCustomValues() {
        SecuredApi annotation = SecuredEntity.class.getAnnotation(SecuredApi.class);

        assertEquals(SecurityLevel.ROLE_BASED, annotation.level());
        assertArrayEquals(new String[]{"ADMIN"}, annotation.roles());
        assertArrayEquals(new String[]{"user:write"}, annotation.authorities());
        assertEquals(SecurityLevel.AUTHENTICATED, annotation.readLevel());
        assertEquals(SecurityLevel.ROLE_BASED, annotation.writeLevel());
    }

    @Test
    void securedApiShouldHaveDefaultValues() {
        SecuredApi annotation = DefaultSecuredEntity.class.getAnnotation(SecuredApi.class);

        assertEquals(SecurityLevel.AUTHENTICATED, annotation.level());
        assertEquals(0, annotation.roles().length);
        assertEquals(0, annotation.authorities().length);
        assertEquals(SecurityLevel.UNDEFINED, annotation.readLevel());
        assertEquals(SecurityLevel.UNDEFINED, annotation.writeLevel());
    }

    @Test
    void shouldBeApplicableToTypes() {
        Target target = SecuredApi.class.getAnnotation(Target.class);
        assertNotNull(target);
        boolean foundType = false;
        for (ElementType type : target.value()) {
            if (type == ElementType.TYPE) {
                foundType = true;
                break;
            }
        }
        assertTrue(foundType, "SecuredApi should be applicable to types");
    }

    @Test
    void shouldHaveRuntimeRetention() {
        Retention retention = SecuredApi.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }
}
