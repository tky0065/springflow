package io.springflow.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SpringFlow GraphQL support.
 * <p>
 * Tests the automatically generated GraphQL API for entities annotated with @AutoApi.
 * This includes:
 * - Query operations (findAll with pagination, findById)
 * - Mutation operations (create, update, delete)
 * - Schema generation and validation
 * </p>
 *
 * @author SpringFlow
 * @since 0.3.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class GraphQLIntegrationTest {

    @Autowired(required = false)
    private GraphQlTester graphQlTester;

    /**
     * Test that GraphQL is available (optional feature).
     * If springflow.graphql.enabled=false, this test is skipped.
     */
    @Test
    public void testGraphQLAvailability() {
        if (graphQlTester == null) {
            System.out.println("GraphQL is not enabled. Skipping GraphQL tests.");
            return;
        }

        assertThat(graphQlTester).isNotNull();
    }

    /**
     * Test GraphQL query: products (findAll with pagination).
     * <p>
     * Query example:
     * <pre>
     * query {
     *   products(page: 0, size: 5) {
     *     content {
     *       id
     *       name
     *       price
     *     }
     *     pageInfo {
     *       totalElements
     *       pageNumber
     *       pageSize
     *     }
     *   }
     * }
     * </pre>
     */
    @Test
    public void testQueryProductsWithPagination() {
        if (graphQlTester == null) {
            return; // Skip if GraphQL not enabled
        }

        String query = """
                query {
                  products(page: 0, size: 5) {
                    content {
                      id
                      name
                      price
                    }
                    pageInfo {
                      totalElements
                      pageNumber
                      pageSize
                    }
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("products.content")
                .entityList(Object.class)
                .satisfies(list -> assertThat(list).isNotNull())
                .path("products.pageInfo.pageNumber")
                .entity(Integer.class).isEqualTo(0)
                .path("products.pageInfo.pageSize")
                .entity(Integer.class).isEqualTo(5);
    }

    /**
     * Test GraphQL query: product (findById).
     * <p>
     * Query example:
     * <pre>
     * query {
     *   product(id: "1") {
     *     id
     *     name
     *     price
     *     description
     *   }
     * }
     * </pre>
     */
    @Test
    public void testQueryProductById() {
        if (graphQlTester == null) {
            return; // Skip if GraphQL not enabled
        }

        String query = """
                query {
                  product(id: "1") {
                    id
                    name
                    price
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("product")
                .entity(Object.class)
                .satisfies(product -> {
                    assertThat(product).isNotNull();
                });
    }

    /**
     * Test GraphQL mutation: createProduct.
     * <p>
     * Mutation example:
     * <pre>
     * mutation {
     *   createProduct(input: {
     *     name: "Test Product"
     *     price: 29.99
     *     description: "A test product"
     *   }) {
     *     id
     *     name
     *     price
     *   }
     * }
     * </pre>
     */
    @Test
    public void testMutationCreateProduct() {
        if (graphQlTester == null) {
            return; // Skip if GraphQL not enabled
        }

        String mutation = """
                mutation {
                  createProduct(input: {
                    name: "Test Product GraphQL"
                    price: 99.99
                    description: "Created via GraphQL mutation"
                  }) {
                    id
                    name
                    price
                  }
                }
                """;

        graphQlTester.document(mutation)
                .execute()
                .path("createProduct")
                .entity(Object.class)
                .satisfies(product -> {
                    assertThat(product).isNotNull();
                });
    }

    /**
     * Test GraphQL mutation: updateProduct.
     * <p>
     * Mutation example:
     * <pre>
     * mutation {
     *   updateProduct(id: "1", input: {
     *     name: "Updated Product"
     *     price: 39.99
     *   }) {
     *     id
     *     name
     *     price
     *   }
     * }
     * </pre>
     */
    @Test
    public void testMutationUpdateProduct() {
        if (graphQlTester == null) {
            return; // Skip if GraphQL not enabled
        }

        String mutation = """
                mutation {
                  updateProduct(id: "1", input: {
                    name: "Updated Product GraphQL"
                    price: 149.99
                  }) {
                    id
                    name
                    price
                  }
                }
                """;

        graphQlTester.document(mutation)
                .execute()
                .path("updateProduct")
                .entity(Object.class)
                .satisfies(product -> {
                    assertThat(product).isNotNull();
                });
    }

    /**
     * Test GraphQL mutation: deleteProduct.
     * <p>
     * Mutation example:
     * <pre>
     * mutation {
     *   deleteProduct(id: "999")
     * }
     * </pre>
     */
    @Test
    public void testMutationDeleteProduct() {
        if (graphQlTester == null) {
            return; // Skip if GraphQL not enabled
        }

        // First create a product to delete
        String createMutation = """
                mutation {
                  createProduct(input: {
                    name: "Product to Delete"
                    price: 1.00
                  }) {
                    id
                  }
                }
                """;

        String productId = graphQlTester.document(createMutation)
                .execute()
                .path("createProduct.id")
                .entity(String.class)
                .get();

        // Now delete it
        String deleteMutation = String.format("""
                mutation {
                  deleteProduct(id: "%s")
                }
                """, productId);

        graphQlTester.document(deleteMutation)
                .execute()
                .path("deleteProduct")
                .entity(Boolean.class)
                .isEqualTo(true);
    }

    /**
     * Test GraphQL query: categories (with self-referencing relation).
     */
    @Test
    public void testQueryCategories() {
        if (graphQlTester == null) {
            return; // Skip if GraphQL not enabled
        }

        String query = """
                query {
                  categories(page: 0, size: 10) {
                    content {
                      id
                      name
                    }
                    pageInfo {
                      totalElements
                    }
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("categories.content")
                .entityList(Object.class)
                .satisfies(list -> assertThat(list).isNotNull());
    }

    /**
     * Test GraphQL query with filters: products filtered by name (LIKE).
     * <p>
     * Demonstrates dynamic filtering with LIKE operation.
     * </p>
     */
    @Test
    public void testQueryProductsWithLikeFilter() {
        if (graphQlTester == null) {
            return; // Skip if GraphQL not enabled
        }

        String query = """
                query {
                  products(
                    page: 0,
                    size: 10,
                    filters: {
                      name_like: "Product"
                    }
                  ) {
                    content {
                      id
                      name
                      price
                    }
                    pageInfo {
                      totalElements
                    }
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("products.content")
                .entityList(Object.class)
                .satisfies(list -> assertThat(list).isNotNull());
    }

    /**
     * Test GraphQL query with filters: products filtered by price range.
     * <p>
     * Demonstrates range filtering with GTE and LTE operations.
     * </p>
     */
    @Test
    public void testQueryProductsWithRangeFilter() {
        if (graphQlTester == null) {
            return; // Skip if GraphQL not enabled
        }

        String query = """
                query {
                  products(
                    page: 0,
                    size: 10,
                    filters: {
                      price_gte: "10"
                      price_lte: "100"
                    }
                  ) {
                    content {
                      id
                      name
                      price
                    }
                    pageInfo {
                      totalElements
                    }
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("products.content")
                .entityList(Object.class)
                .satisfies(list -> assertThat(list).isNotNull());
    }

    /**
     * Test GraphQL query with multiple filters combined.
     * <p>
     * Demonstrates combining multiple filter criteria (name LIKE + price range).
     * </p>
     */
    @Test
    public void testQueryProductsWithMultipleFilters() {
        if (graphQlTester == null) {
            return; // Skip if GraphQL not enabled
        }

        String query = """
                query {
                  products(
                    page: 0,
                    size: 10,
                    filters: {
                      name_like: "Product"
                      price_gte: "0"
                    }
                  ) {
                    content {
                      id
                      name
                      price
                    }
                    pageInfo {
                      totalElements
                    }
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("products.content")
                .entityList(Object.class)
                .satisfies(list -> assertThat(list).isNotNull());
    }
}
