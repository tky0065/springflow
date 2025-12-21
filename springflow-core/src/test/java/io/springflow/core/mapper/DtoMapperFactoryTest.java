package io.springflow.core.mapper;

import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.MetadataResolver;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DtoMapperFactory}.
 */
class DtoMapperFactoryTest {

    private DtoMapperFactory factory;
    private MetadataResolver metadataResolver;

    @BeforeEach
    void setUp() {
        factory = new DtoMapperFactory();
        metadataResolver = new MetadataResolver();
    }

    @Test
    void getMapper_shouldCreateMapper() {
        // Given
        EntityMetadata metadata = metadataResolver.resolve(TestEntity.class);

        // When
        DtoMapper<TestEntity, Long> mapper = factory.getMapper(TestEntity.class, metadata);

        // Then
        assertThat(mapper).isNotNull();
        assertThat(mapper).isInstanceOf(EntityDtoMapper.class);
        assertThat(mapper.getEntityClass()).isEqualTo(TestEntity.class);
    }

    @Test
    void getMapper_shouldCacheMappers() {
        // Given
        EntityMetadata metadata = metadataResolver.resolve(TestEntity.class);

        // When
        DtoMapper<TestEntity, Long> mapper1 = factory.getMapper(TestEntity.class, metadata);
        DtoMapper<TestEntity, Long> mapper2 = factory.getMapper(TestEntity.class, metadata);

        // Then - Should return the same instance
        assertThat(mapper1).isSameAs(mapper2);
        assertThat(factory.getCacheSize()).isEqualTo(1);
    }

    @Test
    void getMapper_shouldCreateDifferentMappersForDifferentEntities() {
        // Given
        EntityMetadata metadata1 = metadataResolver.resolve(TestEntity.class);
        EntityMetadata metadata2 = metadataResolver.resolve(AnotherEntity.class);

        // When
        DtoMapper<TestEntity, Long> mapper1 = factory.getMapper(TestEntity.class, metadata1);
        DtoMapper<AnotherEntity, Long> mapper2 = factory.getMapper(AnotherEntity.class, metadata2);

        // Then
        assertThat(mapper1).isNotSameAs(mapper2);
        assertThat(factory.getCacheSize()).isEqualTo(2);
    }

    @Test
    void clearCache_shouldRemoveAllMappers() {
        // Given
        EntityMetadata metadata = metadataResolver.resolve(TestEntity.class);
        factory.getMapper(TestEntity.class, metadata);
        assertThat(factory.getCacheSize()).isEqualTo(1);

        // When
        factory.clearCache();

        // Then
        assertThat(factory.getCacheSize()).isEqualTo(0);
    }

    @Test
    void getCacheSize_shouldReturnCorrectSize() {
        // Given
        EntityMetadata metadata1 = metadataResolver.resolve(TestEntity.class);
        EntityMetadata metadata2 = metadataResolver.resolve(AnotherEntity.class);

        // When
        factory.getMapper(TestEntity.class, metadata1);
        factory.getMapper(AnotherEntity.class, metadata2);
        factory.getMapper(TestEntity.class, metadata1); // Should use cache

        // Then
        assertThat(factory.getCacheSize()).isEqualTo(2);
    }

    @Entity
    @io.springflow.annotations.AutoApi
    static class TestEntity {
        @Id
        @GeneratedValue
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Entity
    @io.springflow.annotations.AutoApi
    static class AnotherEntity {
        @Id
        @GeneratedValue
        private Long id;
        private String description;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
