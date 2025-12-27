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

- Profondeur max : 1 niveau par défaut
- Au-delà : mapping vers IDs uniquement
- Détection de cycles avec `MappingContext` (v0.4.0+)

## Configuration Avancée

!!! info "Nouveau depuis v0.4.0"
    SpringFlow offre maintenant un contrôle granulaire sur le mapping avec **DtoMappingConfig**, permettant de configurer la profondeur de mapping, la détection de cycles, et l'inclusion des champs null.

### Configurations Prédéfinies

SpringFlow propose trois configurations prêtes à l'emploi :

| Configuration | Max Depth | Detect Cycles | Include Nulls | Cas d'usage |
|---------------|-----------|---------------|---------------|-------------|
| `DEFAULT` | 1 | ✓ | ✗ | Usage standard, relations à 1 niveau |
| `DEEP` | 3 | ✓ | ✗ | Graphes complexes, nested relations |
| `SHALLOW` | 0 | ✓ | ✗ | IDs uniquement, payload minimal |

### Configuration DEFAULT (par défaut)

```java
@Entity
@AutoApi(path = "products")
public class Product {
    @Id
    private Long id;
    private String name;

    @ManyToOne
    private Category category;  // Mappé avec ses champs
}

@Entity
public class Category {
    @Id
    private Long id;
    private String name;

    @ManyToOne
    private Department department;  // Mappé en ID uniquement (depth > 1)
}
```

**Résultat avec DEFAULT (depth=1)** :
```json
{
  "id": 1,
  "name": "iPhone 15",
  "category": {
    "id": 10,
    "name": "Electronics",
    "department": 5  // ← ID uniquement (depth 2)
  }
}
```

### Configuration DEEP (relations profondes)

Pour des graphes d'objets complexes nécessitant 3 niveaux de profondeur :

```java
DtoMappingConfig config = DtoMappingConfig.DEEP;
```

**Résultat avec DEEP (depth=3)** :
```json
{
  "id": 1,
  "name": "iPhone 15",
  "category": {
    "id": 10,
    "name": "Electronics",
    "department": {
      "id": 5,
      "name": "Tech",
      "building": {
        "id": 2,
        "name": "Main Building",
        "company": 1  // ← ID uniquement (depth 4)
      }
    }
  }
}
```

### Configuration SHALLOW (IDs uniquement)

Pour minimiser le payload et obtenir uniquement les IDs des relations :

```java
DtoMappingConfig config = DtoMappingConfig.SHALLOW;
```

**Résultat avec SHALLOW (depth=0)** :
```json
{
  "id": 1,
  "name": "iPhone 15",
  "category": 10  // ← ID uniquement
}
```

### Configuration Personnalisée

Créez votre propre configuration avec le Builder :

```java
DtoMappingConfig config = DtoMappingConfig.builder()
    .maxDepth(2)                    // Profondeur personnalisée
    .detectCycles(true)             // Détection de cycles
    .includeNullFields(true)        // Inclure champs null
    .build();
```

#### Options disponibles

| Option | Type | Défaut | Description |
|--------|------|--------|-------------|
| `maxDepth` | int | 1 | Profondeur max de mapping des relations |
| `detectCycles` | boolean | true | Détection des références circulaires |
| `includeNullFields` | boolean | false | Inclure les champs null dans le DTO |

### Détection de Cycles (MappingContext)

SpringFlow utilise `MappingContext` pour détecter et prévenir les boucles infinies dans les relations bidirectionnelles.

#### Exemple de Relation Bidirectionnelle

```java
@Entity
public class Product {
    @Id
    private Long id;
    private String name;

    @ManyToOne
    private Category category;
}

@Entity
public class Category {
    @Id
    private Long id;
    private String name;

    @OneToMany(mappedBy = "category")
    private List<Product> products;  // ← Relation bidirectionnelle
}
```

Sans détection de cycles, cela causerait une boucle infinie :
```
Product → Category → Products → Category → Products → ...
```

#### Fonctionnement de MappingContext

```java
public class MappingContext {
    private final Set<Object> visitedEntities;

    public boolean isBeingMapped(Object entity) {
        // Utilise System.identityHashCode pour détecter la même instance
        return visitedEntities.stream()
            .anyMatch(visited ->
                System.identityHashCode(visited) == System.identityHashCode(entity)
            );
    }

    public void enterEntity(Object entity) {
        visitedEntities.add(entity);
    }

    public void exitEntity(Object entity) {
        visitedEntities.remove(entity);
    }
}
```

**Résultat avec détection de cycles** :
```json
{
  "id": 1,
  "name": "iPhone 15",
  "category": {
    "id": 10,
    "name": "Electronics",
    "products": [1, 2, 3]  // ← IDs uniquement (cycle détecté)
  }
}
```

### Désactiver la Détection de Cycles

!!! warning "Attention"
    Désactiver la détection de cycles peut causer des **StackOverflowError** avec des relations bidirectionnelles. Ne désactivez que si vous êtes sûr de votre modèle de données.

```java
DtoMappingConfig config = DtoMappingConfig.builder()
    .detectCycles(false)  // ⚠️ Risque de boucle infinie
    .build();
```

### Inclusion des Champs Null

Par défaut, les champs null sont exclus du DTO pour réduire le payload. Activez cette option pour les inclure :

```java
DtoMappingConfig config = DtoMappingConfig.builder()
    .includeNullFields(true)
    .build();
```

**Exemple** :
```java
Product product = new Product();
product.setId(1L);
product.setName("iPhone 15");
product.setDescription(null);  // Champ null
```

**Avec includeNullFields=false (défaut)** :
```json
{
  "id": 1,
  "name": "iPhone 15"
}
```

**Avec includeNullFields=true** :
```json
{
  "id": 1,
  "name": "iPhone 15",
  "description": null
}
```

### Scénarios d'Usage Avancés

#### API Mobile (payload minimal)

```java
// Configuration SHALLOW pour réduire la taille des réponses
DtoMappingConfig config = DtoMappingConfig.SHALLOW;
```

Avantages :
- Payload réduit de 60-80%
- Temps de réponse amélioré
- Consommation data mobile réduite

#### Dashboard Admin (données complètes)

```java
// Configuration DEEP pour afficher toutes les relations
DtoMappingConfig config = DtoMappingConfig.DEEP;
```

Avantages :
- Moins de requêtes vers le backend
- Données complètes en un seul appel
- UX améliorée (pas de chargements multiples)

#### Export JSON (données exhaustives)

```java
// Configuration custom avec tous les champs
DtoMappingConfig config = DtoMappingConfig.builder()
    .maxDepth(5)
    .includeNullFields(true)
    .build();
```

Avantages :
- Export complet de la base de données
- Tous les champs présents (nulls inclus)
- Relations profondes exportées

### Performance

| Configuration | Payload Size | Temps Mapping | Mémoire |
|---------------|--------------|---------------|---------|
| SHALLOW | ★★★★★ | ★★★★★ | ★★★★★ |
| DEFAULT | ★★★★☆ | ★★★★☆ | ★★★★☆ |
| DEEP | ★★★☆☆ | ★★★☆☆ | ★★★☆☆ |

### Fork de Contexte

Pour des mappings imbriqués isolés :

```java
MappingContext context = new MappingContext(DtoMappingConfig.DEFAULT);
MappingContext forked = context.fork();  // Nouveau contexte, même config
```

Cas d'usage :
- Mapping parallèle de collections
- Isolation des cycles détectés
- Thread-safety dans mappings concurrents

## Voir Aussi

- [Annotations](annotations.md#hidden-readonly)
- [Validation](validation.md)
- [PATCH Endpoint](../api/endpoints.md#patch-partial-update)
- [Advanced Architecture](../advanced/architecture.md)
