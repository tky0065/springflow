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

## Voir Aussi

- [Annotations Reference](annotations.md#filterable)
- [Performance](../advanced/performance.md)
