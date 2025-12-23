package io.springflow.core.service;

import io.springflow.annotations.Auditable;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import io.springflow.core.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditTrailTest {

    @Mock
    private JpaRepository<TestEntity, Long> repository;

    @Mock
    private Auditable auditable;

    private GenericCrudService<TestEntity, Long> service;
    private EntityMetadata metadata;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(auditable.createdAtField()).thenReturn("createdAt");
        lenient().when(auditable.updatedAtField()).thenReturn("updatedAt");
        lenient().when(auditable.createdByField()).thenReturn("createdBy");
        lenient().when(auditable.updatedByField()).thenReturn("updatedBy");

        FieldMetadata createdAt = new FieldMetadata(
                TestEntity.class.getDeclaredField("createdAt"),
                "createdAt", java.time.LocalDateTime.class, true, false, false, false, null,
                Collections.emptyList(), null, null
        );
        FieldMetadata createdBy = new FieldMetadata(
                TestEntity.class.getDeclaredField("createdBy"),
                "createdBy", String.class, true, false, false, false, null,
                Collections.emptyList(), null, null
        );

        metadata = new EntityMetadata(
                TestEntity.class, Long.class, "TestEntity", "test_entity", null, null, auditable,
                Arrays.asList(createdAt, createdBy)
        );

        service = new GenericCrudService<TestEntity, Long>(repository, TestEntity.class, metadata) {
        };
    }

    @Test
    void save_shouldApplyAuditing() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("test-user"));

            // Given
            TestEntity entity = new TestEntity();
            when(repository.save(entity)).thenReturn(entity);

            // When
            service.save(entity);

            // Then
            assertThat(entity.getCreatedBy()).isEqualTo("test-user");
            assertThat(entity.getCreatedAt()).isNotNull();
            verify(repository).save(entity);
        }
    }

    static class TestEntity {
        private Long id;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    }
}
