# Configuration Guide

Guide complet de configuration de SpringFlow pour personnaliser le comportement de votre API.

## :material-cog: Configuration de Base

### Configuration Minimale

SpringFlow fonctionne avec **zéro configuration**! Il suffit d'ajouter la dépendance:

```yaml
# Aucune configuration nécessaire
# SpringFlow utilise les valeurs par défaut
```

### Configuration Recommandée

Pour un contrôle plus fin, ajoutez ces paramètres dans `application.yml`:

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
    description: Auto-generated REST API
    version: 1.0.0
```

---

## :material-toggle-switch: Activation / Désactivation

### Activer SpringFlow

```yaml
springflow:
  enabled: true
```

**Valeur par défaut**: `true`

### Désactiver Complètement

Utile pour les tests ou certains profils:

```yaml
springflow:
  enabled: false
```

Avec cette configuration, **aucune** API ne sera générée.

### Désactivation par Profil

```yaml
# application-test.yml
springflow:
  enabled: false
```

```bash
# Lancer avec profil test
./mvnw spring-boot:run -Dspring.profiles.active=test
```

---

## :material-api: Chemin de Base (Base Path)

### Configuration du Chemin de Base

```yaml
springflow:
  base-path: /api
```

**Valeur par défaut**: `/api`

**Résultat**:
- `@AutoApi(path = "/products")` → `/api/products`
- `@AutoApi(path = "/orders")` → `/api/orders`

### Exemples de Configuration

#### API Versionnée

```yaml
springflow:
  base-path: /api/v1
```

Résultat: `/api/v1/products`, `/api/v1/orders`

#### Pas de Préfixe

```yaml
springflow:
  base-path: ""
```

Résultat: `/products`, `/orders` (directement à la racine)

#### Chemin Custom

```yaml
springflow:
  base-path: /rest/public
```

Résultat: `/rest/public/products`

---

## :material-package-variant: Scan de Packages

### Scan Automatique (Recommandé)

Par défaut, SpringFlow scanne les packages de votre application automatiquement:

```yaml
# Pas de configuration nécessaire
# Utilise AutoConfigurationPackages de Spring Boot
```

### Packages Explicites

Pour cibler des packages spécifiques:

```yaml
springflow:
  base-packages:
    - com.example.myapp.domain
    - com.example.myapp.entities
```

**Use Cases**:
- Multi-modules avec entités dans plusieurs packages
- Exclusion de certains packages
- Performance (scan plus rapide)

### Exemple Multi-Modules

```yaml
springflow:
  base-packages:
    - com.example.core.domain
    - com.example.catalog.entities
    - com.example.orders.models
```

---

## :material-page-layout-sidebar-left: Pagination

### Configuration Complète

```yaml
springflow:
  pagination:
    default-page-size: 20          # Taille par défaut
    max-page-size: 100              # Taille maximum autorisée
    page-parameter: page            # Nom du paramètre de page
    size-parameter: size            # Nom du paramètre de taille
    sort-parameter: sort            # Nom du paramètre de tri
    one-indexed-parameters: false   # Pagination commence à 0 ou 1
```

### Paramètres Détaillés

#### default-page-size

```yaml
springflow:
  pagination:
    default-page-size: 20
```

**Valeur par défaut**: `20`

Taille de page utilisée si l'utilisateur ne spécifie pas `?size=X`.

**Exemples**:
- `GET /api/products` → 20 résultats
- `GET /api/products?page=0` → 20 résultats
- `GET /api/products?size=50` → 50 résultats

#### max-page-size

```yaml
springflow:
  pagination:
    max-page-size: 100
```

**Valeur par défaut**: `100`

Taille maximum autorisée. Protège contre les requêtes trop gourmandes.

**Comportement**:
- `?size=50` → OK, retourne 50 résultats
- `?size=200` → Limité à 100 résultats

#### page-parameter, size-parameter, sort-parameter

Personnaliser les noms des paramètres de requête:

```yaml
springflow:
  pagination:
    page-parameter: p       # ?p=0
    size-parameter: limit   # ?limit=20
    sort-parameter: order   # ?order=name,asc
```

**Requête avec configuration par défaut**:
```
GET /api/products?page=1&size=20&sort=name,asc
```

**Requête avec configuration custom**:
```
GET /api/products?p=1&limit=20&order=name,asc
```

#### one-indexed-parameters

```yaml
springflow:
  pagination:
    one-indexed-parameters: true
```

**Valeur par défaut**: `false` (commence à 0)

- `false`: Première page = `?page=0` (standard Spring Data)
- `true`: Première page = `?page=1` (plus intuitif pour certains)

### Exemples de Configuration

#### Configuration Haute Performance

Pour APIs internes avec gros volumes:

```yaml
springflow:
  pagination:
    default-page-size: 100
    max-page-size: 1000
```

#### Configuration API Publique

Pour limiter la charge:

```yaml
springflow:
  pagination:
    default-page-size: 10
    max-page-size: 50
```

#### Compatible avec Systèmes Legacy

```yaml
springflow:
  pagination:
    page-parameter: pageNumber
    size-parameter: pageSize
    sort-parameter: sortBy
    one-indexed-parameters: true
```

Résultat: `?pageNumber=1&pageSize=25&sortBy=name,asc`

---

## :material-file-document: Swagger / OpenAPI

### Configuration Complète

```yaml
springflow:
  swagger:
    enabled: true
    title: My Application API
    description: Complete REST API documentation
    version: 1.0.0
    contact-name: API Support Team
    contact-email: api@example.com
    contact-url: https://example.com/support
    license-name: Apache 2.0
    license-url: https://www.apache.org/licenses/LICENSE-2.0
```

### Paramètres Détaillés

#### enabled

```yaml
springflow:
  swagger:
    enabled: true
```

**Valeur par défaut**: `true`

Active/désactive la documentation Swagger UI.

**Accès**: `http://localhost:8080/swagger-ui.html`

#### Métadonnées de l'API

```yaml
springflow:
  swagger:
    title: E-Commerce API
    description: |
      API REST complète pour la gestion de catalogue produits,
      commandes et clients.

      ## Authentification
      Utilise JWT Bearer tokens.

      ## Rate Limiting
      1000 requêtes par heure.
    version: 2.1.0
```

Ces informations apparaissent en haut de Swagger UI.

#### Informations de Contact

```yaml
springflow:
  swagger:
    contact-name: Support Technique
    contact-email: support@mycompany.com
    contact-url: https://mycompany.com/api-support
```

Affiche un lien de contact dans Swagger UI.

#### Licence

```yaml
springflow:
  swagger:
    license-name: MIT License
    license-url: https://opensource.org/licenses/MIT
```

### Exemples de Configuration

#### Configuration Open Source

```yaml
springflow:
  swagger:
    enabled: true
    title: SpringFlow Demo API
    description: Demonstration API showcasing SpringFlow capabilities
    version: 0.1.0-SNAPSHOT
    license-name: Apache License 2.0
    license-url: https://www.apache.org/licenses/LICENSE-2.0
```

#### Configuration Entreprise

```yaml
springflow:
  swagger:
    enabled: true
    title: Internal CRM API
    description: Customer Relationship Management API - Internal Use Only
    version: 3.2.1
    contact-name: Architecture Team
    contact-email: architecture@company.com
    contact-url: https://wiki.company.com/api-docs
    license-name: Proprietary
    license-url: https://company.com/license
```

#### Désactivation en Production

```yaml
# application-prod.yml
springflow:
  swagger:
    enabled: false
```

**Raison**: Cacher la documentation en production pour des raisons de sécurité.

---

## :material-file-multiple: Configuration par Environnement

### Structure Recommandée

```
src/main/resources/
├── application.yml              # Configuration commune
├── application-dev.yml          # Développement
├── application-test.yml         # Tests
└── application-prod.yml         # Production
```

### application.yml (Base)

```yaml
spring:
  application:
    name: my-application

springflow:
  enabled: true
  base-path: /api

  pagination:
    page-parameter: page
    size-parameter: size
    sort-parameter: sort
```

### application-dev.yml

```yaml
springflow:
  pagination:
    default-page-size: 10   # Petites pages pour debug
    max-page-size: 50

  swagger:
    enabled: true
    title: My API (DEV)
    description: Development environment
    version: ${project.version}-SNAPSHOT
```

### application-test.yml

```yaml
springflow:
  enabled: false  # Désactivé pour tests unitaires
```

### application-prod.yml

```yaml
springflow:
  pagination:
    default-page-size: 50
    max-page-size: 500

  swagger:
    enabled: false  # Caché en production
```

### Activation

```bash
# Développement (par défaut)
./mvnw spring-boot:run

# Test
./mvnw spring-boot:run -Dspring.profiles.active=test

# Production
./mvnw spring-boot:run -Dspring.profiles.active=prod
```

---

## :material-check-all: Configuration Complète Exemple

### Application Réelle

```yaml
# application.yml
spring:
  application:
    name: ecommerce-api

  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

springflow:
  enabled: true
  base-path: /api/v1

  base-packages:
    - com.example.ecommerce.domain.catalog
    - com.example.ecommerce.domain.orders
    - com.example.ecommerce.domain.customers

  pagination:
    default-page-size: 25
    max-page-size: 200
    one-indexed-parameters: false

  swagger:
    enabled: true
    title: E-Commerce REST API
    description: |
      ## E-Commerce Platform API

      Cette API permet de gérer:
      - Catalogue produits
      - Commandes clients
      - Gestion utilisateurs

      ### Authentification
      Utilise OAuth2 Bearer tokens.

      ### Rate Limiting
      - Utilisateurs authentifiés: 5000 req/h
      - Utilisateurs anonymes: 100 req/h
    version: 2.5.1
    contact-name: API Team
    contact-email: api-team@example.com
    contact-url: https://example.com/api-docs
    license-name: Proprietary
    license-url: https://example.com/license

# Logging
logging:
  level:
    io.springflow: INFO
    com.example.ecommerce: DEBUG
```

---

## :material-hammer-wrench: Configuration Avancée

### Propriétés Spring Boot Compatibles

SpringFlow s'intègre avec la configuration Spring Boot standard:

```yaml
spring:
  data:
    web:
      pageable:
        default-page-size: 20
        max-page-size: 100
```

**Note**: Les valeurs `springflow.pagination.*` prennent le dessus sur `spring.data.web.pageable.*`.

### Variables d'Environnement

Toutes les propriétés peuvent être surchargées via variables d'environnement:

```bash
# Format: SPRINGFLOW_PROPERTY_NAME

export SPRINGFLOW_ENABLED=true
export SPRINGFLOW_BASE_PATH=/api/v2
export SPRINGFLOW_PAGINATION_DEFAULT_PAGE_SIZE=50
export SPRINGFLOW_SWAGGER_TITLE="My Custom API"
```

### application.properties (Alternative)

Si vous préférez le format `.properties`:

```properties
# springflow-starter/src/main/resources/application.properties

springflow.enabled=true
springflow.base-path=/api
springflow.base-packages=com.example.domain

springflow.pagination.default-page-size=20
springflow.pagination.max-page-size=100
springflow.pagination.page-parameter=page
springflow.pagination.size-parameter=size
springflow.pagination.sort-parameter=sort
springflow.pagination.one-indexed-parameters=false

springflow.swagger.enabled=true
springflow.swagger.title=My API
springflow.swagger.description=Auto-generated REST API
springflow.swagger.version=1.0.0
springflow.swagger.contact-name=Support
springflow.swagger.contact-email=support@example.com
```

---

## :material-frequently-asked-questions: Questions Fréquentes

### Comment désactiver SpringFlow pour certains tests?

```java
@SpringBootTest(properties = "springflow.enabled=false")
class MyTest {
    // ...
}
```

### Comment avoir des pages de 1 à N au lieu de 0 à N-1?

```yaml
springflow:
  pagination:
    one-indexed-parameters: true
```

### Puis-je avoir des configurations différentes par entité?

Non, la configuration est globale. Utilisez `@AutoApi(pagination = false)` pour désactiver la pagination sur une entité spécifique.

### Comment accéder à Swagger UI?

Par défaut: `http://localhost:8080/swagger-ui.html`

Avec custom port: `http://localhost:9090/swagger-ui.html`

### La configuration s'applique-t-elle aux composants custom?

Partiellement. Le `base-path` s'applique si vous utilisez `@RequestMapping` sans chemin complet. Les composants custom ont un contrôle total sur leur comportement.

---

## :material-link: Voir Aussi

- **[Référence API Configuration](../api/configuration.md)** - Documentation complète des propriétés
- **[Installation](../getting-started/installation.md)** - Premier pas avec SpringFlow
- **[Annotations](annotations.md)** - Configuration au niveau entité
- **[Pagination](pagination.md)** - Guide détaillé de la pagination
- **[Custom Components](../advanced/custom-components.md)** - Personnalisation avancée

---

## :material-table: Résumé des Propriétés

| Propriété | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `enabled` | boolean | `true` | Active/désactive SpringFlow |
| `base-path` | String | `/api` | Chemin de base des endpoints |
| `base-packages` | String[] | `[]` | Packages à scanner |
| **Pagination** ||||
| `default-page-size` | int | `20` | Taille de page par défaut |
| `max-page-size` | int | `100` | Taille maximum autorisée |
| `page-parameter` | String | `page` | Nom du paramètre de page |
| `size-parameter` | String | `size` | Nom du paramètre de taille |
| `sort-parameter` | String | `sort` | Nom du paramètre de tri |
| `one-indexed-parameters` | boolean | `false` | Pages commencent à 1 si true |
| **Swagger** ||||
| `enabled` | boolean | `true` | Active Swagger UI |
| `title` | String | `SpringFlow API` | Titre de l'API |
| `description` | String | `Auto-generated...` | Description |
| `version` | String | `1.0.0` | Version de l'API |
| `contact-name` | String | `null` | Nom du contact |
| `contact-email` | String | `null` | Email du contact |
| `contact-url` | String | `null` | URL du contact |
| `license-name` | String | `null` | Nom de la licence |
| `license-url` | String | `null` | URL de la licence |

[Voir la référence API complète →](../api/configuration.md)
