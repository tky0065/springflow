# Performance Tuning

Optimisations pour la production.

## N+1 Query Prevention

SpringFlow utilise automatiquement les **fetch joins** pour les relations `@ManyToOne`.

```java
// Génère automatiquement:
SELECT p FROM Product p
LEFT JOIN FETCH p.category
WHERE ...
```

## Pagination Optimisée

Les requêtes COUNT sont optimisées :

```java
// Count query optimisé sans joins
SELECT COUNT(p.id) FROM Product p

// Data query avec joins
SELECT p FROM Product p
LEFT JOIN FETCH p.category
LIMIT 20
```

## Cache des Métadonnées

Les métadonnées des entités sont cachées au démarrage :

```java
ConcurrentHashMap<Class<?>, EntityMetadata> cache
```

## Recommandations

### 1. Indexes Base de Données

```java
@Entity
@Table(indexes = {
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_category", columnList = "category_id")
})
public class Product { ... }
```

### 2. Fetch Strategy

```java
@ManyToOne(fetch = FetchType.LAZY)  // Préférer LAZY
private Category category;
```

### 3. Pagination Size

```yaml
springflow:
  pagination:
    default-page-size: 20  # Raisonnable
    max-page-size: 100     # Limiter les gros fetches
```

### 4. DTO Projection

Pour de gros volumes, utilisez des projections :

```java
@Query("SELECT new ProductSummary(p.id, p.name) FROM Product p")
List<ProductSummary> findSummaries();
```

## Monitoring

Activez les statistiques Hibernate :

```yaml
spring:
  jpa:
    properties:
      hibernate:
        generate_statistics: true
```

## Voir Aussi

- [Architecture](architecture.md)
- [Best Practices](best-practices.md)
