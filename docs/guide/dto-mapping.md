# DTO Mapping

SpringFlow mappe automatiquement vos entités vers des DTOs Map-based.

## Fonctionnement

### Mapping Automatique

Pour chaque entité avec `@AutoApi`, SpringFlow crée automatiquement :

- **InputDTO** : Pour POST/PUT (exclut ID, @Hidden, @ReadOnly)
- **OutputDTO** : Pour GET (exclut @Hidden, inclut ID et @ReadOnly)

### Exemple

```java
@Entity
@AutoApi(path = "users")
public class User {
    @Id
    private Long id;
    
    @NotBlank
    private String name;
    
    @Hidden  // Exclu de tous les DTOs
    private String password;
    
    @ReadOnly  // Seulement en lecture
    private LocalDateTime createdAt;
}
```

**InputDTO (POST/PUT)** :
```json
{
  "name": "John Doe"
}
```

**OutputDTO (GET)** :
```json
{
  "id": 1,
  "name": "John Doe",
  "createdAt": "2025-01-15T10:30:00"
}
```

## Relations

Les relations JPA sont automatiquement gérées :

```java
@Entity
@AutoApi(path = "products")
public class Product {
    @Id
    private Long id;
    
    private String name;
    
    @ManyToOne
    private Category category;  // Mapped to category ID
    
    @OneToMany
    private List<Review> reviews;  // Mapped to list of IDs
}
```

## Gestion Circulaire

SpringFlow limite automatiquement la profondeur de mapping pour éviter les références circulaires :

- Profondeur max : 1 niveau
- Au-delà : mapping vers IDs uniquement

## Voir Aussi

- [Annotations](annotations.md#hidden-readonly)
- [Advanced DTO Mapping](../advanced/architecture.md)
