# Annotations API Reference

Référence complète de toutes les annotations SpringFlow avec exemples détaillés.

## :material-tag: Annotations Principales

### @AutoApi

Active la génération automatique d'une API REST complète pour une entité JPA.

**Cible**: Type (Class)
**Retention**: Runtime
**Package**: `io.springflow.annotations`

#### Paramètres

| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `path` | String | `""` | Chemin de base pour les endpoints (sans slash initial) |
| `expose` | Expose | `ALL` | Opérations CRUD exposées |
| `security` | @Security | `@Security()` | Configuration de sécurité |
| `pagination` | boolean | `true` | Active la pagination |
| `sorting` | boolean | `true` | Active le tri |
| `description` | String | `""` | Description pour OpenAPI |
| `tags` | String[] | `{}` | Tags OpenAPI pour regroupement |

#### Exemple Basique

```java
@Entity
@Table(name = "products")
@AutoApi(path = "/products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal price;
}
```

**Génère automatiquement**:
- `GET /api/products` - Liste avec pagination
- `GET /api/products/{id}` - Détails
- `POST /api/products` - Création
- `PUT /api/products/{id}` - Mise à jour complète
- `PATCH /api/products/{id}` - Mise à jour partielle
- `DELETE /api/products/{id}` - Suppression

#### Exemple Complet

```java
@Entity
@AutoApi(
    path = "/products",
    expose = Expose.ALL,
    pagination = true,
    sorting = true,
    description = "Product management API",
    tags = {"Products", "Inventory"},
    security = @Security(
        level = SecurityLevel.AUTHENTICATED
    )
)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100)
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

    @Hidden
    private String internalNotes;
}
```

#### Path Configuration

```java
// Chemin simple
@AutoApi(path = "/products")
// → /api/products

// Chemin hiérarchique
@AutoApi(path = "/admin/users")
// → /api/admin/users

// Chemin avec sous-ressource
@AutoApi(path = "/orders/{orderId}/items")
// → /api/orders/{orderId}/items
```

---

### @Filterable

Active le filtrage dynamique sur un champ d'entité.

**Cible**: Field
**Retention**: Runtime
**Package**: `io.springflow.annotations`

#### Paramètres

| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `types` | FilterType[] | `EQUALS` | Types de filtres supportés |
| `paramName` | String | `""` | Nom personnalisé du paramètre (utilise le nom du champ si vide) |
| `description` | String | `""` | Description pour OpenAPI |
| `caseSensitive` | boolean | `true` | Sensibilité à la casse (pour EQUALS, LIKE) |

#### Exemples par Type

##### EQUALS - Correspondance exacte

```java
@Filterable(types = FilterType.EQUALS)
private String status;
```

**Utilisation**: `GET /api/products?status=ACTIVE`

##### LIKE - Recherche textuelle

```java
@Filterable(types = FilterType.LIKE, caseSensitive = false)
private String name;
```

**Utilisation**: `GET /api/products?name_like=laptop`
**SQL**: `WHERE LOWER(name) LIKE LOWER('%laptop%')`

##### RANGE - Intervalle numérique/temporel

```java
@Filterable(types = FilterType.RANGE)
private BigDecimal price;

@Filterable(types = FilterType.RANGE)
private LocalDateTime createdAt;
```

**Utilisation**:
- `GET /api/products?price_gte=100&price_lte=500`
- `GET /api/products?createdAt_gte=2024-01-01&createdAt_lte=2024-12-31`

##### IN - Multiples valeurs

```java
@Filterable(types = FilterType.IN)
private ProductStatus status;
```

**Utilisation**: `GET /api/products?status_in=ACTIVE,PENDING,DRAFT`

##### Combinaisons

```java
@Filterable(types = {FilterType.EQUALS, FilterType.LIKE, FilterType.IS_NULL})
private String category;
```

**Utilisation**:
- `?category=Electronics` - Exact
- `?category_like=Elect` - Partiel
- `?category_null=false` - Non null

#### Exemples Avancés

##### Filtre avec Nom Personnalisé

```java
@Filterable(
    types = {FilterType.EQUALS, FilterType.LIKE},
    paramName = "search",
    description = "Search by product name or SKU"
)
private String name;
```

**Utilisation**: `?search=laptop` ou `?search_like=lap`

##### Multiple Types avec Case-Insensitive

```java
@Filterable(
    types = {FilterType.EQUALS, FilterType.LIKE},
    caseSensitive = false,
    description = "Filter by customer name (case-insensitive)"
)
private String customerName;
```

---

### @Hidden

Exclut un champ des DTOs générés (input et output).

**Cible**: Field
**Retention**: Runtime
**Package**: `io.springflow.annotations`

#### Caractéristiques

- ❌ N'apparaît jamais dans les requêtes POST/PUT
- ❌ N'apparaît jamais dans les réponses GET
- ❌ N'apparaît pas dans le schéma OpenAPI
- ✅ Reste accessible dans le code Java

#### Cas d'Usage

```java
@Entity
@AutoApi(path = "/users")
public class User {
    @Id
    private Long id;

    private String username;

    @Email
    private String email;

    // Champs sensibles - jamais exposés
    @Hidden
    @Column(nullable = false)
    private String passwordHash;

    @Hidden
    private String apiKey;

    // Champs internes - jamais exposés
    @Hidden
    private String internalNotes;

    @Hidden
    private Integer loginAttempts;
}
```

**Résultat**:

```json
// GET /api/users/1
{
  "id": 1,
  "username": "john.doe",
  "email": "john@example.com"
  // passwordHash, apiKey, internalNotes, loginAttempts ne sont PAS inclus
}
```

#### Comparaison avec @ReadOnly

| Annotation | Input DTO | Output DTO | Use Case |
|------------|-----------|------------|----------|
| `@Hidden` | ❌ Exclu | ❌ Exclu | Données sensibles, champs internes |
| `@ReadOnly` | ❌ Exclu | ✅ Inclus | Timestamps, IDs, champs calculés |
| (rien) | ✅ Inclus | ✅ Inclus | Données normales |

---

### @ReadOnly

Marque un champ comme lecture seule dans l'API REST.

**Cible**: Field
**Retention**: Runtime
**Package**: `io.springflow.annotations`

#### Caractéristiques

- ❌ Exclu des requêtes POST/PUT (non modifiable)
- ✅ Inclus dans les réponses GET (visible)
- 📄 Documenté comme "read-only" dans OpenAPI

#### Cas d'Usage

```java
@Entity
@AutoApi(path = "/orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ReadOnly  // ID auto-généré
    private Long id;

    private String customerName;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> items;

    // Champs calculés
    @ReadOnly
    private BigDecimal totalAmount;  // Calculé à partir des items

    @ReadOnly
    private Integer itemCount;       // Nombre d'items

    // Timestamps
    @ReadOnly
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ReadOnly
    private LocalDateTime updatedAt;

    // Statut auto-généré
    @ReadOnly
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateTotals();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotals();
    }

    private void calculateTotals() {
        totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        itemCount = items.size();
    }
}
```

**Comportement**:

```java
// POST /api/orders
// Input DTO (totalAmount, itemCount, createdAt IGNORÉS)
{
  "customerName": "John Doe",
  "items": [...]
}

// Réponse (tous les champs visibles)
{
  "id": 123,
  "customerName": "John Doe",
  "totalAmount": 499.99,     // ✅ Calculé automatiquement
  "itemCount": 3,            // ✅ Calculé automatiquement
  "createdAt": "2024-12-26T10:00:00",
  "updatedAt": "2024-12-26T10:00:00",
  "status": "PENDING"
}
```

#### ID Fields

!!! tip "ID Fields sont toujours ReadOnly"
    Les champs annotés avec `@Id` ou `@EmbeddedId` sont automatiquement traités comme `@ReadOnly`, même sans l'annotation explicite.

---

## :material-filter: FilterType Enum

Types de filtres disponibles pour `@Filterable`.

### Liste Complète

| Type | Paramètre | SQL Equivalent | Exemple |
|------|-----------|----------------|---------|
| **EQUALS** | `?field=value` | `WHERE field = ?` | `?status=ACTIVE` |
| **LIKE** | `?field_like=pattern` | `WHERE field LIKE '%?%'` | `?name_like=laptop` |
| **GREATER_THAN** | `?field_gt=value` | `WHERE field > ?` | `?price_gt=100` |
| **LESS_THAN** | `?field_lt=value` | `WHERE field < ?` | `?price_lt=1000` |
| **GREATER_THAN_OR_EQUAL** | `?field_gte=value` | `WHERE field >= ?` | `?age_gte=18` |
| **LESS_THAN_OR_EQUAL** | `?field_lte=value` | `WHERE field <= ?` | `?age_lte=65` |
| **RANGE** | `?field_gte=min&field_lte=max` | `WHERE field BETWEEN ? AND ?` | `?price_gte=100&price_lte=500` |
| **IN** | `?field_in=v1,v2,v3` | `WHERE field IN (?, ?, ?)` | `?status_in=ACTIVE,PENDING` |
| **NOT_IN** | `?field_not_in=v1,v2` | `WHERE field NOT IN (?, ?)` | `?status_not_in=DELETED` |
| **IS_NULL** | `?field_null=true/false` | `WHERE field IS [NOT] NULL` | `?deletedAt_null=true` |
| **BETWEEN** | `?field_between=min,max` | `WHERE field BETWEEN ? AND ?` | `?age_between=18,65` |

### Exemples par Cas d'Usage

#### Recherche Textuelle

```java
@Filterable(types = {FilterType.EQUALS, FilterType.LIKE})
private String productName;
```

Requêtes:
- `?productName=Laptop` - Exactement "Laptop"
- `?productName_like=lap` - Contient "lap"

#### Plages de Prix

```java
@Filterable(types = FilterType.RANGE)
private BigDecimal price;
```

Requêtes:
- `?price_gte=100` - Prix >= 100
- `?price_lte=500` - Prix <= 500
- `?price_gte=100&price_lte=500` - Entre 100 et 500

#### Filtres de Dates

```java
@Filterable(types = {FilterType.RANGE, FilterType.IS_NULL})
private LocalDateTime publishedAt;
```

Requêtes:
- `?publishedAt_gte=2024-01-01` - Publié après le 1er janvier
- `?publishedAt_null=false` - Articles publiés (non null)
- `?publishedAt_null=true` - Brouillons (null)

#### Statuts Multiples

```java
@Filterable(types = {FilterType.EQUALS, FilterType.IN, FilterType.NOT_IN})
@Enumerated(EnumType.STRING)
private OrderStatus status;
```

Requêtes:
- `?status=ACTIVE` - Statut ACTIVE uniquement
- `?status_in=ACTIVE,PENDING` - ACTIVE ou PENDING
- `?status_not_in=DELETED,ARCHIVED` - Exclure DELETED et ARCHIVED

---

## :material-shield-lock: Annotations de Sécurité

### @Security

Configure l'authentification et l'autorisation pour les endpoints générés.

**Cible**: Annotation (utilisée dans @AutoApi)
**Retention**: Runtime
**Package**: `io.springflow.annotations`

#### Paramètres

| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `enabled` | boolean | `true` | Active/désactive la sécurité |
| `level` | SecurityLevel | `PUBLIC` | Niveau de sécurité global |
| `roles` | String[] | `{}` | Rôles requis (pour ROLE_BASED) |
| `authorities` | String[] | `{}` | Authorities requises (pour ROLE_BASED) |
| `readLevel` | SecurityLevel | `UNDEFINED` | Niveau pour GET (override level) |
| `writeLevel` | SecurityLevel | `UNDEFINED` | Niveau pour POST/PUT/DELETE (override level) |

#### Exemples

##### Public Access (Par Défaut)

```java
@Entity
@AutoApi(
    path = "/products",
    security = @Security(level = SecurityLevel.PUBLIC)
)
public class Product { }
```

Tous les endpoints sont publics.

##### Authentification Requise

```java
@Entity
@AutoApi(
    path = "/orders",
    security = @Security(level = SecurityLevel.AUTHENTICATED)
)
public class Order { }
```

Utilisateur doit être authentifié (JWT valide, session active).

##### Basé sur les Rôles

```java
@Entity
@AutoApi(
    path = "/users",
    security = @Security(
        level = SecurityLevel.ROLE_BASED,
        roles = {"ADMIN", "USER_MANAGER"}
    )
)
public class User { }
```

Utilisateur doit avoir le rôle ADMIN OU USER_MANAGER.

##### Sécurité Granulaire (Read/Write)

```java
@Entity
@AutoApi(
    path = "/reports",
    security = @Security(
        readLevel = SecurityLevel.AUTHENTICATED,     // GET: n'importe quel user
        writeLevel = SecurityLevel.ROLE_BASED,       // POST/PUT/DELETE: admin seulement
        roles = {"ADMIN"}
    )
)
public class Report { }
```

- **GET** endpoints: authentification simple
- **POST/PUT/DELETE** endpoints: rôle ADMIN requis

##### Authorities (Fine-Grained)

```java
@Entity
@AutoApi(
    path = "/documents",
    security = @Security(
        level = SecurityLevel.ROLE_BASED,
        authorities = {"document:read", "document:write", "document:delete"}
    )
)
public class Document { }
```

### SecurityLevel Enum

| Valeur | Description | Use Case |
|--------|-------------|----------|
| **PUBLIC** | Aucune authentification | APIs publiques, données ouvertes |
| **AUTHENTICATED** | Authentification simple | Données utilisateur, profils |
| **ROLE_BASED** | Rôles/authorities spécifiques | Admin, gestion, opérations sensibles |
| **UNDEFINED** | Hérite du niveau parent | Utilisé pour readLevel/writeLevel |

---

## :material-delete: Annotations Phase 2

Ces annotations sont définies mais **pas encore implémentées** (Phase 2 du roadmap).

### @SoftDelete

Active la suppression logique (soft delete) pour une entité.

**Status**: 🚧 Phase 2 - Non implémenté
**Cible**: Type (Class)
**Retention**: Runtime

#### Paramètres

| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `deletedField` | String | `"deleted"` | Nom du champ boolean de suppression |
| `deletedAtField` | String | `"deletedAt"` | Nom du champ timestamp de suppression |

#### Exemple

```java
@Entity
@AutoApi(path = "/products")
@SoftDelete
public class Product {
    @Id
    private Long id;

    private String name;

    // Champs ajoutés pour soft delete
    private Boolean deleted = false;
    private LocalDateTime deletedAt;
}
```

**Comportement attendu**:
- `DELETE /api/products/1` → Met `deleted = true`, `deletedAt = now()`
- `GET /api/products` → Filtre automatiquement `WHERE deleted = false`
- Endpoint de restauration: `POST /api/products/1/restore`

---

### @Auditable

Active le tracking automatique des modifications (audit trail).

**Status**: 🚧 Phase 2 - Non implémenté
**Cible**: Type (Class)
**Retention**: Runtime

#### Paramètres

| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `versioned` | boolean | `false` | Active l'optimistic locking avec @Version |
| `createdAtField` | String | `"createdAt"` | Nom du champ timestamp création |
| `updatedAtField` | String | `"updatedAt"` | Nom du champ timestamp mise à jour |
| `createdByField` | String | `"createdBy"` | Nom du champ créateur |
| `updatedByField` | String | `"updatedBy"` | Nom du champ dernier modificateur |

#### Exemple

```java
@Entity
@AutoApi(path = "/documents")
@Auditable(versioned = true)
public class Document {
    @Id
    private Long id;

    private String title;
    private String content;

    // Champs ajoutés automatiquement:
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;  // Pour versioned = true
}
```

**Comportement attendu**:
- Remplissage automatique de `createdAt`, `createdBy` lors de la création
- Mise à jour automatique de `updatedAt`, `updatedBy` lors des modifications
- Intégration avec Spring Security pour récupérer l'utilisateur courant
- Optimistic locking avec `version` si `versioned = true`

---

## :material-eye-off: Expose Enum

Contrôle quelles opérations CRUD sont exposées via l'API.

### Valeurs Disponibles

#### ALL (Par Défaut)

Expose toutes les opérations CRUD.

```java
@AutoApi(path = "/products", expose = Expose.ALL)
```

**Endpoints générés**:
- ✅ `GET /api/products` - Liste
- ✅ `GET /api/products/{id}` - Détails
- ✅ `POST /api/products` - Création
- ✅ `PUT /api/products/{id}` - Mise à jour complète
- ✅ `PATCH /api/products/{id}` - Mise à jour partielle
- ✅ `DELETE /api/products/{id}` - Suppression

#### READ_ONLY

Expose uniquement les opérations de lecture.

```java
@AutoApi(path = "/reports", expose = Expose.READ_ONLY)
```

**Endpoints générés**:
- ✅ `GET /api/reports` - Liste
- ✅ `GET /api/reports/{id}` - Détails
- ❌ POST, PUT, PATCH, DELETE

**Use Cases**:
- Données de reporting
- Vues en lecture seule
- Données générées automatiquement

#### CREATE_UPDATE

Expose création et modification, mais pas la suppression.

```java
@AutoApi(path = "/customers", expose = Expose.CREATE_UPDATE)
```

**Endpoints générés**:
- ✅ `GET /api/customers` - Liste
- ✅ `GET /api/customers/{id}` - Détails
- ✅ `POST /api/customers` - Création
- ✅ `PUT /api/customers/{id}` - Mise à jour complète
- ✅ `PATCH /api/customers/{id}` - Mise à jour partielle
- ❌ DELETE

**Use Cases**:
- Entités qui ne doivent jamais être supprimées physiquement
- Combiné avec `@SoftDelete` pour suppression logique
- Données réglementaires (conservation obligatoire)

#### CUSTOM

Réservé pour Phase 2 - contrôle fin des opérations.

```java
@AutoApi(path = "/advanced", expose = Expose.CUSTOM)
```

**Status**: 🚧 Phase 2 - Configuration granulaire par opération

---

## :material-file-tree: Résumé des Annotations

### Annotations Implémentées (Phase 1)

| Annotation | Niveau | Usage | Status |
|------------|--------|-------|--------|
| `@AutoApi` | Class | Active génération API complète | ✅ Implémenté |
| `@Filterable` | Field | Active filtrage dynamique | ✅ Implémenté |
| `@Hidden` | Field | Exclut du DTO (input + output) | ✅ Implémenté |
| `@ReadOnly` | Field | Exclut du DTO input uniquement | ✅ Implémenté |
| `@Security` | Annotation | Configure sécurité endpoints | ✅ Implémenté |

### Annotations Phase 2 (À venir)

| Annotation | Niveau | Usage | Status |
|------------|--------|-------|--------|
| `@SoftDelete` | Class | Suppression logique | 🚧 Phase 2 |
| `@Auditable` | Class | Audit trail automatique | 🚧 Phase 2 |

### Enums

| Enum | Usage | Valeurs | Status |
|------|-------|---------|--------|
| `FilterType` | Types de filtres | EQUALS, LIKE, RANGE, IN, etc. (12 types) | ✅ Implémenté |
| `Expose` | Opérations exposées | ALL, READ_ONLY, CREATE_UPDATE, CUSTOM | ✅ Implémenté |
| `SecurityLevel` | Niveau de sécurité | PUBLIC, AUTHENTICATED, ROLE_BASED | ✅ Implémenté |

---

## :material-lightbulb: Exemples Complets

### E-Commerce Product

```java
@Entity
@Table(name = "products")
@AutoApi(
    path = "/products",
    expose = Expose.ALL,
    pagination = true,
    sorting = true,
    description = "Product catalog management",
    tags = {"Products", "Inventory"},
    security = @Security(
        readLevel = SecurityLevel.PUBLIC,
        writeLevel = SecurityLevel.ROLE_BASED,
        roles = {"ADMIN", "INVENTORY_MANAGER"}
    )
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ReadOnly
    private Long id;

    @NotBlank
    @Size(min = 3, max = 200)
    @Filterable(types = {FilterType.LIKE, FilterType.EQUALS})
    private String name;

    @Size(max = 2000)
    private String description;

    @Column(unique = true)
    @Filterable(types = FilterType.EQUALS)
    private String sku;

    @Min(0)
    @Filterable(types = {FilterType.RANGE, FilterType.GREATER_THAN_OR_EQUAL})
    private BigDecimal price;

    @Min(0)
    @Filterable(types = FilterType.RANGE)
    private Integer stock;

    @Filterable(types = {FilterType.EQUALS, FilterType.IN})
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Filterable(types = {FilterType.EQUALS, FilterType.LIKE})
    private String category;

    @ReadOnly
    private LocalDateTime createdAt;

    @ReadOnly
    private LocalDateTime updatedAt;

    @Hidden
    private String supplierNotes;

    @Hidden
    private BigDecimal costPrice;
}
```

### User Management

```java
@Entity
@Table(name = "users")
@AutoApi(
    path = "/users",
    expose = Expose.CREATE_UPDATE,  // Pas de DELETE physique
    description = "User management",
    tags = {"Users", "Authentication"},
    security = @Security(
        level = SecurityLevel.ROLE_BASED,
        roles = {"ADMIN"}
    )
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ReadOnly
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true)
    @Filterable(types = {FilterType.EQUALS, FilterType.LIKE})
    private String username;

    @Email
    @NotBlank
    @Column(unique = true)
    @Filterable(types = FilterType.EQUALS)
    private String email;

    @Hidden  // Jamais exposé
    @Column(nullable = false)
    private String passwordHash;

    @Filterable(types = {FilterType.EQUALS, FilterType.IN})
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Filterable(types = FilterType.EQUALS)
    private Boolean active = true;

    @ReadOnly
    private LocalDateTime lastLogin;

    @ReadOnly
    private LocalDateTime createdAt;

    @Hidden
    private Integer failedLoginAttempts;

    @Hidden
    private String resetToken;
}
```

---

## :material-help-circle: Questions Fréquentes

### Puis-je combiner @Hidden et @ReadOnly?

Non, c'est redondant. `@Hidden` exclut déjà le champ des DTOs input ET output.

### Comment filtrer sur une relation?

```java
@ManyToOne
@JoinColumn(name = "category_id")
@Filterable(types = FilterType.EQUALS, paramName = "categoryId")
private Category category;
```

Utilisation: `?categoryId=5`

### FilterType.RANGE vs BETWEEN?

- **RANGE**: Utilise `?field_gte=X&field_lte=Y` (deux paramètres séparés)
- **BETWEEN**: Utilise `?field_between=X,Y` (un seul paramètre avec virgule)

Les deux génèrent le même SQL `BETWEEN X AND Y`.

### Comment désactiver la pagination?

```java
@AutoApi(path = "/config", pagination = false)
```

L'endpoint retournera toujours tous les résultats (attention aux performances!).

---

## :material-link: Voir Aussi

- [Quick Start](../getting-started/quickstart.md) - Premiers pas
- [Configuration](../guide/configuration.md) - Configuration complète
- [Filtrage](../guide/filtering.md) - Guide du filtrage dynamique
- [Sécurité](../guide/security.md) - Configuration de la sécurité
- [Custom Components](../advanced/custom-components.md) - Personnalisation avancée
