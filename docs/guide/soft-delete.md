# Soft Delete

Suppression logique avec possibilité de restauration.

## Activation

```java
@Entity
@AutoApi(path = "articles")
@SoftDelete
public class Article {
    @Id
    private Long id;
    
    private String title;
    
    // Ajouté automatiquement par SpringFlow
    // private Boolean deleted;
    // private LocalDateTime deletedAt;
}
```

## Comportement

### DELETE

Au lieu de supprimer physiquement :

```bash
DELETE /api/articles/1
```

SpringFlow met à jour :
```java
article.deleted = true;
article.deletedAt = LocalDateTime.now();
```

### Restauration

```bash
POST /api/articles/1/restore
```

Restaure l'entité :
```java
article.deleted = false;
article.deletedAt = null;
```

## Query Parameters

### Inclure les Supprimés

```bash
GET /api/articles?includeDeleted=true
```

### Uniquement les Supprimés

```bash
GET /api/articles?deletedOnly=true
```

## Hard Delete

Pour une vraie suppression physique, implémentez un endpoint custom.

## Voir Aussi

- [Annotations](annotations.md#softdelete)
- [Auditing](auditing.md)
