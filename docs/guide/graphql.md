# GraphQL Support

SpringFlow provides automatic GraphQL API generation for your JPA entities. Simply enable the GraphQL feature and SpringFlow will generate a complete GraphQL schema with queries and mutations.

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-graphql</artifactId>
    <version>0.4.1</version>
</dependency>
```

### 2. Enable GraphQL

In your `application.yml`:

```yaml
springflow:
  graphql:
    enabled: true
    schema-location: src/main/resources/graphql
    graphiql-enabled: true
    introspection-enabled: true

spring:
  graphql:
    graphiql:
      enabled: true
      path: /graphiql
    path: /graphql
```

### 3. That's It!

SpringFlow will automatically generate:
- GraphQL types for your entities
- Input types for create/update operations
- Query resolvers (`findAll`, `findById`)
- Mutation resolvers (`create`, `update`, `delete`)
- Pagination support

## Generated GraphQL Schema

For an entity like this:

```java
@Entity
@AutoApi(path = "/products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    private Double price;

    @ReadOnly
    private LocalDateTime createdAt;
}
```

SpringFlow generates:

```graphql
type Product {
  id: ID!
  name: String!
  price: Float!
  createdAt: String
}

input ProductInput {
  name: String!
  price: Float!
}

type ProductPage {
  content: [Product!]!
  pageInfo: PageInfo!
}

type PageInfo {
  pageNumber: Int!
  pageSize: Int!
  totalElements: Int!
  totalPages: Int!
  hasNext: Boolean!
  hasPrevious: Boolean!
}

type Query {
  products(page: Int, size: Int): ProductPage!
  product(id: ID!): Product
}

type Mutation {
  createProduct(input: ProductInput!): Product!
  updateProduct(id: ID!, input: ProductInput!): Product!
  deleteProduct(id: ID!): Boolean!
}
```

## Query Examples

### Find All with Pagination

```graphql
query {
  products(page: 0, size: 10) {
    content {
      id
      name
      price
      createdAt
    }
    pageInfo {
      totalElements
      totalPages
      hasNext
    }
  }
}
```

### Find All with Filters

SpringFlow GraphQL supports dynamic filtering using the same filter syntax as the REST API:

```graphql
query {
  products(
    page: 0,
    size: 10,
    filters: {
      name_like: "Laptop"
      price_gte: "500"
      price_lte: "2000"
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
```

**Available Filter Operations:**
- `field`: Equals (e.g., `category: "Electronics"`)
- `field_like`: Contains/LIKE (e.g., `name_like: "Laptop"`)
- `field_gt`: Greater than (e.g., `price_gt: "100"`)
- `field_gte`: Greater than or equal (e.g., `price_gte: "100"`)
- `field_lt`: Less than (e.g., `price_lt: "1000"`)
- `field_lte`: Less than or equal (e.g., `price_lte: "1000"`)
- `field_in`: In list (e.g., `category_in: "Electronics,Computers"`)
- `field_not_in`: Not in list (e.g., `status_not_in: "DELETED,ARCHIVED"`)
- `field_null`: Is null (e.g., `deletedAt_null: "true"`)
- `field_between`: Between (e.g., `price_between: "100,500"`)

**Multiple Filters:**
All filters are combined with AND logic:

```graphql
query {
  products(
    filters: {
      name_like: "Laptop"
      price_gte: "500"
      price_lte: "2000"
      category: "Electronics"
    }
  ) {
    content { id, name, price }
  }
}
```

### Find by ID

```graphql
query {
  product(id: "1") {
    id
    name
    price
    description
  }
}
```

## Mutation Examples

### Create

```graphql
mutation {
  createProduct(input: {
    name: "Laptop"
    price: 999.99
  }) {
    id
    name
    price
  }
}
```

### Update

```graphql
mutation {
  updateProduct(id: "1", input: {
    name: "Gaming Laptop"
    price: 1499.99
  }) {
    id
    name
    price
  }
}
```

### Delete

```graphql
mutation {
  deleteProduct(id: "1")
}
```

## Field Annotations

GraphQL generation respects SpringFlow field annotations:

### @Hidden

Fields marked as `@Hidden` are excluded from both types and input types:

```java
@Hidden
private String internalCode;
```

### @ReadOnly

Fields marked as `@ReadOnly` are included in output types but excluded from input types:

```java
@ReadOnly
private LocalDateTime createdAt;
```

### ID Fields

ID fields are automatically excluded from input types:

```java
@Id
@GeneratedValue
private Long id;
```

## Configuration Options

```yaml
springflow:
  graphql:
    # Enable/disable GraphQL support
    enabled: true

    # Where to write the generated schema.graphqls file
    schema-location: src/main/resources/graphql

    # Enable GraphiQL UI for testing
    graphiql-enabled: true

    # Enable introspection (disable in production for security)
    introspection-enabled: true
```

## GraphiQL Interface

Access the GraphiQL interface at `/graphiql` to explore and test your GraphQL API:

```
http://localhost:8080/graphiql
```

GraphiQL provides:
- Interactive query editor with syntax highlighting
- Auto-completion
- Schema documentation
- Query history
- Variable support

## Type Mappings

SpringFlow automatically maps Java types to GraphQL types:

| Java Type | GraphQL Type |
|-----------|--------------|
| `String` | `String` |
| `Integer`, `int` | `Int` |
| `Long`, `long` | `ID` |
| `Double`, `double`, `Float`, `float` | `Float` |
| `Boolean`, `boolean` | `Boolean` |
| `LocalDate`, `LocalDateTime` | `String` (ISO-8601) |

## Pagination

All generated `findAll` queries support pagination:

```graphql
query {
  products(page: 0, size: 20) {
    content {
      id
      name
    }
    pageInfo {
      pageNumber      # Current page number
      pageSize        # Items per page
      totalElements   # Total items across all pages
      totalPages      # Total number of pages
      hasNext         # Is there a next page?
      hasPrevious     # Is there a previous page?
    }
  }
}
```

Default values:
- `page`: 0
- `size`: 20

## Validation

Input validation is automatically enforced based on JSR-380 annotations:

```java
@NotBlank
@Size(min = 3, max = 100)
private String name;

@NotNull
@Min(0)
private Double price;
```

If validation fails, GraphQL returns an error:

```json
{
  "errors": [
    {
      "message": "Validation failed: name must not be blank",
      "path": ["createProduct"]
    }
  ]
}
```

## Security

GraphQL respects Spring Security configuration. If Spring Security is enabled and configured via `@AutoApi(security = ...)`, the same authorization rules apply to GraphQL operations.

**Production Recommendation:**
- Set `introspection-enabled: false` in production for security
- Configure appropriate authentication/authorization

## Testing GraphQL

Use `GraphQlTester` for integration testing:

```java
@SpringBootTest
class GraphQLTest {

    @Autowired
    GraphQlTester graphQlTester;

    @Test
    void testQuery() {
        String query = """
            query {
              products(page: 0, size: 5) {
                content {
                  id
                  name
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
```

## DataLoader for N+1 Problem

SpringFlow GraphQL automatically configures DataLoaders to solve the N+1 query problem when loading related entities.

### What is the N+1 Problem?

When loading a list of entities with related data, naive implementations can execute N+1 database queries:
- 1 query to load the parent entities
- N queries to load related entities (one per parent)

**Example without DataLoader:**
```
SELECT * FROM Category WHERE ...     -- 1 query
SELECT * FROM Product WHERE category_id = 1  -- Query 1
SELECT * FROM Product WHERE category_id = 2  -- Query 2
... (N more queries)
```

### How DataLoader Solves This

DataLoader batches multiple fetch requests into a single database query:

**Example with DataLoader:**
```
SELECT * FROM Category WHERE ...  -- 1 query
SELECT * FROM Product WHERE category_id IN (1, 2, 3, ...)  -- 1 batched query
```

### Automatic Configuration

SpringFlow automatically registers a DataLoader for each entity annotated with `@AutoApi`. No additional configuration required!

When GraphQL resolves entity relationships, SpringFlow's DataLoader:
1. Collects all IDs that need to be loaded
2. Batches them into a single `findAllById()` query
3. Returns entities in the correct order
4. Dramatically improves query performance

### Performance Comparison

```
Without DataLoader:
  Loading 100 categories with products: 101 queries (1 + 100)

With DataLoader:
  Loading 100 categories with products: 2 queries (1 + 1 batched)
```

### Implementation Details

DataLoaders are automatically registered at application startup via the `DataLoaderRegistrar` component:

```java
@Bean
public DataLoaderRegistrar dataLoaderRegistrar(ApplicationContext applicationContext) {
    return new DataLoaderRegistrar(applicationContext);
}
```

Each entity gets its own DataLoader named `{entityName}Loader` (e.g., `productLoader`, `categoryLoader`).

## Relation Loading (Future Enhancement)

Automatic field resolvers for entity relationships are planned for a future release. This would enable GraphQL queries like:

```graphql
query {
  products(page: 0, size: 10) {
    content {
      id
      name
      category {    # Related entity
        id
        name
      }
    }
  }
}

query {
  categories {
    content {
      id
      name
      products {    # One-to-many relation
        id
        name
        price
      }
      children {    # Self-referencing relation
        id
        name
      }
    }
  }
}
```

**Current Workaround:**

For now, you can fetch related entities using separate queries and the DataLoader will batch the requests:

```graphql
query {
  products(page: 0, size: 10) {
    content {
      id
      name
      # Fetch category IDs manually
    }
  }
}

# Then fetch categories by IDs (batched by DataLoader)
query {
  category(id: "1") { id, name }
}
```

**Implementation Complexity:**

Full relation support requires:
- Schema generation for relation fields
- Field resolvers (@SchemaMapping) for each relation type
- Circular reference handling (e.g., Category ↔ Products)
- Nested DataLoader orchestration
- Lazy vs eager loading strategies

This is planned for version 0.4.0 or later.

## Future Enhancements

Planned for future releases:
- **Relation Field Resolvers**: Automatic GraphQL field resolvers for JPA relationships (v0.4.0+)
- **Subscriptions**: Real-time updates via GraphQL subscriptions
- **Custom DataLoaders**: Support for custom batching strategies
- **Query Complexity Analysis**: Prevent overly complex queries
- **Persisted Queries**: Support for persisted/automatic query caching

## Best Practices

1. **Enable Only When Needed**: GraphQL is opt-in via configuration
2. **Disable Introspection in Production**: Prevents schema discovery
3. **Use Pagination**: Always paginate large result sets
4. **Validate Input**: Rely on JSR-380 annotations for validation
5. **Test with GraphiQL**: Use the interactive UI during development

## Troubleshooting

### GraphQL Not Available

Ensure GraphQL is enabled:

```yaml
springflow:
  graphql:
    enabled: true
```

### Schema Not Generated

Check logs for schema generation errors. The schema file should be created at the configured `schema-location`.

### GraphiQL Not Loading

Verify Spring GraphQL configuration:

```yaml
spring:
  graphql:
    graphiql:
      enabled: true
      path: /graphiql
```

Access at: `http://localhost:8080/graphiql`

## See Also

- [User Guide](index.md)
- [Annotations Reference](annotations.md)
- [Configuration Reference](configuration.md)
