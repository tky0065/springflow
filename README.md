# SpringFlow 🚀

[![Build Status](https://img.shields.io/github/workflow/status/springflow/springflow/CI)](https://github.com/springflow/springflow/actions)
[![Maven Central](https://img.shields.io/maven-central/v/io.springflow/springflow-starter)](https://search.maven.org/artifact/io.springflow/springflow-starter)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Coverage](https://img.shields.io/codecov/c/github/springflow/springflow)](https://codecov.io/gh/springflow/springflow)

> **Générez automatiquement des REST APIs CRUD complètes pour vos entités JPA avec une seule annotation.**

SpringFlow est une bibliothèque Spring Boot qui automatise la création de repositories, services, controllers REST, DTOs et documentation OpenAPI à partir de vos entités JPA. Écrivez moins de code boilerplate, concentrez-vous sur votre logique métier.

---

## ✨ Fonctionnalités principales

- 🎯 **Une annotation, API complète** : `@AutoApi` génère tout automatiquement
- 🔄 **CRUD complet** : GET, POST, PUT, PATCH, DELETE
- 📄 **Pagination & Tri** : Intégrés nativement avec Spring Data
- 🔍 **Filtres dynamiques** : Filtrage automatique sur les champs annotés
- 📚 **Documentation automatique** : Swagger/OpenAPI généré automatiquement
- ✅ **Validation** : Support complet JSR-380
- 🔐 **Sécurité** : Intégration Spring Security avec contrôle par rôles
- 🗑️ **Soft Delete** : Suppression logique avec possibilité de restauration
- 🎨 **Support Java & Kotlin** : Compatible avec les deux langages
- 🚀 **Zéro configuration** : Fonctionne out-of-the-box avec Spring Boot

---

## 🚀 Quick Start (5 minutes)

### 1. Ajoutez la dépendance

**Maven** :
```xml
<dependency>
    <groupId>io.springflow</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle** :
```gradle
implementation 'io.springflow:springflow-starter:1.0.0'
```

### 2. Activez SpringFlow

```java
@SpringBootApplication
@EnableSpringFlow(basePackages = "com.example.entities")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 3. Annotez vos entités

```java
@Entity
@AutoApi(path = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Min(0)
    private BigDecimal price;

    @Filterable(types = {FilterType.LIKE, FilterType.EQUALS})
    private String category;

    // Getters & Setters
}
```

### 4. C'est tout ! 🎉

Démarrez votre application et votre API REST est prête :

```bash
# Lister tous les produits (avec pagination)
GET /api/products?page=0&size=20

# Filtrer et trier
GET /api/products?category_like=Electro&sort=price,desc

# Récupérer un produit
GET /api/products/{id}

# Créer un produit
POST /api/products
Content-Type: application/json
{
  "name": "Laptop",
  "price": 999.99,
  "category": "Electronics"
}

# Mettre à jour
PUT /api/products/{id}

# Mettre à jour partiellement
PATCH /api/products/{id}

# Supprimer
DELETE /api/products/{id}
```

**Documentation Swagger** disponible sur : `http://localhost:8080/swagger-ui.html`

---

## 📖 Documentation complète

### Table des matières

- [Installation](#-installation)
- [Configuration](#️-configuration)
- [Annotations](#-annotations)
- [Filtres dynamiques](#-filtres-dynamiques)
- [Pagination & Tri](#-pagination--tri)
- [Validation](#-validation)
- [Sécurité](#-sécurité)
- [Soft Delete](#️-soft-delete)
- [Personnalisation](#-personnalisation)
- [Support Kotlin](#-support-kotlin)
- [Exemples](#-exemples)

---

## 📥 Installation

### Prérequis

- Java 17 ou supérieur
- Spring Boot 3.2+
- Spring Data JPA
- Base de données (H2, PostgreSQL, MySQL, etc.)

### Configuration Maven complète

```xml
<dependencies>
    <!-- SpringFlow -->
    <dependency>
        <groupId>io.springflow</groupId>
        <artifactId>springflow-starter</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Database (exemple avec H2) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

---

## ⚙️ Configuration

### Configuration minimale

**application.yml** :
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update

springflow:
  enabled: true
  base-packages: com.example.entities
```

### Configuration avancée

```yaml
springflow:
  enabled: true
  base-path: /api/v1                    # Préfixe global des endpoints
  base-packages:                         # Packages à scanner
    - com.example.entities
    - com.example.models

  pagination:
    default-size: 20                     # Taille par défaut des pages
    max-size: 100                        # Taille maximale

  filtering:
    case-sensitive: false                # Filtres case-insensitive
    default-operator: AND                # Opérateur par défaut

  swagger:
    enabled: true                        # Activer Swagger UI
    title: "My API"
    description: "API Documentation"
    version: "1.0.0"

  soft-delete:
    enabled: true                        # Activer soft delete global
    field-name: deleted
    timestamp-field: deletedAt

  security:
    enabled: false                       # Sécurité globale (désactivée par défaut)
```

---

## 🏷️ Annotations

### `@AutoApi`

Active la génération automatique de l'API REST pour une entité.

```java
@Entity
@AutoApi(
    path = "users",                      // Chemin de l'endpoint
    expose = Expose.ALL,                 // Quelles opérations exposer
    pagination = true,                   // Activer pagination
    sorting = true,                      // Activer tri
    description = "User management API"  // Description pour Swagger
)
public class User {
    // ...
}
```

**Paramètres** :
- `path` : Chemin de l'API (défaut : nom de la classe en minuscules + 's')
- `expose` : `ALL`, `CREATE_UPDATE`, `READ_ONLY` (défaut : `ALL`)
- `pagination` : Activer/désactiver la pagination (défaut : `true`)
- `sorting` : Activer/désactiver le tri (défaut : `true`)
- `description` : Description pour la documentation

### `@Filterable`

Active le filtrage dynamique sur un champ.

```java
@Entity
@AutoApi(path = "products")
public class Product {
    @Id
    @GeneratedValue
    private Long id;

    @Filterable(types = {FilterType.LIKE, FilterType.EQUALS})
    private String name;

    @Filterable(types = {FilterType.GREATER_THAN, FilterType.LESS_THAN, FilterType.RANGE})
    private BigDecimal price;

    @Filterable(types = FilterType.IN)
    private String category;
}
```

**Types de filtres disponibles** :
- `EQUALS` : Égalité exacte
- `LIKE` : Recherche partielle (avec wildcards)
- `GREATER_THAN` : Supérieur à
- `LESS_THAN` : Inférieur à
- `GREATER_THAN_OR_EQUAL` : Supérieur ou égal
- `LESS_THAN_OR_EQUAL` : Inférieur ou égal
- `RANGE` : Entre deux valeurs
- `IN` : Dans une liste de valeurs
- `IS_NULL` : Est null
- `IS_NOT_NULL` : N'est pas null

### `@Hidden`

Exclut un champ des DTOs générés (Input et Output).

```java
@Entity
@AutoApi(path = "users")
public class User {
    @Id
    private Long id;

    private String username;

    @Hidden  // Ne sera pas exposé dans l'API
    private String password;

    @Hidden  // Champ interne uniquement
    private String internalToken;
}
```

### `@SoftDelete`

Active la suppression logique (soft delete).

```java
@Entity
@AutoApi(path = "articles")
@SoftDelete
public class Article {
    @Id
    private Long id;

    private String title;
    private String content;

    // Champs ajoutés automatiquement par SpringFlow
    // private Boolean deleted;
    // private LocalDateTime deletedAt;
}
```

**Endpoints générés** :
- `DELETE /api/articles/{id}` → Soft delete (deleted = true)
- `POST /api/articles/{id}/restore` → Restauration (deleted = false)
- `GET /api/articles?includeDeleted=true` → Inclure les supprimés

---

## 🔍 Filtres dynamiques

### Syntaxe des filtres

```bash
# Filtre simple (EQUALS)
GET /api/products?category=Electronics

# Filtre LIKE (recherche partielle)
GET /api/products?name_like=Lap

# Filtre numérique
GET /api/products?price_gt=100&price_lt=500

# Filtre RANGE
GET /api/products?price_between=100,500

# Filtre IN (liste de valeurs)
GET /api/products?category_in=Electronics,Books,Toys

# Filtre NULL
GET /api/products?discount_null=true

# Combinaison de filtres (AND par défaut)
GET /api/products?category=Electronics&price_lt=1000&name_like=Samsung
```

### Opérateurs disponibles

| Opérateur | Syntaxe | Exemple |
|-----------|---------|---------|
| Égalité | `?field=value` | `?status=ACTIVE` |
| Like | `?field_like=value` | `?name_like=John` |
| Supérieur | `?field_gt=value` | `?age_gt=18` |
| Supérieur ou égal | `?field_gte=value` | `?price_gte=100` |
| Inférieur | `?field_lt=value` | `?age_lt=65` |
| Inférieur ou égal | `?field_lte=value` | `?price_lte=500` |
| Range | `?field_between=min,max` | `?age_between=18,65` |
| In | `?field_in=v1,v2,v3` | `?status_in=ACTIVE,PENDING` |
| Is Null | `?field_null=true` | `?email_null=true` |
| Is Not Null | `?field_null=false` | `?email_null=false` |

---

## 📄 Pagination & Tri

### Pagination

```bash
# Page 0, taille 20 (défaut)
GET /api/products

# Page spécifique
GET /api/products?page=2&size=50

# Taille maximum respectée (configuré dans application.yml)
GET /api/products?size=1000  # Limité à max-size (100)
```

**Format de réponse** :
```json
{
  "content": [
    { "id": 1, "name": "Product 1" },
    { "id": 2, "name": "Product 2" }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": true },
    "offset": 0
  },
  "totalElements": 150,
  "totalPages": 8,
  "last": false,
  "size": 20,
  "number": 0,
  "first": true,
  "numberOfElements": 20,
  "empty": false
}
```

### Tri

```bash
# Tri simple ascendant
GET /api/products?sort=name

# Tri ascendant explicite
GET /api/products?sort=name,asc

# Tri descendant
GET /api/products?sort=price,desc

# Tri multi-champs
GET /api/products?sort=category,asc&sort=price,desc

# Tri + Pagination
GET /api/products?page=0&size=20&sort=createdAt,desc
```

---

## ✅ Validation

SpringFlow supporte automatiquement toutes les annotations JSR-380.

### Exemple d'entité avec validation

```java
@Entity
@AutoApi(path = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email(message = "Email must be valid")
    private String email;

    @Min(value = 18, message = "Must be at least 18 years old")
    @Max(value = 120)
    private Integer age;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
    private String phone;

    @NotNull
    @PastOrPresent
    private LocalDate birthDate;
}
```

### Réponse d'erreur de validation

```json
{
  "timestamp": "2025-12-18T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email must be valid",
      "rejectedValue": "invalid-email"
    },
    {
      "field": "age",
      "message": "Must be at least 18 years old",
      "rejectedValue": 15
    }
  ]
}
```

---

## 🔐 Sécurité

### Configuration de la sécurité

```java
@Entity
@AutoApi(
    path = "admin/users",
    security = @Security(
        enabled = true,
        roles = {"ADMIN", "MANAGER"}
    )
)
public class User {
    // ...
}
```

### Sécurité granulaire par opération

```java
@Entity
@AutoApi(path = "products")
public class Product {
    // Endpoint publics pour lecture
    // Endpoints sécurisés pour création/modification/suppression
}

// Dans votre configuration Spring Security
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                .requestMatchers("/api/**").authenticated()
            )
            .httpBasic();
        return http.build();
    }
}
```

### Intégration JWT

```java
@AutoApi(
    path = "secure/data",
    security = @Security(
        enabled = true,
        roles = {"USER"}
    )
)
```

Requête avec JWT :
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/secure/data
```

---

## 🗑️ Soft Delete

### Activation

```java
@Entity
@AutoApi(path = "posts")
@SoftDelete
public class Post {
    @Id
    private Long id;
    private String title;
    private String content;
}
```

### Utilisation

```bash
# Suppression logique
DELETE /api/posts/1
# Réponse: 204 No Content
# L'entité existe toujours en base avec deleted=true

# Restauration
POST /api/posts/1/restore
# Réponse: 200 OK
# L'entité est restaurée avec deleted=false

# Liste sans les supprimés (défaut)
GET /api/posts
# Retourne uniquement les posts non supprimés

# Liste avec les supprimés
GET /api/posts?includeDeleted=true
# Retourne tous les posts

# Liste uniquement les supprimés
GET /api/posts?deletedOnly=true
# Retourne uniquement les posts supprimés

# Suppression définitive (si autorisée)
DELETE /api/posts/1?hard=true
# Suppression physique de la base de données
```

---

## 🎨 Personnalisation

### Ajouter des endpoints personnalisés

```java
@RestController
@RequestMapping("/api/users")
public class UserController extends GenericCrudController<User, Long> {

    // Les endpoints CRUD standards sont hérités automatiquement

    // Ajoutez vos endpoints personnalisés
    @GetMapping("/active")
    public ResponseEntity<List<User>> findActiveUsers() {
        // Votre logique
        return ResponseEntity.ok(activeUsers);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<User> activateUser(@PathVariable Long id) {
        // Votre logique
        return ResponseEntity.ok(activatedUser);
    }

    // Override un endpoint standard si nécessaire
    @Override
    public ResponseEntity<UserOutputDTO> findById(Long id) {
        // Votre logique personnalisée
        return super.findById(id);
    }
}
```

### Service personnalisé

```java
@Service
public class UserService extends GenericCrudService<User, Long> {

    // Méthodes CRUD héritées automatiquement

    // Ajoutez votre logique métier
    public List<User> findActiveUsers() {
        return repository.findByActiveTrue();
    }

    @Transactional
    public User activateUser(Long id) {
        User user = findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setActive(true);
        return repository.save(user);
    }

    // Hooks de lifecycle
    @Override
    protected void beforeCreate(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
    }

    @Override
    protected void afterUpdate(User user) {
        // Envoyer un email, logger, etc.
    }
}
```

---

## 🔷 Support Kotlin

SpringFlow fonctionne parfaitement avec Kotlin et les data classes.

### Exemple Kotlin

```kotlin
@Entity
@AutoApi(path = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank
    @field:Filterable(types = [FilterType.LIKE, FilterType.EQUALS])
    val name: String,

    @field:Min(0)
    val price: BigDecimal,

    @field:Filterable(types = [FilterType.IN])
    val category: String? = null,

    val description: String? = null
)
```

### Application Kotlin

```kotlin
@SpringBootApplication
@EnableSpringFlow(basePackages = ["com.example.entities"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
```

### Nullable types

```kotlin
data class User(
    val id: Long? = null,
    val name: String,           // Non-null
    val email: String?,         // Nullable
    val age: Int? = null        // Nullable avec default
)
```

---

## 📚 Exemples

### E-Commerce complet

```java
// Product.java
@Entity
@AutoApi(path = "products")
@SoftDelete
public class Product {
    @Id @GeneratedValue
    private Long id;

    @NotBlank
    @Filterable(types = {FilterType.LIKE, FilterType.EQUALS})
    private String name;

    @Min(0)
    @Filterable(types = FilterType.RANGE)
    private BigDecimal price;

    @Filterable(types = FilterType.IN)
    private String category;

    @ManyToOne
    private Brand brand;

    private Integer stock;
}

// Order.java
@Entity
@AutoApi(
    path = "orders",
    security = @Security(enabled = true, roles = {"USER"})
)
public class Order {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @NotNull
    private OrderStatus status;

    private LocalDateTime orderedAt;
}

// Category.java
@Entity
@AutoApi(path = "categories")
public class Category {
    @Id @GeneratedValue
    private Long id;

    @NotBlank
    private String name;

    @ManyToOne
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children;
}
```

### Utilisation de l'API

```bash
# Recherche de produits
GET /api/products?category_in=Electronics,Books&price_between=10,100&sort=price,asc

# Création de commande (authentifié)
POST /api/orders
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json
{
  "userId": 1,
  "items": [
    { "productId": 10, "quantity": 2 },
    { "productId": 15, "quantity": 1 }
  ],
  "status": "PENDING"
}

# Liste des commandes de l'utilisateur
GET /api/orders?userId=1&sort=orderedAt,desc
```

---

## 🛠️ Troubleshooting

### Problème : Les endpoints ne sont pas générés

**Solution** :
1. Vérifiez que `@EnableSpringFlow` est présent sur votre classe principale
2. Vérifiez que le package des entités est dans `basePackages`
3. Vérifiez que vos entités ont `@Entity` ET `@AutoApi`
4. Activez les logs debug :
```yaml
logging:
  level:
    io.springflow: DEBUG
```

### Problème : Erreur 404 sur les endpoints

**Solution** :
- Vérifiez le base-path configuré
- Vérifiez le path dans `@AutoApi`
- URL complète : `http://localhost:8080/{base-path}/{entity-path}`

### Problème : Validation ne fonctionne pas

**Solution** :
- Ajoutez `spring-boot-starter-validation` dans vos dépendances
- Utilisez `@Valid` dans vos controllers custom
- Vérifiez que les annotations sont sur les champs ou getters

### Problème : Performance avec beaucoup d'entités

**Solution** :
```yaml
springflow:
  cache:
    enabled: true
    max-size: 1000
  lazy-loading:
    enabled: true
```

---

## 🤝 Contribuer

Nous accueillons les contributions ! Consultez [CONTRIBUTING.md](CONTRIBUTING.md) pour les guidelines.

### Développement local

```bash
# Cloner le repository
git clone https://github.com/springflow/springflow.git
cd springflow

# Build
./mvnw clean install

# Run tests
./mvnw test

# Run demo
cd springflow-demo
./mvnw spring-boot:run
```

---

## 📝 Changelog

Voir [CHANGELOG.md](CHANGELOG.md) pour l'historique des versions.

---

## 📄 License

SpringFlow est sous licence [Apache License 2.0](LICENSE).

---

## 🌟 Support

- 📖 **Documentation** : [docs.springflow.io](https://docs.springflow.io)
- 💬 **Discord** : [Rejoindre la communauté](https://discord.gg/springflow)
- 🐛 **Issues** : [GitHub Issues](https://github.com/springflow/springflow/issues)
- 📧 **Email** : support@springflow.io

---

## 🎯 Roadmap

- ✅ Phase 1 (v1.0) : MVP avec CRUD, pagination, filtres, validation
- 🔄 Phase 2 (v2.0) : GraphQL, Security avancée, Soft Delete, Audit
- 📅 Phase 3 (v3.0) : Admin UI, CLI, Multi-DB, Monitoring

Voir [roadmap.md](roadmap.md) pour plus de détails.

---

## ⭐ Star History

Si vous aimez SpringFlow, n'oubliez pas de mettre une ⭐ sur GitHub !

[![Star History Chart](https://api.star-history.com/svg?repos=springflow/springflow&type=Date)](https://star-history.com/#springflow/springflow&Date)

---

**Fait avec ❤️ par l'équipe SpringFlow**
