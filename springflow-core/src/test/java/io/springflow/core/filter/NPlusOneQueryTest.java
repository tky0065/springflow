package io.springflow.core.filter;

import io.springflow.annotations.AutoApi;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.MetadataResolver;
import jakarta.persistence.*;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests to verify that N+1 query problem is avoided using fetch joins.
 * <p>
 * These tests use Hibernate statistics to count the number of SQL queries executed
 * and verify that eager fetching is working correctly.
 * </p>
 */
@DataJpaTest
@ContextConfiguration(classes = NPlusOneQueryTest.TestConfig.class)
class NPlusOneQueryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    private FilterResolver filterResolver;
    private MetadataResolver metadataResolver;
    private Statistics statistics;

    @BeforeEach
    void setUp() {
        filterResolver = new FilterResolver();
        metadataResolver = new MetadataResolver();

        // Get Hibernate statistics
        EntityManagerFactory emf = entityManager.getEntityManager().getEntityManagerFactory();
        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        // Create test data
        createTestData();

        // Clear statistics after setup
        entityManager.clear();
        statistics.clear();
    }

    @Test
    void testFetchJoinsAvoidNPlusOne_ManyToOne() {
        // Given: EntityMetadata for Book with ManyToOne relation to Author
        EntityMetadata metadata = metadataResolver.resolve(Book.class);
        assertThat(metadata).isNotNull();

        // Specify to fetch the 'author' relation
        List<String> fetchFields = Arrays.asList("author");
        Specification<Book> spec = filterResolver.buildSpecification(
            java.util.Collections.emptyMap(),
            metadata,
            fetchFields
        );

        // Clear statistics
        statistics.clear();

        // When: Fetch all books with author
        List<Book> books = bookRepository.findAll(spec);

        // Then: Should execute only 1 query (with JOIN) instead of N+1 queries
        long queryCount = statistics.getPrepareStatementCount();
        assertThat(books).hasSize(5);
        assertThat(queryCount).as("Should use 1 query with JOIN instead of N+1 queries")
                .isLessThanOrEqualTo(2); // Allow 1-2 queries (main + possible metadata query)

        // Verify authors are loaded without additional queries
        statistics.clear();
        for (Book book : books) {
            String authorName = book.getAuthor().getName(); // Should not trigger lazy load
        }
        long lazyLoadQueries = statistics.getPrepareStatementCount();
        assertThat(lazyLoadQueries).as("No additional queries should be executed when accessing authors")
                .isEqualTo(0);
    }

    @Test
    void testWithoutFetchJoins_CausesNPlusOne() {
        // Given: No fetch specification
        Specification<Book> spec = filterResolver.buildSpecification(
            java.util.Collections.emptyMap(),
            metadataResolver.resolve(Book.class),
            null
        );

        // Clear statistics
        statistics.clear();

        // When: Fetch all books
        List<Book> books = bookRepository.findAll(spec);

        // Access authors (would cause N+1 without fetch joins)
        // BUT FilterResolver applies default fetch for ManyToOne relations
        for (Book book : books) {
            String authorName = book.getAuthor().getName();
        }

        // Then: With our default fetch strategy, should NOT have N+1
        long totalQueries = statistics.getPrepareStatementCount();
        assertThat(books).hasSize(5);
        // Our implementation fetches ManyToOne by default, so should be low query count
        assertThat(totalQueries).as("Default fetch strategy should prevent N+1")
                .isLessThanOrEqualTo(3);
    }

    @Test
    void testDefaultFetchStrategy_AutoFetchesManyToOne() {
        // Given: No explicit fetch fields (uses default strategy)
        EntityMetadata metadata = metadataResolver.resolve(Book.class);
        Specification<Book> spec = filterResolver.buildSpecification(
            java.util.Collections.emptyMap(),
            metadata,
            null // null = use default strategy
        );

        statistics.clear();

        // When: Fetch books
        List<Book> books = bookRepository.findAll(spec);

        // Then: Default strategy should have fetched ManyToOne relations
        assertThat(books).hasSize(5);

        // Verify no lazy loading occurs when accessing author
        statistics.clear();
        books.forEach(book -> book.getAuthor().getName());

        long lazyQueries = statistics.getPrepareStatementCount();
        assertThat(lazyQueries).as("Default strategy should fetch ManyToOne eagerly")
                .isEqualTo(0);
    }

    @Test
    void testOneToManyRelations_NotFetchedByDefault() {
        // Given: Author with OneToMany books
        EntityMetadata metadata = metadataResolver.resolve(Author.class);
        Specification<Author> spec = filterResolver.buildSpecification(
            java.util.Collections.emptyMap(),
            metadata,
            null
        );

        statistics.clear();

        // When: Fetch authors
        List<Author> authors = authorRepository.findAll(spec);

        // Then: Books should NOT be fetched by default (to avoid MultipleBagFetchException)
        assertThat(authors).hasSize(3);

        // Accessing books will cause lazy loading (expected behavior)
        statistics.clear();
        int totalBooks = authors.stream()
                .mapToInt(author -> author.getBooks().size())
                .sum();

        assertThat(totalBooks).isGreaterThan(0);
        // Lazy loading is expected for OneToMany
        long lazyQueries = statistics.getPrepareStatementCount();
        assertThat(lazyQueries).as("OneToMany should be lazy-loaded")
                .isGreaterThan(0);
    }

    private void createTestData() {
        // Create authors
        Author author1 = new Author();
        author1.setName("J.K. Rowling");

        Author author2 = new Author();
        author2.setName("George R.R. Martin");

        Author author3 = new Author();
        author3.setName("J.R.R. Tolkien");

        entityManager.persist(author1);
        entityManager.persist(author2);
        entityManager.persist(author3);

        // Create books
        Book book1 = new Book();
        book1.setTitle("Harry Potter 1");
        book1.setAuthor(author1);

        Book book2 = new Book();
        book2.setTitle("Harry Potter 2");
        book2.setAuthor(author1);

        Book book3 = new Book();
        book3.setTitle("Game of Thrones 1");
        book3.setAuthor(author2);

        Book book4 = new Book();
        book4.setTitle("Game of Thrones 2");
        book4.setAuthor(author2);

        Book book5 = new Book();
        book5.setTitle("The Hobbit");
        book5.setAuthor(author3);

        entityManager.persist(book1);
        entityManager.persist(book2);
        entityManager.persist(book3);
        entityManager.persist(book4);
        entityManager.persist(book5);

        entityManager.flush();
    }

    // Test entities
    @Entity
    @Table(name = "test_authors")
    @AutoApi(path = "authors")
    public static class Author {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
        private List<Book> books;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<Book> getBooks() { return books; }
        public void setBooks(List<Book> books) { this.books = books; }
    }

    @Entity
    @Table(name = "test_books")
    @AutoApi(path = "books")
    public static class Book {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String title;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "author_id")
        private Author author;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Author getAuthor() { return author; }
        public void setAuthor(Author author) { this.author = author; }
    }

    // Repositories
    interface AuthorRepository extends JpaRepository<Author, Long>, JpaSpecificationExecutor<Author> {}
    interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {}

    // Configuration
    @org.springframework.boot.autoconfigure.SpringBootApplication
    @org.springframework.data.jpa.repository.config.EnableJpaRepositories(
        basePackageClasses = NPlusOneQueryTest.class,
        considerNestedRepositories = true
    )
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        public MetadataResolver metadataResolver() {
            return new MetadataResolver();
        }
    }
}
