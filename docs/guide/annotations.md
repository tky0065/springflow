# Annotations Reference

Guide utilisateur des annotations SpringFlow avec exemples pratiques.

!!! info "Documentation Complète"
    Pour la référence API complète de toutes les annotations, consultez:

    **[→ Référence API des Annotations](../api/annotations.md)**

## :material-bookmark: Annotations Essentielles

### @AutoApi - Générer une API REST Complète

L'annotation principale qui active la génération automatique d'API pour une entité.

```java
@Entity
@AutoApi(path = "/products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal price;
}
```

**Résultat**: 6 endpoints REST générés automatiquement!

[En savoir plus sur @AutoApi →](../api/annotations.md#autoapi)

---

### @Filterable - Ajouter des Filtres de Recherche

Active le filtrage dynamique sur vos champs.

```java
@Filterable(types = {FilterType.LIKE, FilterType.EQUALS})
private String name;

@Filterable(types = FilterType.RANGE)
private BigDecimal price;
```

**Utilisation**:
- `GET /api/products?name_like=laptop`
- `GET /api/products?price_gte=100&price_lte=500`

[En savoir plus sur @Filterable →](../api/annotations.md#filterable)

---

### @Hidden - Protéger les Données Sensibles

Exclut complètement un champ de l'API (lecture ET écriture).

```java
@Hidden
private String passwordHash;

@Hidden
private String apiKey;
```

Ces champs ne seront **jamais** exposés via l'API.

[En savoir plus sur @Hidden →](../api/annotations.md#hidden)

---

### @ReadOnly - Champs en Lecture Seule

Visible dans les réponses, mais non modifiable via l'API.

```java
@ReadOnly
private Long id;

@ReadOnly
private LocalDateTime createdAt;

@ReadOnly
private BigDecimal calculatedTotal;
```

Parfait pour les IDs, timestamps, et valeurs calculées.

[En savoir plus sur @ReadOnly →](../api/annotations.md#readonly)

---

## :material-rocket: Démarrage Rapide

### Exemple Minimal

```java
@Entity
@AutoApi(path = "/tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    private String description;

    @ReadOnly
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

C'est tout! Vous avez maintenant une API REST complète.

### Exemple Intermédiaire

```java
@Entity
@AutoApi(
    path = "/products",
    description = "Product catalog",
    pagination = true,
    sorting = true
)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Filterable(types = {FilterType.LIKE, FilterType.EQUALS})
    private String name;

    @Min(0)
    @Filterable(types = FilterType.RANGE)
    private BigDecimal price;

    @Filterable(types = {FilterType.EQUALS, FilterType.IN})
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @ReadOnly
    private LocalDateTime createdAt;
}
```

### Exemple Avancé avec Sécurité

```java
@Entity
@AutoApi(
    path = "/admin/users",
    expose = Expose.CREATE_UPDATE,  // Pas de DELETE
    security = @Security(
        level = SecurityLevel.ROLE_BASED,
        roles = {"ADMIN"}
    )
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Filterable(types = FilterType.EQUALS)
    private String username;

    @Email
    @Filterable(types = FilterType.EQUALS)
    private String email;

    @Hidden  // Jamais exposé
    private String passwordHash;

    @Filterable(types = FilterType.EQUALS)
    private Boolean active = true;

    @ReadOnly
    private LocalDateTime createdAt;
}
```

---

## :material-lightbulb: Cas d'Usage Courants

### 1. API de Blog

```java
@Entity
@AutoApi(path = "/posts")
public class BlogPost {
    @Id
    private Long id;

    @NotBlank
    @Filterable(types = FilterType.LIKE)
    private String title;

    @NotBlank
    private String content;

    @Filterable(types = {FilterType.EQUALS, FilterType.IN})
    @Enumerated(EnumType.STRING)
    private PostStatus status;  // DRAFT, PUBLISHED

    @Filterable(types = FilterType.EQUALS)
    private String author;

    @ReadOnly
    private Integer viewCount = 0;

    @ReadOnly
    private LocalDateTime publishedAt;
}
```

**Requêtes possibles**:
- `GET /api/posts?status=PUBLISHED` - Articles publiés
- `GET /api/posts?title_like=spring` - Recherche par titre
- `GET /api/posts?author=john` - Articles d'un auteur

### 2. E-Commerce

```java
@Entity
@AutoApi(
    path = "/products",
    security = @Security(
        readLevel = SecurityLevel.PUBLIC,
        writeLevel = SecurityLevel.ROLE_BASED,
        roles = {"ADMIN"}
    )
)
public class Product {
    @Id
    private Long id;

    @Filterable(types = {FilterType.LIKE, FilterType.EQUALS})
    private String name;

    @Filterable(types = FilterType.RANGE)
    private BigDecimal price;

    @Filterable(types = FilterType.EQUALS)
    private String category;

    @Min(0)
    @Filterable(types = FilterType.RANGE)
    private Integer stock;

    @ReadOnly
    private LocalDateTime createdAt;

    @Hidden
    private BigDecimal costPrice;  // Marge cachée
}
```

**Fonctionnalités**:
- Lecture publique, modification admin seulement
- Filtrage par prix, catégorie, stock
- Coût d'achat caché des clients

### 3. Gestion de Tâches

```java
@Entity
@AutoApi(path = "/tasks")
public class Task {
    @Id
    private Long id;

    @NotBlank
    @Filterable(types = FilterType.LIKE)
    private String title;

    @Filterable(types = {FilterType.EQUALS, FilterType.IN})
    @Enumerated(EnumType.STRING)
    private TaskPriority priority;  // LOW, MEDIUM, HIGH

    @Filterable(types = {FilterType.EQUALS, FilterType.IN})
    @Enumerated(EnumType.STRING)
    private TaskStatus status;  // TODO, IN_PROGRESS, DONE

    @Filterable(types = FilterType.RANGE)
    private LocalDate dueDate;

    @Filterable(types = FilterType.EQUALS)
    private String assignedTo;

    @ReadOnly
    private LocalDateTime createdAt;
}
```

**Requêtes utiles**:
- `GET /api/tasks?status=TODO&assignedTo=alice` - Mes tâches
- `GET /api/tasks?priority_in=HIGH,MEDIUM&status=TODO` - Tâches urgentes
- `GET /api/tasks?dueDate_lte=2024-12-31` - Tâches à faire avant fin d'année

---

## :material-filter: Types de Filtres Disponibles

| Type | Usage | Exemple |
|------|-------|---------|
| **EQUALS** | Correspondance exacte | `?status=ACTIVE` |
| **LIKE** | Recherche textuelle | `?name_like=laptop` |
| **RANGE** | Intervalle min/max | `?price_gte=100&price_lte=500` |
| **IN** | Liste de valeurs | `?status_in=ACTIVE,PENDING` |
| **NOT_IN** | Exclusion de valeurs | `?status_not_in=DELETED` |
| **IS_NULL** | Vérifier null/non-null | `?deletedAt_null=true` |
| **GREATER_THAN** | Plus grand que | `?age_gt=18` |
| **LESS_THAN** | Plus petit que | `?price_lt=1000` |

[Liste complète des FilterType →](../api/annotations.md#filtertype-enum)

---

## :material-shield-lock: Niveaux de Sécurité

### PUBLIC (Par Défaut)

```java
@AutoApi(path = "/products")  // Accessible à tous
```

### AUTHENTICATED

```java
@AutoApi(
    path = "/orders",
    security = @Security(level = SecurityLevel.AUTHENTICATED)
)
```

Nécessite un utilisateur connecté.

### ROLE_BASED

```java
@AutoApi(
    path = "/admin/users",
    security = @Security(
        level = SecurityLevel.ROLE_BASED,
        roles = {"ADMIN", "MANAGER"}
    )
)
```

Nécessite un rôle spécifique.

### Sécurité Granulaire

```java
@AutoApi(
    path = "/reports",
    security = @Security(
        readLevel = SecurityLevel.AUTHENTICATED,  // Lecture: tout le monde
        writeLevel = SecurityLevel.ROLE_BASED,    // Écriture: admin seulement
        roles = {"ADMIN"}
    )
)
```

[En savoir plus sur la sécurité →](security.md)

---

## :material-eye-off: Contrôle d'Exposition

### Expose.ALL (Par Défaut)

Tous les endpoints CRUD.

```java
@AutoApi(path = "/products", expose = Expose.ALL)
```

### Expose.READ_ONLY

Lecture seule (GET uniquement).

```java
@AutoApi(path = "/reports", expose = Expose.READ_ONLY)
```

Génère uniquement:
- `GET /api/reports`
- `GET /api/reports/{id}`

### Expose.CREATE_UPDATE

Pas de suppression physique.

```java
@AutoApi(path = "/customers", expose = Expose.CREATE_UPDATE)
```

Génère GET, POST, PUT, PATCH mais **pas DELETE**.

Parfait avec `@SoftDelete` (Phase 2).

---

## :material-frequently-asked-questions: Questions Fréquentes

### Puis-je utiliser @Hidden ET @ReadOnly?

Non, c'est redondant. `@Hidden` cache déjà complètement le champ.

### Comment filtrer sur une relation?

```java
@ManyToOne
@Filterable(types = FilterType.EQUALS, paramName = "categoryId")
private Category category;
```

Utilisation: `GET /api/products?categoryId=5`

### Comment désactiver la pagination?

```java
@AutoApi(path = "/config", pagination = false)
```

⚠️ Attention: tous les résultats seront retournés!

### Les IDs sont-ils automatiquement ReadOnly?

Oui! Les champs `@Id` sont automatiquement traités comme `@ReadOnly`.

---

## :material-link: Voir Aussi

- **[Référence API Complète](../api/annotations.md)** - Tous les paramètres et options
- **[Configuration](configuration.md)** - Configurer SpringFlow
- **[Filtrage](filtering.md)** - Guide complet du filtrage
- **[Sécurité](security.md)** - Configuration de la sécurité
- **[Quick Start](../getting-started/quickstart.md)** - Premier projet
- **[Custom Components](../advanced/custom-components.md)** - Personnalisation avancée

---

## :material-star: Résumé

SpringFlow propose **5 annotations principales**:

1. **@AutoApi** - Active la génération d'API
2. **@Filterable** - Active le filtrage dynamique
3. **@Hidden** - Cache complètement un champ
4. **@ReadOnly** - Champ visible mais non modifiable
5. **@Security** - Configure l'authentification/autorisation

Et **3 enums de configuration**:

1. **FilterType** - 12 types de filtres disponibles
2. **Expose** - Contrôle des opérations CRUD exposées
3. **SecurityLevel** - PUBLIC, AUTHENTICATED, ROLE_BASED

Avec ces annotations simples, vous pouvez générer des APIs REST complètes et sécurisées en quelques lignes de code! 🚀
