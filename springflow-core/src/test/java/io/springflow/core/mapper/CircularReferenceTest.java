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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for circular reference handling in DTO mapping.
 * <p>
 * Verifies that circular relationships (e.g., Author -> Books -> Author)
 * are handled correctly with depth limiting to prevent infinite recursion.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class CircularReferenceTest {

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
    void toOutputDto_withCircularReference_shouldLimitDepth() {
        // Given: Author with Books, each Book references back to Author (circular)
        Author author = new Author();
        author.setId(1L);
        author.setName("J.K. Rowling");

        Book book1 = new Book();
        book1.setId(101L);
        book1.setTitle("Harry Potter and the Philosopher's Stone");
        book1.setAuthor(author); // Circular reference: Book -> Author

        Book book2 = new Book();
        book2.setId(102L);
        book2.setTitle("Harry Potter and the Chamber of Secrets");
        book2.setAuthor(author); // Circular reference: Book -> Author

        List<Book> books = new ArrayList<>();
        books.add(book1);
        books.add(book2);
        author.setBooks(books); // Author -> Books -> Author (circular)

        // Create mapper for Author
        EntityMetadata authorMetadata = metadataResolver.resolve(Author.class);
        EntityDtoMapper<Author, Long> authorMapper = new EntityDtoMapper<>(
            Author.class,
            authorMetadata,
            entityManager,
            mapperFactory
        );

        // When: Map author to DTO
        Map<String, Object> authorDto = authorMapper.toOutputDto(author);

        // Then: Should map successfully without infinite recursion
        assertThat(authorDto).isNotNull();
        assertThat(authorDto.get("id")).isEqualTo(1L);
        assertThat(authorDto.get("name")).isEqualTo("J.K. Rowling");

        // Books should be mapped to IDs only (depth limit prevents full nesting)
        assertThat(authorDto.get("books")).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<Long> booksList = (List<Long>) authorDto.get("books");
        assertThat(booksList).hasSize(2);

        // At depth limit, books should be represented as IDs only
        assertThat(booksList.get(0)).isInstanceOf(Long.class);
        assertThat(booksList.get(1)).isInstanceOf(Long.class);
        assertThat(booksList).containsExactlyInAnyOrder(101L, 102L);
    }

    @Test
    void toOutputDto_withSelfReference_shouldLimitDepth() {
        // Given: Category with parent/children self-referencing structure
        Category root = new Category();
        root.setId(1L);
        root.setName("Root Category");

        Category child1 = new Category();
        child1.setId(2L);
        child1.setName("Child Category 1");
        child1.setParent(root);

        Category child2 = new Category();
        child2.setId(3L);
        child2.setName("Child Category 2");
        child2.setParent(root);

        List<Category> children = new ArrayList<>();
        children.add(child1);
        children.add(child2);
        root.setChildren(children);

        // Create mapper for Category
        EntityMetadata categoryMetadata = metadataResolver.resolve(Category.class);
        EntityDtoMapper<Category, Long> categoryMapper = new EntityDtoMapper<>(
            Category.class,
            categoryMetadata,
            entityManager,
            mapperFactory
        );

        // When: Map root category to DTO
        Map<String, Object> categoryDto = categoryMapper.toOutputDto(root);

        // Then: Should handle self-reference without infinite recursion
        assertThat(categoryDto).isNotNull();
        assertThat(categoryDto.get("id")).isEqualTo(1L);
        assertThat(categoryDto.get("name")).isEqualTo("Root Category");
        assertThat(categoryDto.get("parent")).isNull(); // Root has no parent

        // Children should be mapped to IDs only (depth limit)
        assertThat(categoryDto.get("children")).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<Long> childrenList = (List<Long>) categoryDto.get("children");
        assertThat(childrenList).hasSize(2);
        assertThat(childrenList.get(0)).isInstanceOf(Long.class);
        assertThat(childrenList.get(1)).isInstanceOf(Long.class);
        assertThat(childrenList).containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    void toOutputDto_deepNesting_shouldRespectMaxDepth() {
        // Given: A deeply nested structure
        Author author = new Author();
        author.setId(1L);
        author.setName("Author");

        Book book = new Book();
        book.setId(100L);
        book.setTitle("Book");
        book.setAuthor(author);

        List<Book> books = new ArrayList<>();
        books.add(book);
        author.setBooks(books);

        // Create mapper
        EntityMetadata metadata = metadataResolver.resolve(Author.class);
        EntityDtoMapper<Author, Long> mapper = new EntityDtoMapper<>(
            Author.class,
            metadata,
            entityManager,
            mapperFactory
        );

        // When: Map with default depth
        Map<String, Object> dto = mapper.toOutputDto(author);

        // Then: Should limit depth to prevent deep nesting
        assertThat(dto).isNotNull();
        assertThat(dto.get("books")).isInstanceOf(List.class);

        // Books should be represented as IDs at depth limit
        List<?> booksList = (List<?>) dto.get("books");
        assertThat(booksList.get(0)).isInstanceOf(Long.class);
        assertThat(booksList.get(0)).isEqualTo(100L);
    }

    // Test entities for circular reference tests

    @Entity
    @Table(name = "test_authors_circular")
    @AutoApi
    static class Author {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
        private List<Book> books;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<Book> getBooks() { return books; }
        public void setBooks(List<Book> books) { this.books = books; }
    }

    @Entity
    @Table(name = "test_books_circular")
    @AutoApi
    static class Book {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String title;

        @ManyToOne
        @JoinColumn(name = "author_id")
        private Author author;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Author getAuthor() { return author; }
        public void setAuthor(Author author) { this.author = author; }
    }

    @Entity
    @Table(name = "test_categories_self_ref")
    @AutoApi
    static class Category {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        @ManyToOne
        @JoinColumn(name = "parent_id")
        private Category parent;

        @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
        private List<Category> children;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Category getParent() { return parent; }
        public void setParent(Category parent) { this.parent = parent; }
        public List<Category> getChildren() { return children; }
        public void setChildren(List<Category> children) { this.children = children; }
    }
}
