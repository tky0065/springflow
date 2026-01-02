package io.springflow.core.mapper;

import io.springflow.annotations.AutoApi;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.MetadataResolver;
import jakarta.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelationshipMappingTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private DtoMapperFactory mapperFactory;

    private MetadataResolver metadataResolver;

    @BeforeEach
    void setUp() {
        metadataResolver = new MetadataResolver();
    }

    @Test
    void toEntity_withManyToOne_shouldResolveReference() {
        // Given
        Map<String, Object> inputDto = new HashMap<>();
        inputDto.put("name", "Test Book");
        inputDto.put("author", 1L);

        EntityMetadata metadata = metadataResolver.resolve(Book.class);
        EntityDtoMapper<Book, Long> mapper = new EntityDtoMapper<>(Book.class, metadata, entityManager, mapperFactory);

        Author authorRef = new Author();
        authorRef.setId(1L);
        when(entityManager.getReference(Author.class, 1L)).thenReturn(authorRef);

        // When
        Book book = mapper.toEntity(inputDto);

        // Then
        assertThat(book.getName()).isEqualTo("Test Book");
        assertThat(book.getAuthor()).isNotNull();
        assertThat(book.getAuthor().getId()).isEqualTo(1L);
    }

    @Test
    void toEntity_withNestedDto_shouldResolveNestedEntity() {
        // Given
        Map<String, Object> authorDto = new HashMap<>();
        authorDto.put("name", "J.K. Rowling");

        Map<String, Object> bookDto = new HashMap<>();
        bookDto.put("name", "Harry Potter");
        bookDto.put("author", authorDto);

        EntityMetadata bookMetadata = metadataResolver.resolve(Book.class);
        EntityMetadata authorMetadata = metadataResolver.resolve(Author.class);
        
        EntityDtoMapper<Book, Long> bookMapper = new EntityDtoMapper<>(Book.class, bookMetadata, entityManager, mapperFactory);
        EntityDtoMapper<Author, Long> authorMapper = new EntityDtoMapper<>(Author.class, authorMetadata, entityManager, mapperFactory);

        when(mapperFactory.getMapper((Class) Author.class)).thenReturn(authorMapper);

        // When
        Book book = bookMapper.toEntity(bookDto);

        // Then
        assertThat(book.getName()).isEqualTo("Harry Potter");
        assertThat(book.getAuthor()).isNotNull();
        assertThat(book.getAuthor().getName()).isEqualTo("J.K. Rowling");
    }

    @Entity
    @AutoApi
    static class Author {
        @Id private Long id;
        private String name;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @Entity
    @AutoApi
    static class Book {
        @Id private Long id;
        private String name;
        @ManyToOne private Author author;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Author getAuthor() { return author; }
        public void setAuthor(Author author) { this.author = author; }
    }
}
