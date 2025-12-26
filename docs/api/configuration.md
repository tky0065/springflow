# Configuration Properties API Reference

Référence complète des propriétés de configuration SpringFlow.

## :material-information: Vue d'Ensemble

**Classe**: `io.springflow.starter.config.SpringFlowProperties`
**Prefix**: `springflow`
**Format**: YAML ou Properties
**Source**: Fichier `application.yml` ou `application.properties`

---

## :material-cog: Propriétés Racine

### `springflow.enabled`

Active ou désactive SpringFlow complètement.

| Attribut | Valeur |
|----------|--------|
| **Type** | `boolean` |
| **Défaut** | `true` |
| **Requis** | Non |
| **Exemple** | `enabled: true` |

**Description**: Lorsque défini à `false`, aucune API ne sera générée. Tous les composants SpringFlow sont court-circuités.

**Exemples**:

```yaml
# Production - activé
springflow:
  enabled: true
```

```yaml
# Tests - désactivé
springflow:
  enabled: false
```

```java
// Override pour un test spécifique
@SpringBootTest(properties = "springflow.enabled=false")
class MyTest { }
```

---

### `springflow.base-path`

Chemin de base préfixé à tous les endpoints générés.

| Attribut | Valeur |
|----------|--------|
| **Type** | `String` |
| **Défaut** | `"/api"` |
| **Requis** | Non |
| **Format** | Doit commencer par `/` ou être vide |
| **Exemple** | `base-path: /api/v1` |

**Description**: Tous les endpoints `@AutoApi(path = "/products")` seront préfixés par cette valeur.

**Comportement**:

| Configuration | @AutoApi(path = "/products") | Endpoint Résultant |
|---------------|------------------------------|---------------------|
| `/api` | `/products` | `/api/products` |
| `/api/v1` | `/products` | `/api/v1/products` |
| `/rest` | `/users` | `/rest/users` |
| `""` (vide) | `/products` | `/products` |

**Validation**:
- Doit être vide OU commencer par `/`
- Ne doit PAS se terminer par `/`

---

### `springflow.base-packages`

Packages à scanner pour les entités `@AutoApi`.

| Attribut | Valeur |
|----------|--------|
| **Type** | `String[]` (array) |
| **Défaut** | `[]` (vide - scan automatique) |
| **Requis** | Non |
| **Format** | Packages Java séparés par virgule |
| **Exemple** | `base-packages: [com.example.domain, com.example.entities]` |

**Description**: Si vide, utilise `AutoConfigurationPackages` de Spring Boot (package de la classe `@SpringBootApplication`).

**Exemples**:

```yaml
# Scan automatique (recommandé)
springflow:
  # base-packages: [] implicite
```

```yaml
# Packages explicites
springflow:
  base-packages:
    - com.example.myapp.domain
    - com.example.myapp.catalog
```

```yaml
# Format properties
springflow.base-packages[0]=com.example.domain
springflow.base-packages[1]=com.example.entities
```

---

## :material-page-layout-sidebar-left: springflow.pagination.*

Configuration de la pagination des endpoints générés.

### `springflow.pagination.default-page-size`

Taille de page par défaut si non spécifiée dans la requête.

| Attribut | Valeur |
|----------|--------|
| **Type** | `int` |
| **Défaut** | `20` |
| **Requis** | Non |
| **Min** | `1` |
| **Max** | Doit être ≤ `max-page-size` |
| **Exemple** | `default-page-size: 25` |

**Comportement**:
- `GET /api/products` → Retourne 20 éléments (défaut)
- `GET /api/products?size=50` → Retourne 50 éléments (override)

---

### `springflow.pagination.max-page-size`

Taille maximum autorisée pour une page.

| Attribut | Valeur |
|----------|--------|
| **Type** | `int` |
| **Défaut** | `100` |
| **Requis** | Non |
| **Min** | `1` |
| **Recommandé** | `100-1000` |
| **Exemple** | `max-page-size: 500` |

**Description**: Protection contre les requêtes trop gourmandes. Si `?size=X` dépasse cette valeur, la requête est limitée à `max-page-size`.

**Comportement**:
- `?size=50` (< 100) → OK, retourne 50
- `?size=200` (> 100) → Limité à 100

---

### `springflow.pagination.page-parameter`

Nom du paramètre de requête pour le numéro de page.

| Attribut | Valeur |
|----------|--------|
| **Type** | `String` |
| **Défaut** | `"page"` |
| **Requis** | Non |
| **Format** | Nom de paramètre valide |
| **Exemple** | `page-parameter: p` |

**Comportement**:

| Configuration | Requête |
|---------------|---------|
| `page` (défaut) | `?page=0` |
| `p` | `?p=0` |
| `pageNumber` | `?pageNumber=0` |

---

### `springflow.pagination.size-parameter`

Nom du paramètre de requête pour la taille de page.

| Attribut | Valeur |
|----------|--------|
| **Type** | `String` |
| **Défaut** | `"size"` |
| **Requis** | Non |
| **Format** | Nom de paramètre valide |
| **Exemple** | `size-parameter: limit` |

**Comportement**:

| Configuration | Requête |
|---------------|---------|
| `size` (défaut) | `?size=20` |
| `limit` | `?limit=20` |
| `pageSize` | `?pageSize=20` |

---

### `springflow.pagination.sort-parameter`

Nom du paramètre de requête pour le tri.

| Attribut | Valeur |
|----------|--------|
| **Type** | `String` |
| **Défaut** | `"sort"` |
| **Requis** | Non |
| **Format** | Nom de paramètre valide |
| **Exemple** | `sort-parameter: order` |

**Comportement**:

| Configuration | Requête |
|---------------|---------|
| `sort` (défaut) | `?sort=name,asc` |
| `order` | `?order=name,asc` |
| `sortBy` | `?sortBy=name,asc` |

**Format du tri**: `field,direction` où direction = `asc` ou `desc`

---

### `springflow.pagination.one-indexed-parameters`

Pagination commence à 0 (standard) ou 1.

| Attribut | Valeur |
|----------|--------|
| **Type** | `boolean` |
| **Défaut** | `false` |
| **Requis** | Non |
| **Exemple** | `one-indexed-parameters: true` |

**Comportement**:

| Configuration | Première Page | Deuxième Page |
|---------------|---------------|---------------|
| `false` (défaut) | `?page=0` | `?page=1` |
| `true` | `?page=1` | `?page=2` |

**Note**: Avec `one-indexed-parameters: true`, la page 1 retourne les premiers résultats.

---

## :material-file-document: springflow.swagger.*

Configuration de la documentation OpenAPI/Swagger.

### `springflow.swagger.enabled`

Active ou désactive Swagger UI et OpenAPI.

| Attribut | Valeur |
|----------|--------|
| **Type** | `boolean` |
| **Défaut** | `true` |
| **Requis** | Non |
| **Exemple** | `enabled: true` |

**Accès**: `http://localhost:8080/swagger-ui.html`

**Recommandation**: Désactiver en production pour raisons de sécurité.

---

### `springflow.swagger.title`

Titre de l'API affiché dans Swagger UI.

| Attribut | Valeur |
|----------|--------|
| **Type** | `String` |
| **Défaut** | `"SpringFlow API"` |
| **Requis** | Non |
| **Exemple** | `title: E-Commerce API` |

**Description**: Apparaît en haut de la page Swagger UI.

---

### `springflow.swagger.description`

Description de l'API affichée dans Swagger UI.

| Attribut | Valeur |
|----------|--------|
| **Type** | `String` |
| **Défaut** | `"Auto-generated REST API documentation"` |
| **Requis** | Non |
| **Format** | Supporte Markdown |
| **Exemple** | Voir ci-dessous |

**Exemple avec Markdown**:

```yaml
springflow:
  swagger:
    description: |
      ## My E-Commerce Platform API

      Cette API permet de gérer:
      - Catalogue produits
      - Commandes
      - Utilisateurs

      ### Authentification
      Bearer JWT tokens requis.
```

---

### `springflow.swagger.version`

Version de l'API affichée dans Swagger.

| Attribut | Valeur |
|----------|--------|
| **Type** | `String` |
| **Défaut** | `"1.0.0"` |
| **Requis** | Non |
| **Format** | SemVer recommandé |
| **Exemple** | `version: 2.3.1` |

**Exemples**:

```yaml
version: 1.0.0          # Release stable
version: 2.1.0-beta     # Pre-release
version: ${project.version}  # Dynamique depuis POM
```

---

### `springflow.swagger.contact-name`

Nom du contact pour l'API.

| Attribut | Valeur |
|----------|--------|
| **Type** | `String` |
| **Défaut** | `null` |
| **Requis** | Non |
| **Exemple** | `contact-name: API Support Team` |

**Affichage**: Lien de contact dans Swagger UI.

---

### `springflow.swagger.contact-email`

Email du contact pour l'API.

| Attribut | Valeur |
|----------|--------|
| **Type** | `String` |
| **Défaut** | `null` |
| **Requis** | Non |
| **Format** | Email valide |
| **Exemple** | `contact-email: api@example.com` |

**Affichage**: Lien `mailto:` dans Swagger UI.

---

### `springflow.swagger.contact-url`

URL du contact/support pour l'API.

| Attribut | Valeur |
|----------|--------|
| **Type** | `String` |
| **Défaut** | `null` |
| **Requis** | Non |
| **Format** | URL valide |
| **Exemple** | `contact-url: https://example.com/support` |

**Affichage**: Lien cliquable dans Swagger UI.

---

### `springflow.swagger.license-name`

Nom de la licence de l'API.

| Attribut | Valeur |
|----------|--------|
| **Type** | `String` |
| **Défaut** | `null` |
| **Requis** | Non |
| **Exemple** | `license-name: Apache 2.0` |

**Exemples courants**:
- `Apache 2.0`
- `MIT License`
- `Proprietary`
- `GNU GPL v3`

---

### `springflow.swagger.license-url`

URL vers le texte complet de la licence.

| Attribut | Valeur |
|----------|--------|
| **Type** | `String` |
| **Défaut** | `null` |
| **Requis** | Non |
| **Format** | URL valide |
| **Exemple** | `license-url: https://www.apache.org/licenses/LICENSE-2.0` |

---

## :material-file-code: Exemples Complets

### Configuration Minimale

```yaml
# Utilise toutes les valeurs par défaut
springflow:
  enabled: true
```

### Configuration Recommandée

```yaml
springflow:
  enabled: true
  base-path: /api

  pagination:
    default-page-size: 20
    max-page-size: 100

  swagger:
    enabled: true
    title: My Application API
    version: 1.0.0
```

### Configuration Complète

```yaml
springflow:
  # Core
  enabled: true
  base-path: /api/v1
  base-packages:
    - com.example.domain
    - com.example.entities

  # Pagination
  pagination:
    default-page-size: 25
    max-page-size: 200
    page-parameter: page
    size-parameter: size
    sort-parameter: sort
    one-indexed-parameters: false

  # Swagger/OpenAPI
  swagger:
    enabled: true
    title: E-Commerce REST API
    description: |
      Complete REST API for e-commerce platform.

      ## Features
      - Product catalog
      - Order management
      - Customer accounts

      ## Authentication
      Bearer JWT tokens required for all endpoints except public catalog.
    version: 2.5.1
    contact-name: API Support Team
    contact-email: api-support@example.com
    contact-url: https://example.com/api-docs
    license-name: Proprietary
    license-url: https://example.com/license
```

### Configuration par Environnement

```yaml
# application.yml
springflow:
  enabled: true
  base-path: /api

---
# application-dev.yml
springflow:
  pagination:
    default-page-size: 10
  swagger:
    enabled: true
    title: My API (DEV)

---
# application-prod.yml
springflow:
  pagination:
    default-page-size: 50
    max-page-size: 500
  swagger:
    enabled: false
```

### Format Properties

```properties
# application.properties

# Core
springflow.enabled=true
springflow.base-path=/api/v1
springflow.base-packages[0]=com.example.domain
springflow.base-packages[1]=com.example.entities

# Pagination
springflow.pagination.default-page-size=20
springflow.pagination.max-page-size=100
springflow.pagination.page-parameter=page
springflow.pagination.size-parameter=size
springflow.pagination.sort-parameter=sort
springflow.pagination.one-indexed-parameters=false

# Swagger
springflow.swagger.enabled=true
springflow.swagger.title=My API
springflow.swagger.description=Auto-generated REST API
springflow.swagger.version=1.0.0
springflow.swagger.contact-name=Support Team
springflow.swagger.contact-email=support@example.com
springflow.swagger.contact-url=https://example.com/support
springflow.swagger.license-name=Apache 2.0
springflow.swagger.license-url=https://www.apache.org/licenses/LICENSE-2.0
```

---

## :material-code-json: Configuration Programmatique

### Java Configuration

Bien que non recommandé, vous pouvez configurer via Java:

```java
@Configuration
public class SpringFlowConfig {

    @Bean
    @ConfigurationProperties(prefix = "springflow")
    public SpringFlowProperties springFlowProperties() {
        SpringFlowProperties props = new SpringFlowProperties();
        props.setEnabled(true);
        props.setBasePath("/api/v1");

        SpringFlowProperties.Pagination pagination = new SpringFlowProperties.Pagination();
        pagination.setDefaultPageSize(25);
        pagination.setMaxPageSize(200);
        props.setPagination(pagination);

        SpringFlowProperties.Swagger swagger = new SpringFlowProperties.Swagger();
        swagger.setEnabled(true);
        swagger.setTitle("My API");
        props.setSwagger(swagger);

        return props;
    }
}
```

**Note**: Préférez toujours la configuration via `application.yml`.

---

## :material-table: Tableau Récapitulatif

| Propriété | Type | Défaut | Description |
|-----------|------|--------|-------------|
| **Racine** ||||
| `enabled` | boolean | `true` | Active/désactive SpringFlow |
| `base-path` | String | `"/api"` | Préfixe des endpoints |
| `base-packages` | String[] | `[]` | Packages à scanner |
| **Pagination** ||||
| `pagination.default-page-size` | int | `20` | Taille par défaut |
| `pagination.max-page-size` | int | `100` | Taille maximum |
| `pagination.page-parameter` | String | `"page"` | Nom paramètre page |
| `pagination.size-parameter` | String | `"size"` | Nom paramètre taille |
| `pagination.sort-parameter` | String | `"sort"` | Nom paramètre tri |
| `pagination.one-indexed-parameters` | boolean | `false` | Pages 1-based |
| **Swagger** ||||
| `swagger.enabled` | boolean | `true` | Active Swagger UI |
| `swagger.title` | String | `"SpringFlow API"` | Titre API |
| `swagger.description` | String | `"Auto-generated..."` | Description |
| `swagger.version` | String | `"1.0.0"` | Version |
| `swagger.contact-name` | String | `null` | Nom contact |
| `swagger.contact-email` | String | `null` | Email contact |
| `swagger.contact-url` | String | `null` | URL contact |
| `swagger.license-name` | String | `null` | Nom licence |
| `swagger.license-url` | String | `null` | URL licence |

---

## :material-link: Voir Aussi

- [Guide Configuration](../guide/configuration.md) - Documentation utilisateur
- [Annotations API](annotations.md) - Configuration au niveau entité
- [Installation](../getting-started/installation.md) - Setup initial
- [Pagination](../guide/pagination.md) - Guide pagination
- **Code Source**: `SpringFlowProperties.java` dans `springflow-starter`
