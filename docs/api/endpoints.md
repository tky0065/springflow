# Generated Endpoints API Reference

Référence complète des endpoints REST générés automatiquement par SpringFlow.

## :material-information: Vue d'Ensemble

Pour chaque entité annotée avec `@AutoApi`, SpringFlow génère automatiquement **6 endpoints REST** suivant les conventions RESTful.

**Format de Base**:
```
{springflow.base-path}{@AutoApi.path}
```

**Exemple**:
- Configuration: `springflow.base-path: /api`
- Annotation: `@AutoApi(path = "/products")`
- **Résultat**: `/api/products`

---

## :material-api: Endpoints Générés

### 1. GET - Liste Paginée

Récupère une liste paginée d'entités avec support de tri et filtrage.

**Signature**:
```http
GET {base-path}/{entity-path}?page={page}&size={size}&sort={field,direction}
```

**Paramètres de Requête**:

| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `page` | int | `0` | Numéro de page (0-based par défaut) |
| `size` | int | `20` | Taille de la page |
| `sort` | string | - | Tri: `field,asc` ou `field,desc` |

**Headers de Réponse**:

| Header | Description |
|--------|-------------|
| `Content-Type` | `application/json` |
| `X-Total-Count` | Nombre total d'éléments |

**Corps de Réponse**:

```json
{
  "content": [
    {
      "id": 1,
      "name": "Product A",
      "price": 29.99,
      "createdAt": "2024-01-15T10:30:00"
    },
    {
      "id": 2,
      "name": "Product B",
      "price": 49.99,
      "createdAt": "2024-01-16T14:20:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 100,
  "last": false,
  "first": true,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 20,
  "empty": false
}
```

**Codes de Status**:

| Code | Description |
|------|-------------|
| `200 OK` | Succès |
| `400 Bad Request` | Paramètres invalides (ex: `size > max-page-size`) |
| `401 Unauthorized` | Authentification requise |
| `403 Forbidden` | Permissions insuffisantes |

**Exemples**:

```bash
# Première page avec taille par défaut
curl -X GET "http://localhost:8080/api/products"

# Page 2, 50 éléments par page
curl -X GET "http://localhost:8080/api/products?page=1&size=50"

# Trié par prix décroissant
curl -X GET "http://localhost:8080/api/products?sort=price,desc"

# Tri multiple: par nom ASC, puis prix DESC
curl -X GET "http://localhost:8080/api/products?sort=name,asc&sort=price,desc"

# Avec filtrage (si @Filterable configuré)
curl -X GET "http://localhost:8080/api/products?name_like=laptop&price_gte=100&price_lte=500"
```

---

### 2. GET - Par ID

Récupère une entité spécifique par son identifiant.

**Signature**:
```http
GET {base-path}/{entity-path}/{id}
```

**Paramètres de Chemin**:

| Paramètre | Type | Description |
|-----------|------|-------------|
| `id` | Long/String/Composite | Identifiant unique de l'entité |

**Corps de Réponse**:

```json
{
  "id": 1,
  "name": "Product A",
  "price": 29.99,
  "stock": 100,
  "category": "Electronics",
  "createdAt": "2024-01-15T10:30:00"
}
```

**Codes de Status**:

| Code | Description |
|------|-------------|
| `200 OK` | Entité trouvée |
| `404 Not Found` | Entité inexistante |
| `401 Unauthorized` | Authentification requise |
| `403 Forbidden` | Permissions insuffisantes |

**Exemples**:

```bash
# ID numérique
curl -X GET "http://localhost:8080/api/products/42"

# ID String (UUID)
curl -X GET "http://localhost:8080/api/users/550e8400-e29b-41d4-a716-446655440000"

# Avec authentification Bearer
curl -X GET "http://localhost:8080/api/orders/123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Réponse 404**:

```json
{
  "timestamp": "2024-01-20T15:30:45.123Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product with id 999 not found",
  "path": "/api/products/999"
}
```

---

### 3. POST - Créer

Crée une nouvelle entité.

**Signature**:
```http
POST {base-path}/{entity-path}
```

**Headers de Requête**:

| Header | Valeur |
|--------|--------|
| `Content-Type` | `application/json` |

**Corps de Requête**:

```json
{
  "name": "New Product",
  "price": 39.99,
  "stock": 50,
  "category": "Books"
}
```

**Notes**:
- Les champs `@Hidden` sont ignorés (même s'ils sont fournis)
- Les champs `@ReadOnly` sont ignorés (ID, timestamps, etc.)
- Les champs `@Id` avec `@GeneratedValue` sont auto-générés
- Les validations JSR-380 avec **groupe Create** sont appliquées (v0.4.0+)

**Corps de Réponse** (201 Created):

```json
{
  "id": 101,
  "name": "New Product",
  "price": 39.99,
  "stock": 50,
  "category": "Books",
  "createdAt": "2024-01-20T16:45:30"
}
```

**Headers de Réponse**:

| Header | Description |
|--------|-------------|
| `Location` | URI de la ressource créée: `/api/products/101` |
| `Content-Type` | `application/json` |

**Codes de Status**:

| Code | Description |
|------|-------------|
| `201 Created` | Entité créée avec succès |
| `400 Bad Request` | Données invalides (validation échouée) |
| `401 Unauthorized` | Authentification requise |
| `403 Forbidden` | Permissions insuffisantes |
| `409 Conflict` | Contrainte d'unicité violée |

**Exemples**:

```bash
# Création basique
curl -X POST "http://localhost:8080/api/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Pro",
    "price": 1299.99,
    "stock": 10,
    "category": "Electronics"
  }'

# Avec authentification
curl -X POST "http://localhost:8080/api/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "customerId": 5,
    "totalAmount": 299.99,
    "items": [
      {"productId": 1, "quantity": 2}
    ]
  }'
```

**Réponse 400 - Validation Échouée**:

```json
{
  "timestamp": "2024-01-20T16:45:30.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "name",
      "message": "Name is required",
      "rejectedValue": null
    },
    {
      "field": "price",
      "message": "Price must be greater than 0",
      "rejectedValue": -10.0
    }
  ],
  "path": "/api/products"
}
```

**Réponse 409 - Contrainte d'Unicité**:

```json
{
  "timestamp": "2024-01-20T16:45:30.123Z",
  "status": 409,
  "error": "Conflict",
  "message": "Product with name 'Laptop Pro' already exists",
  "path": "/api/products"
}
```

---

### 4. PUT - Mise à Jour Complète

Remplace complètement une entité existante.

**Signature**:
```http
PUT {base-path}/{entity-path}/{id}
```

**Headers de Requête**:

| Header | Valeur |
|--------|--------|
| `Content-Type` | `application/json` |

**Corps de Requête**:

```json
{
  "name": "Updated Product Name",
  "price": 49.99,
  "stock": 75,
  "category": "Electronics"
}
```

**Comportement**:
- **Tous les champs** doivent être fournis (sauf `@ReadOnly` et `@Hidden`)
- Les champs omis sont mis à `null` (sauf contraintes NOT NULL)
- Les champs `@ReadOnly` sont ignorés
- Les validations avec **groupe Update** sont appliquées (v0.4.0+)

**Corps de Réponse** (200 OK):

```json
{
  "id": 42,
  "name": "Updated Product Name",
  "price": 49.99,
  "stock": 75,
  "category": "Electronics",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-20T17:00:00"
}
```

**Codes de Status**:

| Code | Description |
|------|-------------|
| `200 OK` | Mise à jour réussie |
| `400 Bad Request` | Données invalides |
| `404 Not Found` | Entité inexistante |
| `401 Unauthorized` | Authentification requise |
| `403 Forbidden` | Permissions insuffisantes |
| `409 Conflict` | Contrainte violée |

**Exemples**:

```bash
# Mise à jour complète
curl -X PUT "http://localhost:8080/api/products/42" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Pro 2024",
    "price": 1499.99,
    "stock": 20,
    "category": "Electronics"
  }'
```

---

### 5. PATCH - Mise à Jour Partielle

Met à jour seulement les champs fournis.

**Signature**:
```http
PATCH {base-path}/{entity-path}/{id}
```

**Headers de Requête**:

| Header | Valeur |
|--------|--------|
| `Content-Type` | `application/json` |

**Corps de Requête**:

```json
{
  "price": 44.99,
  "stock": 80
}
```

**Comportement**:
- **Seuls les champs fournis** sont mis à jour
- Les champs omis conservent leur valeur actuelle
- Les champs `null` sont traités comme "ne pas modifier" (pas comme "mettre à null")
- Les champs `@ReadOnly` sont **rejetés** avec erreur 400 (v0.4.0+)
- Les champs `@Hidden` sont **rejetés** avec erreur 400 (v0.4.0+)
- Les validations sont appliquées avec **groupe Update** (v0.4.0+)

**Corps de Réponse** (200 OK):

```json
{
  "id": 42,
  "name": "Product A",
  "price": 44.99,
  "stock": 80,
  "category": "Electronics",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-20T17:15:00"
}
```

**Codes de Status**:

| Code | Description |
|------|-------------|
| `200 OK` | Mise à jour réussie |
| `400 Bad Request` | Données invalides |
| `404 Not Found` | Entité inexistante |
| `401 Unauthorized` | Authentification requise |
| `403 Forbidden` | Permissions insuffisantes |
| `409 Conflict` | Contrainte violée |

**Exemples**:

```bash
# Mettre à jour seulement le prix
curl -X PATCH "http://localhost:8080/api/products/42" \
  -H "Content-Type: application/json" \
  -d '{"price": 39.99}'

# Mettre à jour plusieurs champs
curl -X PATCH "http://localhost:8080/api/products/42" \
  -H "Content-Type: application/json" \
  -d '{
    "price": 39.99,
    "stock": 150,
    "category": "Books"
  }'
```

**Différence PUT vs PATCH**:

| Aspect | PUT | PATCH |
|--------|-----|-------|
| **Champs requis** | Tous (sauf @ReadOnly) | Seulement ceux à modifier |
| **Champs omis** | Mis à null | Inchangés |
| **Use Case** | Remplacement complet | Modification partielle |
| **Idempotence** | Oui | Oui |

#### Validation de Champs (v0.4.0+)

!!! info "Nouveau depuis v0.4.0"
    SpringFlow valide maintenant strictement les champs dans les requêtes PATCH, en rejetant les tentatives de modification de champs protégés.

**Protection des Champs @Hidden**:

```java
@Entity
@AutoApi(path = "users")
public class User {
    @Id
    private Long id;
    private String name;

    @Hidden
    private String passwordHash;  // Protégé contre les modifications
}
```

**Requête rejetée**:
```bash
curl -X PATCH "http://localhost:8080/api/users/1" \
  -H "Content-Type: application/json" \
  -d '{"passwordHash": "hacked"}'  # ❌ Rejeté
```

**Réponse 400**:
```json
{
  "timestamp": "2025-12-27T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot update hidden field: passwordHash"
}
```

**Protection des Champs @ReadOnly**:

```java
@Entity
@AutoApi(path = "products")
public class Product {
    @Id
    private Long id;
    private String name;

    @ReadOnly
    private LocalDateTime createdAt;  // Protégé contre les modifications
}
```

**Requête rejetée**:
```bash
curl -X PATCH "http://localhost:8080/api/products/1" \
  -H "Content-Type: application/json" \
  -d '{"createdAt": "2020-01-01T00:00:00"}'  # ❌ Rejeté
```

**Réponse 400**:
```json
{
  "timestamp": "2025-12-27T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot update read-only field: createdAt"
}
```

#### Validation Groups (v0.4.0+)

PATCH applique automatiquement le **groupe Update** pour les validations JSR-380.

**Exemple avec Validation Conditionnelle**:

```java
@Entity
@AutoApi(path = "products")
public class Product {
    @Id
    private Long id;

    @NotBlank(groups = {Create.class, Update.class})
    private String name;

    @NotNull(groups = Create.class, message = "Category required on creation")
    private String initialCategory;  // Requis à la création, optionnel en update

    @Email(groups = Update.class, message = "Supplier email must be valid")
    private String supplierEmail;  // Validé UNIQUEMENT en update
}
```

**PATCH avec validation Update**:
```bash
curl -X PATCH "http://localhost:8080/api/products/1" \
  -H "Content-Type: application/json" \
  -d '{
    "supplierEmail": "invalid-email"  # ❌ Validation échoue (groupe Update)
  }'
```

**Réponse 400**:
```json
{
  "timestamp": "2025-12-27T10:30:00",
  "status": 400,
  "errors": [
    {
      "field": "supplierEmail",
      "message": "Supplier email must be valid",
      "rejectedValue": "invalid-email",
      "code": "Email",
      "validationGroup": "Update"
    }
  ]
}
```

**PATCH réussi**:
```bash
curl -X PATCH "http://localhost:8080/api/products/1" \
  -H "Content-Type: application/json" \
  -d '{
    "supplierEmail": "supplier@example.com"  # ✅ Valide
  }'
```

**Note**: Le champ `initialCategory` n'est **pas** validé en PATCH car il n'appartient qu'au groupe `Create`.

---

### 6. DELETE - Suppression

Supprime une entité.

**Signature**:
```http
DELETE {base-path}/{entity-path}/{id}
```

**Paramètres de Chemin**:

| Paramètre | Type | Description |
|-----------|------|-------------|
| `id` | Long/String/Composite | Identifiant de l'entité à supprimer |

**Corps de Réponse**:

Aucun (204 No Content) ou confirmation JSON selon configuration.

**Codes de Status**:

| Code | Description |
|------|-------------|
| `204 No Content` | Suppression réussie |
| `404 Not Found` | Entité inexistante |
| `401 Unauthorized` | Authentification requise |
| `403 Forbidden` | Permissions insuffisantes |
| `409 Conflict` | Contraintes référentielles (FK) |

**Exemples**:

```bash
# Suppression simple
curl -X DELETE "http://localhost:8080/api/products/42"

# Avec authentification
curl -X DELETE "http://localhost:8080/api/orders/123" \
  -H "Authorization: Bearer TOKEN"
```

**Réponse 409 - Contrainte Référentielle**:

```json
{
  "timestamp": "2024-01-20T17:30:00.123Z",
  "status": 409,
  "error": "Conflict",
  "message": "Cannot delete product 42: referenced by 5 active orders",
  "path": "/api/products/42"
}
```

**Note**: With `@SoftDelete`, deletion is logical (soft delete) rather than physical.

---

## :material-filter: Filtrage Dynamique

Si des champs sont annotés avec `@Filterable`, des paramètres de requête supplémentaires sont disponibles sur l'endpoint GET liste.

### Syntaxe des Filtres

| FilterType | Paramètre | Exemple |
|------------|-----------|---------|
| **EQUALS** | `{field}` | `?status=ACTIVE` |
| **LIKE** | `{field}_like` | `?name_like=laptop` |
| **GREATER_THAN** | `{field}_gt` | `?price_gt=100` |
| **LESS_THAN** | `{field}_lt` | `?price_lt=500` |
| **GREATER_THAN_OR_EQUAL** | `{field}_gte` | `?price_gte=100` |
| **LESS_THAN_OR_EQUAL** | `{field}_lte` | `?price_lte=500` |
| **RANGE** | `{field}_gte` + `{field}_lte` | `?price_gte=100&price_lte=500` |
| **IN** | `{field}_in` | `?status_in=ACTIVE,PENDING` |
| **NOT_IN** | `{field}_not_in` | `?status_not_in=DELETED` |
| **IS_NULL** | `{field}_null` | `?deletedAt_null=true` |
| **BETWEEN** | `{field}_between` | `?createdAt_between=2024-01-01,2024-12-31` |

### Exemples de Filtrage

**Entité avec Filtres**:

```java
@Entity
@AutoApi(path = "/products")
public class Product {
    @Id
    private Long id;

    @Filterable(types = {FilterType.LIKE, FilterType.EQUALS})
    private String name;

    @Filterable(types = FilterType.RANGE)
    private BigDecimal price;

    @Filterable(types = {FilterType.EQUALS, FilterType.IN})
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Filterable(types = FilterType.RANGE)
    private LocalDate releaseDate;
}
```

**Requêtes Filtrées**:

```bash
# Recherche par nom (contient "laptop")
curl -X GET "http://localhost:8080/api/products?name_like=laptop"

# Produits entre 100€ et 500€
curl -X GET "http://localhost:8080/api/products?price_gte=100&price_lte=500"

# Produits actifs ou en cours
curl -X GET "http://localhost:8080/api/products?status_in=ACTIVE,PENDING"

# Combinaison de filtres
curl -X GET "http://localhost:8080/api/products?name_like=laptop&price_gte=500&status=ACTIVE"

# Produits sans date de suppression (non supprimés)
curl -X GET "http://localhost:8080/api/products?deletedAt_null=true"

# Produits sortis en 2024
curl -X GET "http://localhost:8080/api/products?releaseDate_between=2024-01-01,2024-12-31"
```

**Filtrage + Pagination + Tri**:

```bash
curl -X GET "http://localhost:8080/api/products?name_like=laptop&price_gte=500&page=0&size=20&sort=price,asc"
```

---

## :material-cog: Contrôle d'Exposition

L'annotation `@AutoApi(expose = ...)` contrôle quels endpoints sont générés.

### Expose.ALL (Par Défaut)

Génère les 6 endpoints:

```java
@AutoApi(path = "/products", expose = Expose.ALL)
```

**Endpoints**:
- ✅ `GET /api/products` - Liste
- ✅ `GET /api/products/{id}` - Par ID
- ✅ `POST /api/products` - Créer
- ✅ `PUT /api/products/{id}` - Mise à jour complète
- ✅ `PATCH /api/products/{id}` - Mise à jour partielle
- ✅ `DELETE /api/products/{id}` - Supprimer

### Expose.READ_ONLY

Lecture seule (GET uniquement):

```java
@AutoApi(path = "/reports", expose = Expose.READ_ONLY)
```

**Endpoints**:
- ✅ `GET /api/reports` - Liste
- ✅ `GET /api/reports/{id}` - Par ID
- ❌ POST, PUT, PATCH, DELETE

### Expose.CREATE_UPDATE

Pas de suppression physique:

```java
@AutoApi(path = "/customers", expose = Expose.CREATE_UPDATE)
```

**Endpoints**:
- ✅ `GET /api/customers` - Liste
- ✅ `GET /api/customers/{id}` - Par ID
- ✅ `POST /api/customers` - Créer
- ✅ `PUT /api/customers/{id}` - Mise à jour complète
- ✅ `PATCH /api/customers/{id}` - Mise à jour partielle
- ❌ DELETE

**Use Case**: Combined with `@SoftDelete` for logical deletion only.

### Expose.CUSTOM

Aucun endpoint généré (implémentation custom complète):

```java
@AutoApi(path = "/admin/settings", expose = Expose.CUSTOM)
```

**Endpoints**: Aucun (vous devez implémenter votre propre controller).

---

## :material-shield-lock: Sécurité des Endpoints

### SecurityLevel.PUBLIC (Par Défaut)

Accessible sans authentification:

```java
@AutoApi(path = "/products")  // Implicite: PUBLIC
```

**Tous les endpoints** sont publics.

### SecurityLevel.AUTHENTICATED

Authentification requise:

```java
@AutoApi(
    path = "/orders",
    security = @Security(level = SecurityLevel.AUTHENTICATED)
)
```

**Effet**:
- Header `Authorization` requis
- Utilisateur connecté nécessaire
- Tous les endpoints (GET, POST, PUT, PATCH, DELETE) protégés

### SecurityLevel.ROLE_BASED

Rôles spécifiques requis:

```java
@AutoApi(
    path = "/admin/users",
    security = @Security(
        level = SecurityLevel.ROLE_BASED,
        roles = {"ADMIN", "USER_MANAGER"}
    )
)
```

**Effet**:
- Utilisateur doit avoir un des rôles spécifiés
- Vérifié via Spring Security
- Tous les endpoints protégés

### Sécurité Granulaire

Lecture publique, écriture protégée:

```java
@AutoApi(
    path = "/products",
    security = @Security(
        readLevel = SecurityLevel.PUBLIC,
        writeLevel = SecurityLevel.ROLE_BASED,
        roles = {"ADMIN", "INVENTORY_MANAGER"}
    )
)
```

**Effet**:
- ✅ `GET /api/products` - Public
- ✅ `GET /api/products/{id}` - Public
- 🔒 `POST /api/products` - Requiert rôle ADMIN ou INVENTORY_MANAGER
- 🔒 `PUT /api/products/{id}` - Requiert rôle ADMIN ou INVENTORY_MANAGER
- 🔒 `PATCH /api/products/{id}` - Requiert rôle ADMIN ou INVENTORY_MANAGER
- 🔒 `DELETE /api/products/{id}` - Requiert rôle ADMIN ou INVENTORY_MANAGER

**Réponse 401 Unauthorized**:

```json
{
  "timestamp": "2024-01-20T18:00:00.123Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/orders"
}
```

**Réponse 403 Forbidden**:

```json
{
  "timestamp": "2024-01-20T18:00:00.123Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied: requires role ADMIN",
  "path": "/api/admin/users"
}
```

---

## :material-code-json: Format des DTOs

SpringFlow utilise des **DTOs dynamiques** basés sur `Map<String, Object>` au lieu de classes DTO fixes.

### DTO de Sortie (Output DTO)

Contient tous les champs **sauf**:
- Champs annotés `@Hidden`
- Champs transient ou statiques

**Exemple**:

```java
@Entity
public class User {
    @Id
    private Long id;

    private String username;

    private String email;

    @Hidden
    private String passwordHash;

    @ReadOnly
    private LocalDateTime createdAt;
}
```

**DTO Retourné (GET)**:

```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "createdAt": "2024-01-15T10:30:00"
}
```

❌ `passwordHash` n'est **jamais** dans la réponse.

### DTO d'Entrée (Input DTO)

Contient tous les champs **sauf**:
- Champs annotés `@Hidden`
- Champs annotés `@ReadOnly`
- Champs annotés `@Id` (pour POST)
- Champs transient ou statiques

**DTO Attendu (POST)**:

```json
{
  "username": "johndoe",
  "email": "john@example.com"
}
```

❌ `id` est auto-généré, ignoré si fourni.
❌ `createdAt` est en lecture seule, ignoré si fourni.
❌ `passwordHash` est caché, ignoré si fourni.

**DTO Attendu (PUT/PATCH)**:

```json
{
  "username": "johndoe_updated",
  "email": "newemail@example.com"
}
```

✅ `id` est dans l'URL, pas dans le corps.
❌ `createdAt` est en lecture seule, ignoré si fourni.

---

## :material-format-list-numbered: Format de Pagination

### Réponse Page Spring Data

SpringFlow retourne le format standard `Page<T>` de Spring Data:

```json
{
  "content": [...],           // Array des entités
  "pageable": {
    "pageNumber": 0,          // Numéro de page actuelle
    "pageSize": 20,           // Taille de la page
    "sort": {...},            // Info de tri
    "offset": 0,              // Offset global
    "paged": true,
    "unpaged": false
  },
  "totalPages": 10,           // Nombre total de pages
  "totalElements": 200,       // Nombre total d'éléments
  "last": false,              // Dernière page?
  "first": true,              // Première page?
  "size": 20,                 // Taille de la page
  "number": 0,                // Numéro de page
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 20,     // Éléments dans cette page
  "empty": false              // Page vide?
}
```

### Navigation entre Pages

**Première Page**:
```bash
GET /api/products?page=0&size=20
```

**Page Suivante**:
```bash
GET /api/products?page=1&size=20
```

**Dernière Page** (calculée depuis `totalPages`):
```bash
GET /api/products?page=9&size=20  # Si totalPages = 10
```

**Détection de Fin**:
- `last: true` → Pas de page suivante
- `first: true` → Pas de page précédente
- `numberOfElements < size` → Possiblement dernière page

---

## :material-alert: Gestion des Erreurs

### Format Standard des Erreurs

SpringFlow utilise le format d'erreur standard de Spring Boot:

```json
{
  "timestamp": "2024-01-20T18:30:00.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/products"
}
```

### Erreurs de Validation (400)

```json
{
  "timestamp": "2024-01-20T18:30:00.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email should be valid",
      "rejectedValue": "invalid-email"
    },
    {
      "field": "age",
      "message": "Age must be at least 18",
      "rejectedValue": 15
    }
  ],
  "path": "/api/users"
}
```

### Codes de Status Communs

| Code | Nom | Quand |
|------|-----|-------|
| **200** | OK | GET, PUT, PATCH réussis |
| **201** | Created | POST réussi |
| **204** | No Content | DELETE réussi |
| **400** | Bad Request | Validation échouée, données invalides |
| **401** | Unauthorized | Authentification manquante |
| **403** | Forbidden | Permissions insuffisantes |
| **404** | Not Found | Entité inexistante |
| **409** | Conflict | Contrainte d'unicité, contrainte FK |
| **500** | Internal Server Error | Erreur serveur inattendue |

---

## :material-check-all: Exemples Complets par Scénario

### Scénario 1: CRUD Basique (Produits)

**Entité**:

```java
@Entity
@AutoApi(path = "/products")
public class Product {
    @Id @GeneratedValue
    private Long id;

    @NotBlank
    private String name;

    @Min(0)
    private BigDecimal price;

    private Integer stock;
}
```

**Opérations**:

```bash
# 1. Créer un produit
curl -X POST "http://localhost:8080/api/products" \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptop", "price": 999.99, "stock": 10}'

# Réponse: {"id": 1, "name": "Laptop", "price": 999.99, "stock": 10}

# 2. Lister les produits
curl -X GET "http://localhost:8080/api/products?page=0&size=10"

# 3. Récupérer le produit #1
curl -X GET "http://localhost:8080/api/products/1"

# 4. Mettre à jour le prix (PATCH)
curl -X PATCH "http://localhost:8080/api/products/1" \
  -H "Content-Type: application/json" \
  -d '{"price": 899.99}'

# 5. Supprimer le produit
curl -X DELETE "http://localhost:8080/api/products/1"
```

### Scénario 2: Filtrage et Pagination (Blog)

**Entité**:

```java
@Entity
@AutoApi(path = "/posts")
public class BlogPost {
    @Id @GeneratedValue
    private Long id;

    @Filterable(types = FilterType.LIKE)
    private String title;

    @Filterable(types = {FilterType.EQUALS, FilterType.IN})
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Filterable(types = FilterType.RANGE)
    private LocalDate publishedAt;

    @ReadOnly
    private Integer viewCount;
}
```

**Requêtes**:

```bash
# Articles publiés
curl -X GET "http://localhost:8080/api/posts?status=PUBLISHED"

# Recherche "spring" dans le titre
curl -X GET "http://localhost:8080/api/posts?title_like=spring"

# Articles publiés en janvier 2024
curl -X GET "http://localhost:8080/api/posts?publishedAt_gte=2024-01-01&publishedAt_lte=2024-01-31"

# Combinaison: publiés + recherche + tri par vues
curl -X GET "http://localhost:8080/api/posts?status=PUBLISHED&title_like=spring&sort=viewCount,desc"

# Page 2, 20 par page
curl -X GET "http://localhost:8080/api/posts?status=PUBLISHED&page=1&size=20"
```

### Scénario 3: Sécurité (Commandes)

**Entité**:

```java
@Entity
@AutoApi(
    path = "/orders",
    security = @Security(level = SecurityLevel.AUTHENTICATED)
)
public class Order {
    @Id @GeneratedValue
    private Long id;

    @NotNull
    private Long customerId;

    @Min(0)
    private BigDecimal totalAmount;

    @ReadOnly
    private LocalDateTime createdAt;
}
```

**Requêtes**:

```bash
# ❌ Échoue sans authentification
curl -X GET "http://localhost:8080/api/orders"
# Réponse: 401 Unauthorized

# ✅ Avec token JWT
curl -X GET "http://localhost:8080/api/orders" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# ✅ Créer une commande (authentifié)
curl -X POST "http://localhost:8080/api/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"customerId": 5, "totalAmount": 299.99}'
```

### Scénario 4: Lecture Seule (Rapports)

**Entité**:

```java
@Entity
@AutoApi(
    path = "/reports",
    expose = Expose.READ_ONLY
)
public class Report {
    @Id @GeneratedValue
    private Long id;

    private String title;

    @ReadOnly
    private LocalDateTime generatedAt;
}
```

**Endpoints Disponibles**:

```bash
# ✅ GET liste
curl -X GET "http://localhost:8080/api/reports"

# ✅ GET par ID
curl -X GET "http://localhost:8080/api/reports/1"

# ❌ POST non disponible (405 Method Not Allowed)
curl -X POST "http://localhost:8080/api/reports" \
  -H "Content-Type: application/json" \
  -d '{"title": "Test"}'
# Réponse: 405 Method Not Allowed

# ❌ DELETE non disponible
curl -X DELETE "http://localhost:8080/api/reports/1"
# Réponse: 405 Method Not Allowed
```

---

## :material-link: Voir Aussi

- **[Annotations API Reference](annotations.md)** - Référence complète des annotations
- **[Configuration API Reference](configuration.md)** - Toutes les propriétés de configuration
- **[Guide Annotations](../guide/annotations.md)** - Guide utilisateur des annotations
- **[Quick Start](../getting-started/quickstart.md)** - Premiers pas
- **[First Project](../getting-started/first-project.md)** - Tutoriel complet

---

## :material-frequently-asked-questions: Questions Fréquentes

### Comment personnaliser le format des réponses?

Par défaut, SpringFlow retourne le format `Page<T>` de Spring Data. Pour personnaliser, implémentez votre propre controller en étendant `GenericCrudController`.

### Les endpoints supportent-ils HATEOAS?

Not currently supported. HATEOAS support via Spring HATEOAS is planned for a future release.

### Comment gérer les relations dans les DTOs?

Les relations `@ManyToOne` sont incluses par défaut (ID de l'entité liée). Pour inclure l'objet complet, utilisez des projections custom ou étendez le controller.

### Peut-on avoir des endpoints custom en plus des CRUD?

Oui! Créez un controller custom qui étend `GenericCrudController` et ajoutez vos propres méthodes `@GetMapping`, `@PostMapping`, etc.

### Comment désactiver certains endpoints (ex: DELETE)?

Utilisez `@AutoApi(expose = Expose.CREATE_UPDATE)` pour désactiver DELETE, ou `Expose.READ_ONLY` pour désactiver toutes les écritures.

### Les validations JSR-380 sont-elles appliquées?

Oui! Toutes les annotations de validation (`@NotNull`, `@Min`, `@Email`, etc.) sont appliquées automatiquement sur POST, PUT et PATCH.

### Comment tester les endpoints?

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **cURL**: Exemples dans cette documentation
- **Postman/Insomnia**: Importez le fichier OpenAPI
- **Tests Spring**: `@SpringBootTest` + `MockMvc`
