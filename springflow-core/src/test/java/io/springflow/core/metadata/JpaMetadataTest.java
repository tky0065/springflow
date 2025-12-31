package io.springflow.core.metadata;

import io.springflow.core.metadata.testentities.SpecSupportedEntity;
import io.springflow.core.metadata.testentities.ValidatedEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JpaMetadataTest {

    private MetadataResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new MetadataResolver();
    }

    @Test
    void shouldResolveSpecificationSupport() {
        EntityMetadata enabled = resolver.resolve(SpecSupportedEntity.class);
        EntityMetadata disabled = resolver.resolve(ValidatedEntity.class);

        assertThat(enabled.autoApiConfig().supportSpecification()).isTrue();
        assertThat(disabled.autoApiConfig().supportSpecification()).isFalse();

        assertThat(enabled.isSpecificationSupported()).isTrue();
        assertThat(disabled.isSpecificationSupported()).isFalse();
    }
}
