# SpringFlow - Plan d'Implémentation

## Architecture du projet

```
springflow/
├── springflow-annotations/       # Module des annotations
│   └── src/main/java/io/springflow/annotations/
│       ├── AutoApi.java
│       ├── Filterable.java
│       ├── Hidden.java
│       └── SoftDelete.java
│
├── springflow-core/              # Logique principale
│   └── src/main/java/io/springflow/core/
│       ├── scanner/              # Scan des entités
│       ├── metadata/             # Métadonnées des entités
│       ├── generator/            # Génération de composants
│       ├── controller/           # Controllers génériques
│       ├── service/              # Services génériques
│       ├── filter/               # Système de filtres
│       ├── mapper/               # DTO mapping
│       └── config/               # Configuration
│
├── springflow-starter/           # Spring Boot Starter
│   ├── src/main/java/io/springflow/autoconfigure/
│   └── src/main/resources/
│       └── META-INF/spring.factories
│
├── springflow-demo/              # Application de démonstration
│   ├── springflow-demo-java/    # Demo Java
│   └── springflow-demo-kotlin/  # Demo Kotlin
│
├── springflow-ui/                # Admin UI (Phase 3)
│   └── admin-panel/
│
├── springflow-cli/               # CLI Tool (Phase 3)
│   └── src/main/java/io/springflow/cli/
│
└── springflow-docs/              # Documentation
    ├── getting-started.md
    ├── configuration.md
    ├── advanced.md
    └── examples/
```

---

## Phase 1 - MVP (Semaines 1-10)

### Module 1: Project Setup (Semaine 1)

#### Structure
```xml
<modules>
    <module>springflow-annotations</module>
    <module>springflow-core</module>
    <module>springflow-starter</module>
    <module>springflow-demo</module>
</modules>
```

#### Configuration Maven
- Parent POM avec dependency management
- Spring Boot 3.2+ comme base
- Java 17+ minimum
- Kotlin 1.9+ support

#### CI/CD
- GitHub Actions pour build
- SonarQube pour quality
- Maven Central pour publication

---

### Module 2: Annotations (Semaine 2)

#### `@AutoApi`
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoApi {
    String path() default "";
    Expose expose() default Expose.ALL;
    Security security() default @Security;
    boolean pagination() default true;
    boolean sorting() default true;
}
```

#### `@Filterable`
```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Filterable {
    FilterType[] types() default {FilterType.EQUALS};
    String paramName() default "";
}
```

#### Enums
- `Expose`: ALL, CREATE_UPDATE, READ_ONLY
- `FilterType`: EQUALS, LIKE, RANGE, IN, GREATER_THAN, LESS_THAN
- `SecurityLevel`: PUBLIC, AUTHENTICATED, ROLE_BASED

---

### Module 3: Entity Scanner (Semaine 3)

#### `EntityScanner.java`
```java
public class EntityScanner {
    private final ApplicationContext context;
    private final Map<Class<?>, EntityMetadata> cache;

    public List<Class<?>> scanEntities(String... basePackages);
    public boolean hasAutoApiAnnotation(Class<?> entityClass);
    public EntityMetadata extractMetadata(Class<?> entityClass);
}
```

#### Responsabilités
- Scanner le classpath pour `@Entity` + `@AutoApi`
- Utiliser Spring's `ClassPathScanningCandidateComponentProvider`
- Cacher les résultats pour performance
- Support scanning multi-packages

---

### Module 4: Metadata Resolver (Semaine 3-4)

#### `EntityMetadata.java`
```java
public class EntityMetadata {
    private Class<?> entityClass;
    private Class<?> idType;
    private String entityName;
    private String tableName;
    private Field idField;
    private List<FieldMetadata> fields;
    private AutoApi autoApiConfig;
    private Map<String, RelationMetadata> relations;
}
```

#### `FieldMetadata.java`
```java
public class FieldMetadata {
    private Field field;
    private String name;
    private Class<?> type;
    private boolean nullable;
    private boolean hidden;
    private Filterable filterConfig;
    private List<Annotation> validations;
}
```

#### Extraction
- ID field et type (via `@Id`)
- Validation annotations (JSR-380)
- Relations JPA (`@OneToMany`, etc.)
- Champs hidden (`@Hidden`)
- Configuration des filtres

---

### Module 5: Repository Generation (Semaine 4)

#### `RepositoryGenerator.java`
```java
public class RepositoryGenerator {
    public void generateRepository(EntityMetadata metadata) {
        // Créer bean definition pour JpaRepository<T, ID>
        // Enregistrer dans BeanDefinitionRegistry
        // Support JpaSpecificationExecutor pour filtres
    }
}
```

#### Approche
- Utiliser `GenericBeanDefinition`
- Étendre `JpaRepository<T, ID>`
- Implémenter `JpaSpecificationExecutor<T>` pour filtres
- Enregistrement dynamique via `BeanDefinitionRegistryPostProcessor`

---

### Module 6: Service Generation (Semaine 5)

#### `GenericCrudService.java`
```java
public abstract class GenericCrudService<T, ID> {
    protected JpaRepository<T, ID> repository;

    @Transactional(readOnly = true)
    public Page<T> findAll(Pageable pageable, Specification<T> spec);

    @Transactional(readOnly = true)
    public Optional<T> findById(ID id);

    @Transactional
    public T save(T entity);

    @Transactional
    public T update(ID id, T entity);

    @Transactional
    public void deleteById(ID id);
}
```

#### Génération
- Créer implémentation concrète par entité
- Injection automatique du repository
- Gestion des transactions
- Exception handling standardisé

---

### Module 7: Controller Generation (Semaine 6)

#### `GenericCrudController.java`
```java
@RestController
public abstract class GenericCrudController<T, ID> {
    protected GenericCrudService<T, ID> service;
    protected DtoMapper<T> mapper;

    @GetMapping
    public ResponseEntity<Page<OutputDTO>> findAll(Pageable pageable);

    @GetMapping("/{id}")
    public ResponseEntity<OutputDTO> findById(@PathVariable ID id);

    @PostMapping
    public ResponseEntity<OutputDTO> create(@Valid @RequestBody InputDTO dto);

    @PutMapping("/{id}")
    public ResponseEntity<OutputDTO> update(@PathVariable ID id, @Valid @RequestBody InputDTO dto);

    @PatchMapping("/{id}")
    public ResponseEntity<OutputDTO> partialUpdate(@PathVariable ID id, @RequestBody Map<String, Object> updates);

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id);
}
```

---

### Module 8: Pagination & Sorting (Semaine 7)

#### Configuration
```java
@Configuration
public class PageableConfiguration {
    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer customizer() {
        return resolver -> {
            resolver.setPageParameterName("page");
            resolver.setSizeParameterName("size");
            resolver.setMaxPageSize(100);
        };
    }
}
```

#### Query Parameters
- `?page=0&size=20` - Pagination
- `?sort=name,asc&sort=createdAt,desc` - Multi-field sorting
- Response format: Spring Data `Page<T>`

---

### Module 9: Controller Registration (Semaine 7)

#### `SpringFlowBeanDefinitionRegistrar.java`
```java
public class SpringFlowBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        EntityScanner scanner = new EntityScanner();
        List<Class<?>> entities = scanner.scanEntities();

        for (Class<?> entity : entities) {
            EntityMetadata meta = scanner.extractMetadata(entity);
            registerRepository(meta, registry);
            registerService(meta, registry);
            registerController(meta, registry);
        }
    }
}
```

---

### Module 10: Auto Configuration (Semaine 8)

#### `SpringFlowAutoConfiguration.java`
```java
@Configuration
@ConditionalOnClass(SpringFlowBeanDefinitionRegistrar.class)
@EnableConfigurationProperties(SpringFlowProperties.class)
public class SpringFlowAutoConfiguration {
    @Bean
    public EntityScanner entityScanner();

    @Bean
    public RepositoryGenerator repositoryGenerator();

    @Bean
    public ServiceGenerator serviceGenerator();

    @Bean
    public ControllerGenerator controllerGenerator();
}
```

#### `application.yml`
```yaml
springflow:
  enabled: true
  base-path: /api
  base-packages: com.example.entities
  pagination:
    default-size: 20
    max-size: 100
  swagger:
    enabled: true
```

---

### Module 11: OpenAPI Integration (Semaine 8)

#### Configuration
```java
@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI customOpenAPI(SpringFlowProperties props) {
        return new OpenAPI()
            .info(new Info()
                .title("Auto-Generated API")
                .version("1.0"))
            .components(generateSchemas());
    }
}
```

#### Génération automatique
- Schemas pour chaque entité
- Endpoints documentation
- Request/Response examples
- Validation constraints

---

### Module 12: DTO Generation (Semaine 9)

#### `DtoGenerator.java`
```java
public class DtoGenerator {
    public Class<?> generateInputDto(EntityMetadata metadata);
    public Class<?> generateOutputDto(EntityMetadata metadata);
}
```

#### InputDTO
- Tous les champs sauf ID
- Exclure champs `@Hidden`
- Inclure validations

#### OutputDTO
- Tous les champs incluant ID
- Exclure champs sensibles
- Support relations (lazy/eager)

#### Mapping
```java
public interface DtoMapper<T> {
    T toEntity(InputDTO dto);
    OutputDTO toDto(T entity);
    void updateEntity(T entity, InputDTO dto);
}
```

---

### Module 13: Validation (Semaine 9)

#### Support JSR-380
```java
public class InputDTO {
    @NotBlank
    private String name;

    @Email
    private String email;

    @Min(0)
    private Integer age;
}
```

#### Error Response
```json
{
  "timestamp": "2025-12-18T10:30:00Z",
  "status": 400,
  "errors": [
    {
      "field": "email",
      "message": "must be a valid email",
      "rejectedValue": "invalid"
    }
  ]
}
```

---

### Module 14: Kotlin Support (Semaine 10)

#### Data Class Example
```kotlin
@Entity
@AutoApi(path = "users")
data class User(
    @Id @GeneratedValue
    val id: Long? = null,

    @field:NotBlank
    val name: String,

    @field:Email
    val email: String?
)
```

#### Configuration
- Support nullable types
- Extension functions
- Coroutines (optionnel Phase 2)

---

### Module 15: Demo & Docs (Semaine 10)

#### Demo App
```java
@SpringBootApplication
@EnableSpringFlow(basePackages = "com.example.entities")
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

```java
@Entity
@AutoApi(path = "products")
public class Product {
    @Id @GeneratedValue
    private Long id;

    @NotBlank
    private String name;

    private BigDecimal price;
}
```

#### Documentation
- Quick Start (5 minutes)
- Configuration guide
- API reference
- Examples repository
- Troubleshooting

---

## Phase 2 - Advanced (Semaines 11-18)

### Module 16: Dynamic Filters (Semaines 11-12)

#### Implementation
```java
public class FilterResolver {
    public Specification<T> buildSpecification(Map<String, String> params, EntityMetadata metadata);
}
```

#### Query Examples
- `?name=John` → EQUALS
- `?name_like=Joh` → LIKE
- `?age_gte=18&age_lte=65` → RANGE
- `?status_in=ACTIVE,PENDING` → IN

---

### Module 17: Security (Semaine 13)

#### Configuration
```java
@AutoApi(
    path = "admin/users",
    security = @Security(
        enabled = true,
        roles = {"ADMIN", "MANAGER"}
    )
)
```

#### Integration
- Spring Security
- Method-level `@PreAuthorize`
- JWT support
- OAuth2 (optionnel)

---

### Module 18: Soft Delete (Semaine 15)

```java
@Entity
@AutoApi
@SoftDelete
public class User {
    private Boolean deleted = false;
    private LocalDateTime deletedAt;
}
```

#### Endpoints
- `DELETE /users/{id}` → Soft delete
- `POST /users/{id}/restore` → Restore
- `GET /users?includeDeleted=true` → Include deleted

---

## Phase 3 - Extended (Semaines 19-26)

### Module 21: GraphQL Support

```graphql
type Product {
  id: ID!
  name: String!
  price: Float
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

---

### Module 22: Admin UI

#### Stack
- React + TypeScript
- Material UI / Ant Design
- React Query
- Auto-generated CRUD forms

---

### Module 23: CLI Tool

```bash
springflow init my-project
springflow generate entity User name:string email:string
springflow generate module blog
```

---

## Estimation des efforts

| Phase | Modules | Semaines | Développeurs |
|-------|---------|----------|--------------|
| Phase 1 | 15 | 10 | 2-3 |
| Phase 2 | 5 | 6 | 2 |
| Phase 3 | 4 | 8 | 2-3 |

**Total**: 24 semaines avec 2-3 développeurs

---

**Dernière mise à jour**: 2025-12-18
