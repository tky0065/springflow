package io.springflow.core.mapper;

import io.springflow.annotations.Hidden;
import io.springflow.annotations.ReadOnly;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.MetadataResolver;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.springflow.annotations.Hidden;
import io.springflow.annotations.ReadOnly;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.MetadataResolver;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link EntityDtoMapper}.
 */
@ExtendWith(MockitoExtension.class)
class EntityDtoMapperTest {

    private MetadataResolver metadataResolver;
    private EntityDtoMapper<TestEntity, Long> mapper;
    private EntityMetadata metadata;

    @Mock
    private EntityManager entityManager;

    @Mock
    private DtoMapperFactory mapperFactory;

    @BeforeEach
    void setUp() {
        metadataResolver = new MetadataResolver();
        metadata = metadataResolver.resolve(TestEntity.class);
        mapper = new EntityDtoMapper<>(TestEntity.class, metadata, entityManager, mapperFactory);
    }

    @Test
    void toEntity_withRelation_shouldResolveReference() {
        // Given
        Map<String, Object> inputDto = new HashMap<>();
        inputDto.put("name", "Test Name");
        inputDto.put("relatedEntity", 42L);

        RelatedEntity mockRelated = new RelatedEntity();
        mockRelated.setId(42L);
        when(entityManager.getReference(eq(RelatedEntity.class), eq(42L))).thenReturn(mockRelated);

        // When
        TestEntity entity = mapper.toEntity(inputDto);

        // Then
        assertThat(entity.getRelatedEntity()).isNotNull();
        assertThat(entity.getRelatedEntity().getId()).isEqualTo(42L);
    }

    @Test
    void toOutputDto_withRelation_shouldMapToId() {
        // Given
        RelatedEntity related = new RelatedEntity();
        related.setId(42L);
        
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("Test Name");
        entity.setRelatedEntity(related);

        // When
        Map<String, Object> outputDto = mapper.toOutputDto(entity);

        // Then
        assertThat(outputDto.get("relatedEntity")).isEqualTo(42L);
    }

    @Test
    void toOutputDto_withCollectionRelation_shouldMapToIds() {
        // Given
        RelatedEntity related1 = new RelatedEntity();
        related1.setId(101L);
        RelatedEntity related2 = new RelatedEntity();
        related2.setId(102L);
        
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setRelatedList(Arrays.asList(related1, related2));

        // When
        Map<String, Object> outputDto = mapper.toOutputDto(entity);

        // Then
        assertThat(outputDto.get("relatedList")).isInstanceOf(List.class);
        List<Long> ids = (List<Long>) outputDto.get("relatedList");
        assertThat(ids).containsExactly(101L, 102L);
    }

    @Test
    void toEntity_shouldConvertInputDtoToEntity() {
        // Given
        Map<String, Object> inputDto = new HashMap<>();
        inputDto.put("name", "Test Name");
        inputDto.put("email", "test@example.com");
        inputDto.put("age", 25);

        // When
        TestEntity entity = mapper.toEntity(inputDto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Test Name");
        assertThat(entity.getEmail()).isEqualTo("test@example.com");
        assertThat(entity.getAge()).isEqualTo(25);
        assertThat(entity.getId()).isNull(); // ID should not be set from input
    }

    @Test
    void toEntity_shouldIgnoreIdField() {
        // Given
        Map<String, Object> inputDto = new HashMap<>();
        inputDto.put("id", 999L); // Should be ignored
        inputDto.put("name", "Test Name");

        // When
        TestEntity entity = mapper.toEntity(inputDto);

        // Then
        assertThat(entity.getId()).isNull(); // ID should not be set from input
    }

    @Test
    void toEntity_shouldIgnoreHiddenFields() {
        // Given
        Map<String, Object> inputDto = new HashMap<>();
        inputDto.put("name", "Test Name");
        inputDto.put("secretToken", "should-be-ignored");

        // When
        TestEntity entity = mapper.toEntity(inputDto);

        // Then
        assertThat(entity.getSecretToken()).isNull(); // Hidden field should not be set
    }

    @Test
    void toEntity_shouldIgnoreReadOnlyFields() {
        // Given
        Map<String, Object> inputDto = new HashMap<>();
        inputDto.put("name", "Test Name");
        inputDto.put("createdAt", "2025-01-01"); // ReadOnly field

        // When
        TestEntity entity = mapper.toEntity(inputDto);

        // Then
        assertThat(entity.getCreatedAt()).isNull(); // ReadOnly field should not be set
    }

    @Test
    void toEntity_shouldHandleTypeConversions() {
        // Given - age provided as String instead of Integer
        Map<String, Object> inputDto = new HashMap<>();
        inputDto.put("name", "Test Name");
        inputDto.put("age", "30");

        // When
        TestEntity entity = mapper.toEntity(inputDto);

        // Then
        assertThat(entity.getAge()).isEqualTo(30);
    }

    @Test
    void toOutputDto_shouldConvertEntityToOutputDto() {
        // Given
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("Test Name");
        entity.setEmail("test@example.com");
        entity.setAge(25);
        entity.setCreatedAt("2025-01-01");

        // When
        Map<String, Object> outputDto = mapper.toOutputDto(entity);

        // Then
        assertThat(outputDto).isNotNull();
        assertThat(outputDto.get("id")).isEqualTo(1L);
        assertThat(outputDto.get("name")).isEqualTo("Test Name");
        assertThat(outputDto.get("email")).isEqualTo("test@example.com");
        assertThat(outputDto.get("age")).isEqualTo(25);
        assertThat(outputDto.get("createdAt")).isEqualTo("2025-01-01");
    }

    @Test
    void toOutputDto_shouldExcludeHiddenFields() {
        // Given
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("Test Name");
        entity.setSecretToken("secret-value");

        // When
        Map<String, Object> outputDto = mapper.toOutputDto(entity);

        // Then
        assertThat(outputDto).doesNotContainKey("secretToken");
    }

    @Test
    void toOutputDto_shouldIncludeReadOnlyFields() {
        // Given
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("Test Name");
        entity.setCreatedAt("2025-01-01");

        // When
        Map<String, Object> outputDto = mapper.toOutputDto(entity);

        // Then
        assertThat(outputDto.get("createdAt")).isEqualTo("2025-01-01");
    }

    @Test
    void toOutputDto_shouldHandleNullEntity() {
        // When
        Map<String, Object> outputDto = mapper.toOutputDto(null);

        // Then
        assertThat(outputDto).isNull();
    }

    @Test
    void updateEntity_shouldUpdateExistingEntity() {
        // Given
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("Old Name");
        entity.setEmail("old@example.com");

        Map<String, Object> inputDto = new HashMap<>();
        inputDto.put("name", "New Name");
        inputDto.put("email", "new@example.com");

        // When
        mapper.updateEntity(entity, inputDto);

        // Then
        assertThat(entity.getId()).isEqualTo(1L); // ID should not change
        assertThat(entity.getName()).isEqualTo("New Name");
        assertThat(entity.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void updateEntity_shouldNotUpdateIdField() {
        // Given
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("Test Name");

        Map<String, Object> inputDto = new HashMap<>();
        inputDto.put("id", 999L); // Should be ignored
        inputDto.put("name", "Updated Name");

        // When
        mapper.updateEntity(entity, inputDto);

        // Then
        assertThat(entity.getId()).isEqualTo(1L); // ID should not change
    }

    @Test
    void updateEntity_shouldNotUpdateReadOnlyFields() {
        // Given
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setCreatedAt("2025-01-01");

        Map<String, Object> inputDto = new HashMap<>();
        inputDto.put("createdAt", "2025-12-31"); // Should be ignored

        // When
        mapper.updateEntity(entity, inputDto);

        // Then
        assertThat(entity.getCreatedAt()).isEqualTo("2025-01-01"); // Should not change
    }

    @Test
    void toOutputDtoList_shouldConvertListOfEntities() {
        // Given
        TestEntity entity1 = new TestEntity();
        entity1.setId(1L);
        entity1.setName("Entity 1");

        TestEntity entity2 = new TestEntity();
        entity2.setId(2L);
        entity2.setName("Entity 2");

        List<TestEntity> entities = Arrays.asList(entity1, entity2);

        // When
        List<Map<String, Object>> dtoList = mapper.toOutputDtoList(entities);

        // Then
        assertThat(dtoList).hasSize(2);
        assertThat(dtoList.get(0).get("id")).isEqualTo(1L);
        assertThat(dtoList.get(0).get("name")).isEqualTo("Entity 1");
        assertThat(dtoList.get(1).get("id")).isEqualTo(2L);
        assertThat(dtoList.get(1).get("name")).isEqualTo("Entity 2");
    }

    @Test
    void toOutputDtoList_shouldHandleNullList() {
        // When
        List<Map<String, Object>> dtoList = mapper.toOutputDtoList(null);

        // Then
        assertThat(dtoList).isEmpty();
    }

    @Test
    void toOutputDtoPage_shouldConvertPageOfEntities() {
        // Given
        TestEntity entity1 = new TestEntity();
        entity1.setId(1L);
        entity1.setName("Entity 1");

        TestEntity entity2 = new TestEntity();
        entity2.setId(2L);
        entity2.setName("Entity 2");

        List<TestEntity> entities = Arrays.asList(entity1, entity2);
        Page<TestEntity> entityPage = new PageImpl<>(entities, PageRequest.of(0, 10), 2);

        // When
        Page<Map<String, Object>> dtoPage = mapper.toOutputDtoPage(entityPage);

        // Then
        assertThat(dtoPage.getContent()).hasSize(2);
        assertThat(dtoPage.getTotalElements()).isEqualTo(2);
        assertThat(dtoPage.getNumber()).isEqualTo(0);
        assertThat(dtoPage.getSize()).isEqualTo(10);
    }

    @Test
    void toOutputDtoPage_shouldHandleNullPage() {
        // When
        Page<Map<String, Object>> dtoPage = mapper.toOutputDtoPage(null);

        // Then
        assertThat(dtoPage).isNotNull();
        assertThat(dtoPage.getContent()).isEmpty();
    }

    @Test
    void toOutputDto_withFields_shouldOnlyIncludeRequestedFields() {
        // Given
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("Test Name");
        entity.setEmail("test@example.com");
        entity.setAge(25);

        List<String> requestedFields = Arrays.asList("name", "age");

        // When
        Map<String, Object> outputDto = mapper.toOutputDto(entity, requestedFields);

        // Then
        assertThat(outputDto).hasSize(2);
        assertThat(outputDto.get("name")).isEqualTo("Test Name");
        assertThat(outputDto.get("age")).isEqualTo(25);
        assertThat(outputDto).doesNotContainKey("id");
        assertThat(outputDto).doesNotContainKey("email");
    }

    @Test
    void toOutputDtoPage_withFields_shouldOnlyIncludeRequestedFields() {
        // Given
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("Test Name");
        entity.setEmail("test@example.com");

        List<TestEntity> entities = Collections.singletonList(entity);
        Page<TestEntity> entityPage = new PageImpl<>(entities, PageRequest.of(0, 10), 1);
        List<String> requestedFields = Collections.singletonList("name");

        // When
        Page<Map<String, Object>> dtoPage = mapper.toOutputDtoPage(entityPage, requestedFields);

        // Then
        assertThat(dtoPage.getContent()).hasSize(1);
        Map<String, Object> dto = dtoPage.getContent().get(0);
        assertThat(dto).hasSize(1);
        assertThat(dto.get("name")).isEqualTo("Test Name");
        assertThat(dto).doesNotContainKey("id");
        assertThat(dto).doesNotContainKey("email");
    }

    @Test
    void getEntityClass_shouldReturnCorrectClass() {
        // When/Then
        assertThat(mapper.getEntityClass()).isEqualTo(TestEntity.class);
    }

    // Test entity for DTO mapping tests
    @Entity
    @io.springflow.annotations.AutoApi
    static class TestEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank
        @Size(min = 3, max = 50)
        private String name;

        private String email;

        private Integer age;

        @Hidden
        private String secretToken;

        @ReadOnly
        private String createdAt;

        @ManyToOne
        private RelatedEntity relatedEntity;

        @OneToMany
        private List<RelatedEntity> relatedList;

        // Getters and setters
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

        public String getSecretToken() {
            return secretToken;
        }

        public void setSecretToken(String secretToken) {
            this.secretToken = secretToken;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public RelatedEntity getRelatedEntity() {
            return relatedEntity;
        }

        public void setRelatedEntity(RelatedEntity relatedEntity) {
            this.relatedEntity = relatedEntity;
        }

        public List<RelatedEntity> getRelatedList() {
            return relatedList;
        }

        public void setRelatedList(List<RelatedEntity> relatedList) {
            this.relatedList = relatedList;
        }
    }

    @Entity
    static class RelatedEntity {
        @Id
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
