package io.springflow.core.mapper;

import io.springflow.annotations.AutoApi;
import io.springflow.annotations.Summary;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.MetadataResolver;
import jakarta.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelationshipProjectionTest {

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
    @SuppressWarnings("unchecked")
    void toOutputDto_withSummaryOnRelation_shouldProjectSummaryRegardlessOfDepth() {
        // Given
        Author author = new Author();
        author.setId(1L);
        author.setName("J.K. Rowling");
        author.setEmail("jk@rowling.com");

        Book book = new Book();
        book.setId(101L);
        book.setName("Harry Potter");
        book.setAuthor(author);

        EntityMetadata bookMetadata = metadataResolver.resolve(Book.class);
        EntityMetadata authorMetadata = metadataResolver.resolve(Author.class);
        
        // Depth 2 would normally allow full mapping of Author
        EntityDtoMapper<Book, Long> bookMapper = new EntityDtoMapper<>(Book.class, bookMetadata, entityManager, mapperFactory, 2);
        EntityDtoMapper<Author, Long> authorMapper = new EntityDtoMapper<>(Author.class, authorMetadata, entityManager, mapperFactory, 2);

        org.mockito.Mockito.lenient().when(mapperFactory.getMapper((Class) Author.class)).thenReturn(authorMapper);

        // When
        Map<String, Object> bookDto = bookMapper.toOutputDto(book);

        // Then
        Map<String, Object> authorResult = (Map<String, Object>) bookDto.get("author");
        assertThat(authorResult).isNotNull();
        assertThat(authorResult.get("name")).isEqualTo("J.K. Rowling");
        assertThat(authorResult.get("id")).isEqualTo(1L);
        // Email should NOT be present because it's not marked with @Summary, 
        // AND we requested a summary via @Summary on the Book.author field
        assertThat(authorResult).doesNotContainKey("email");
    }

    @Test
    @SuppressWarnings("unchecked")
    void toOutputDto_withSummaryOnRelation_butExplicitFields_shouldProjectRequestedFields() {
        // Given
        Author author = new Author();
        author.setId(1L);
        author.setName("J.K. Rowling");
        author.setEmail("jk@rowling.com");

        Book book = new Book();
        book.setId(101L);
        book.setName("Harry Potter");
        book.setAuthor(author);

        EntityMetadata bookMetadata = metadataResolver.resolve(Book.class);
        EntityMetadata authorMetadata = metadataResolver.resolve(Author.class);
        
        EntityDtoMapper<Book, Long> bookMapper = new EntityDtoMapper<>(Book.class, bookMetadata, entityManager, mapperFactory, 2);
        EntityDtoMapper<Author, Long> authorMapper = new EntityDtoMapper<>(Author.class, authorMetadata, entityManager, mapperFactory, 2);

        org.mockito.Mockito.lenient().when(mapperFactory.getMapper((Class) Author.class)).thenReturn(authorMapper);

        // When - Explicitly request author.email even if author field has @Summary
        Map<String, Object> bookDto = bookMapper.toOutputDto(book, List.of("id", "name", "author.id", "author.email"));

        // Then
        Map<String, Object> authorResult = (Map<String, Object>) bookDto.get("author");
        assertThat(authorResult).isNotNull();
        assertThat(authorResult.get("email")).isEqualTo("jk@rowling.com");
        assertThat(authorResult.get("id")).isEqualTo(1L);
        // Name should NOT be present because it wasn't requested in author.email (and explicit fields filter applied)
        assertThat(authorResult).doesNotContainKey("name");
    }

    @Entity
    @AutoApi
    static class Author {
        @Id private Long id;
        @Summary private String name;
        private String email;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    @Entity
    @AutoApi
    static class Book {
        @Id private Long id;
        private String name;
        
        @ManyToOne
        @Summary // This should force Author to be mapped as summary
        private Author author;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Author getAuthor() { return author; }
        public void setAuthor(Author author) { this.author = author; }
    }
}
