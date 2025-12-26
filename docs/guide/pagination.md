# Pagination & Sorting

Gestion automatique de la pagination et du tri.

## Pagination

### Configuration

Par défaut :
- Page size : 20
- Max page size : 100

Personnalisation dans `application.yml` :

```yaml
springflow:
  pagination:
    default-page-size: 20
    max-page-size: 100
```

### Utilisation

```bash
# Page 1 (0-indexed), 20 items
GET /api/products?page=0&size=20

# Page 2, 50 items
GET /api/products?page=1&size=50
```

### Response Format

```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false,
  "number": 0,
  "size": 20
}
```

## Sorting

### Simple Sort

```bash
# Tri par nom (ascendant)
GET /api/products?sort=name

# Tri par prix (descendant)
GET /api/products?sort=price,desc
```

### Multi-field Sort

```bash
GET /api/products?sort=category,asc&sort=price,desc
```

## Pagination + Sorting

```bash
GET /api/products?page=0&size=20&sort=name,asc
```

## Voir Aussi

- [Configuration](configuration.md)
- [Performance](../advanced/performance.md)
