# Best Practices

Recommandations et patterns pour utiliser SpringFlow efficacement.

## Design Patterns

### 1. Une Entité = Une Ressource REST

```java
// ✅ Bon
@Entity
@AutoApi(path = "products")
public class Product { ... }

// ❌ Éviter : entités internes
@Entity  // Pas de @AutoApi
public class ProductAuditLog { ... }
```

### 2. Utiliser @Hidden pour Données Sensibles

```java
@Entity
@AutoApi(path = "users")
public class User {
    @Hidden
    private String password;  // Jamais exposé dans l'API
    
    @Hidden
    private String apiToken;
}
```

### 3. @ReadOnly pour Champs Calculés

```java
@Entity
@AutoApi(path = "orders")
public class Order {
    @ReadOnly
    private BigDecimal totalAmount;  // Calculé, pas modifiable
    
    @ReadOnly
    private LocalDateTime createdAt;
}
```

## Validation

### Valider au Bon Niveau

```java
@Entity
@AutoApi(path = "users")
public class User {
    @NotBlank(message = "Name required")
    @Size(min = 3, max = 50)
    private String name;
    
    @Email
    @NotBlank
    private String email;
}
```

## Sécurité

### Protéger les Endpoints Sensibles

```java
@AutoApi(
    path = "admin/users",
    security = @Security(
        enabled = true,
        roles = {"ADMIN"}
    )
)
```

## Performance

### Lazy vs Eager

```java
// ✅ Préférer LAZY
@ManyToOne(fetch = FetchType.LAZY)
private Category category;

// ❌ Éviter EAGER sauf nécessaire
@OneToMany(fetch = FetchType.EAGER)
private List<Review> reviews;
```

### Pagination Raisonnable

```yaml
springflow:
  pagination:
    default-page-size: 20    # Pas trop petit
    max-page-size: 100       # Pas trop grand
```

## Testing

### Test vos Entités

```java
@SpringBootTest
class ProductApiTest {
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateProduct() {
        // Test generated endpoints
    }
}
```

## Documentation

### Documenter avec OpenAPI

```java
@AutoApi(
    path = "products",
    description = "Product management API",
    tags = {"Products", "Catalog"}
)
```

## Voir Aussi

- [Architecture](architecture.md)
- [Performance](performance.md)
- [Security](../guide/security.md)
