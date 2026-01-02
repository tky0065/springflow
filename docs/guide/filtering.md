# Dynamic Filtering

Filtrage dynamique des résultats avec `@Filterable`.

## Configuration

Activez le filtrage sur les champs :

```java
@Entity
@AutoApi(path = "products")
public class Product {
    @Id
    private Long id;
    
    @Filterable(types = {FilterType.EQUALS, FilterType.LIKE})
    private String name;
    
    @Filterable(types = {FilterType.RANGE})
    private BigDecimal price;
    
    @Filterable(types = {FilterType.EQUALS})
    private Category category;
}
```

## Types de Filtres

| Type | Opérateur | Exemple |
|------|-----------|---------|
| `EQUALS` | `=` | `?name=Phone` |
| `LIKE` | `LIKE %x%` | `?name_like=Phon` |
| `GREATER_THAN` | `>` | `?price_gt=100` |
| `LESS_THAN` | `<` | `?price_lt=500` |
| `RANGE` | `BETWEEN` | `?price_range=100,500` |
| `IN` | `IN (...)` | `?status_in=ACTIVE,PENDING` |
| `IS_NULL` | `IS NULL` | `?email_null=true` |

## Exemples

### Filtre Simple

```bash
GET /api/products?name=iPhone
```

### Filtre LIKE

```bash
GET /api/products?name_like=Phone
```

### Filtre Range

```bash
GET /api/products?price_range=100,500
```

### Combinaison de Filtres

```bash
GET /api/products?name_like=Phone&price_range=100,500&category=ELECTRONICS
```

## Advanced Search (JPA Specification)

Pour des scénarios de filtrage plus complexes nécessitant une structure stricte, vous pouvez activer le support **JPA Specification**.

### Activation

Ajoutez `supportSpecification = true` à l'annotation `@AutoApi` :

```java
@Entity
@AutoApi(
    path = "/products",
    supportSpecification = true
)
public class Product { ... }
```

Cela génère automatiquement :
1.  Un Repository étendant `JpaSpecificationExecutor<Product>`.
2.  Un endpoint `POST /api/products/search`.

### Endpoint de Recherche

Utilisez l'endpoint `POST /search` avec un corps JSON structuré (`SearchRequest`).

**Requête** : `POST /api/products/search`

```json
{
  "operator": "AND",
  "criteria": [
    {
      "field": "name",
      "operator": "LIKE",
      "value": "Laptop"
    },
    {
      "field": "price",
      "operator": "GREATER_THAN",
      "value": 1000
    },
    {
      "field": "active",
      "operator": "EQUALS",
      "value": true
    }
  ]
}
```

### Opérateurs Disponibles

| Opérateur | Description |
|-----------|-------------|
| `EQUALS` | Égalité exacte |
| `NOT_EQUALS` | Différence |
| `GREATER_THAN` | Strictement supérieur |
| `GREATER_THAN_OR_EQUAL` | Supérieur ou égal |
| `LESS_THAN` | Strictement inférieur |
| `LESS_THAN_OR_EQUAL` | Inférieur ou égal |
| `LIKE` | Contient le texte (case-sensitive selon DB) |
| `IN` | Dans une liste de valeurs |
| `IS_NULL` | Est null (value ignorée) |
| `IS_NOT_NULL` | N'est pas null (value ignorée) |

## Voir Aussi

- [Annotations Reference](../api/annotations.md#filterable)
- [Performance](../advanced/performance.md)