# SpringFlow 🚀

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-25%2B%20(17%2B)-orange)](https://www.oracle.com/java/technologies/javase-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0%2B-brightgreen)](https://spring.io/projects/spring-boot)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.5.0-blue)](https://search.maven.org/artifact/io.github.tky0065/springflow-starter)

> **Générez automatiquement des REST APIs CRUD complètes pour vos entités JPA avec une seule annotation.**

SpringFlow est une bibliothèque Spring Boot qui automatise la création de repositories, services, controllers REST, DTOs et documentation OpenAPI à partir de vos entités JPA. Écrivez moins de code boilerplate, concentrez-vous sur votre logique métier.

---

## ✨ Fonctionnalités

### Phase 1 - MVP (v0.1.x) ✅

- 🎯 **Une annotation, API complète** : `@AutoApi` génère tout automatiquement
- 🔄 **CRUD complet** : GET (list + by ID), POST, PUT, DELETE
- 📄 **Pagination & Tri** : Intégrés nativement avec Spring Data
- 🔒 **DTO automatiques** : Support @Hidden et @ReadOnly
- 📚 **Documentation automatique** : Swagger/OpenAPI généré automatiquement
- ✅ **Validation** : Support complet JSR-380 (NotNull, NotBlank, Size, Min, Max, Email, Pattern)
- 🎨 **Support Java & Kotlin** : Compatible avec les deux langages
- 🚀 **Zéro configuration** : Auto-configuration Spring Boot, aucune annotation requise

### Phase 2 - Advanced Features (v0.2.0) ✅

- 🔍 **Filtres dynamiques** : Requêtes paramétrables avec JPA Specifications (EQUALS, LIKE, RANGE, IN, BETWEEN, etc.)
- 🧩 **JPA Specifications** : Support avancé pour critères de recherche complexes via endpoint dédié `/search`
- 🔐 **Sécurité intégrée** : Support Spring Security avec @PreAuthorize dynamique, contrôle par rôle et permission
- 🗑️ **Soft Delete** : Suppression logique avec support de restauration via `@SoftDelete`
- 📊 **Audit Trail** : Traçabilité complète avec `@Auditable` (createdAt, updatedAt, createdBy, updatedBy)
- 🔗 **Relations avancées** : Mapping automatique OneToMany, ManyToOne, ManyToMany avec gestion N+1
- 🎯 **Controllers personnalisés** : Détection et intégration de controllers custom
- 📦 **DTO nested** : Support relations imbriquées avec profondeur configurable

### Phase 3 - Extended Ecosystem (v0.3.0+) 🚀

- ✅ **GraphQL Support** : API GraphQL auto-générée avec queries, mutations et pagination (opt-in)

#### v0.3.2 - Critical Bugfixes 🐛

- 🔧 **EntityValidator Bean** : Configuration automatique du bean EntityValidator
- 📊 **JaCoCo Upgrade** : Version 0.8.12 avec meilleure compatibilité Hibernate
- 🔒 **Invoice Collision Fix** : Génération UUID pour numéros de facture uniques

#### v0.3.1 - Phase 2 Enhancements ⚡

- 🛡️ **PATCH Security** : Protection @Hidden/@ReadOnly avec validation stricte des champs
- 🔖 **Validation Groups** : Validation contextuelle (Create/Update) avec JSR-380
- 🗺️ **Advanced DTO Mapping** : Configuration profondeur (SHALLOW/DEFAULT/DEEP), détection cycles

### 🚧 Prochainement
- 💻 Admin UI React/Vue
- 🛠️ CLI tool pour génération de code
- 🗄️ Support multi-DB (MongoDB, etc.)
- 📈 Monitoring & Metrics avec Actuator

---

## 🚀 Quick Start (5 minutes)

### 1. Ajoutez la dépendance

**Maven** :
```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.5.0</version>
</dependency>

<!-- Optional: GraphQL Support -->
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-graphql</artifactId>
    <version>0.5.0</version>
</dependency>
```

**Gradle** :
```gradle
implementation 'io.github.tky0065:springflow-starter:0.5.0'

// Optional: GraphQL Support
implementation 'io.github.tky0065:springflow-graphql:0.5.0'
```

### 2. Annotez vos entités

```java
@Entity
@Table(name = "products")
@AutoApi(
    path = "/products",
    description = "Product management API"
)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @NotNull
    @Min(0)
    private Double price;

    @ReadOnly
    private LocalDateTime createdAt;

    // Getters & Setters (ou utilisez Lombok @Data)
}
```

### 3. C'est tout ! 🎉

Démarrez votre application et votre API REST est prête :

```bash
# Lister tous les produits (avec pagination)
GET /api/products?page=0&size=20

# Récupérer un produit
GET /api/products/1

# Créer un produit
POST /api/products
Content-Type: application/json
{
  "name": "Laptop",
  "price": 999.99
}

# Mettre à jour (complète)
PUT /api/products/1
{
  "name": "Laptop Pro",
  "price": 1299.99
}

# Mettre à jour (partielle) - v0.3.1+
PATCH /api/products/1
{
  "price": 899.99
}

# Supprimer
DELETE /api/products/1
```

**Documentation Swagger** disponible sur : `http://localhost:8080/swagger-ui.html`

---

## 📖 Documentation complète

### Table des matières

- [Installation](#-installation)
- [Configuration](#️-configuration)
- [Annotations](#-annotations)
- [Pagination & Tri](#-pagination--tri)
- [Validation](#-validation)
- [Personnalisation](#-personnalisation)
- [Support Kotlin](#-support-kotlin)
- [Exemples](#-exemples)

---

## 📥 Installation

### Prérequis

- Java 25 ou supérieur (compatible avec Java 17+)
- Spring Boot 4.0+
- Spring Data JPA
- Base de données (H2, PostgreSQL, MySQL, etc.)

### Configuration Maven complète

```xml
<dependencies>
    <!-- SpringFlow -->
    <dependency>
        <groupId>io.github.tky0065</groupId>
        <artifactId>springflow-starter</artifactId>
        <version>0.1.1-SNAPSHOT</version>
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

    <!-- Lombok (optionnel mais recommandé) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## ⚙️ Configuration

### Configuration minimale

SpringFlow s'active automatiquement via Spring Boot auto-configuration. **Aucune annotation requise!**

**application.yml** :
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update
    defer-datasource-initialization: true

# Configuration SpringFlow (toutes optionnelles)
springflow:
  enabled: true                          # Défaut: true
  base-path: /api                        # Défaut: /api
```

### Configuration avancée

```yaml
springflow:
  enabled: true
  base-path: /api/v1                    # Préfixe global des endpoints

  pagination:
    default-page-size: 20                # Taille par défaut des pages
    max-page-size: 100                   # Taille maximale
    page-parameter: page                 # Nom du paramètre page
    size-parameter: size                 # Nom du paramètre size
    sort-parameter: sort                 # Nom du paramètre sort
    one-indexed-parameters: false        # Pagination 0-indexed (défaut)

  swagger:
    enabled: true                        # Activer Swagger UI
    title: "SpringFlow API"
    description: "Auto-generated REST API"
    version: "1.0.0"
    contact:
      name: "API Support"
      email: "support@example.com"

  # GraphQL Support (requires springflow-graphql dependency)
  graphql:
    enabled: true                        # Activer le support GraphQL
    graphiql-enabled: true               # Activer GraphiQL UI
    schema-location: src/main/resources/graphql

# IMPORTANT: Configuration Spring GraphQL requise
spring:
  graphql:
    graphiql:
      enabled: true                      # Active Spring Boot GraphQL auto-configuration
```

> ⚠️ **Note importante pour GraphQL**: Lorsque vous utilisez `springflow-graphql`, vous devez également configurer `spring.graphql.graphiql.enabled=true` pour activer l'auto-configuration Spring Boot GraphQL. Sans cette configuration, l'application ne démarrera pas correctement.

---

## 🏷️ Annotations

### `@AutoApi`

Active la génération automatique de l'API REST pour une entité.

```java
@Entity
@AutoApi(
    path = "/users",                     // Chemin de l'endpoint (défaut: /nomEntité)
    description = "User management API", // Description pour Swagger
    tags = {"Users", "Authentication"}   // Tags Swagger
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
}
```

**Endpoints générés automatiquement** :
- `GET /api/users` - Liste avec pagination
- `GET /api/users/{id}` - Récupérer par ID
- `POST /api/users` - Créer
- `PUT /api/users/{id}` - Mettre à jour
- `DELETE /api/users/{id}` - Supprimer

### `@Hidden`

Exclut un champ des DTOs générés (Input et Output).

```java
@Entity
@AutoApi(path = "/users")
public class User {
    @Id
    private Long id;

    private String username;

    @Hidden  // ❌ Ne sera JAMAIS exposé dans l'API
    private String password;

    @Hidden  // ❌ Champ interne uniquement
    private String internalToken;
}
```

**Résultat** : Les champs `password` et `internalToken` n'apparaissent ni en input (POST/PUT) ni en output (GET).

### `@ReadOnly`

Champ visible en output (GET) mais pas accepté en input (POST/PUT).

```java
@Entity
@AutoApi(path = "/products")
public class Product {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ReadOnly  // ✅ Visible en GET, ❌ ignoré en POST/PUT
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ReadOnly
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### `@Filterable` (Phase 2 - à venir)

Active le filtrage dynamique sur un champ.

```java
@Entity
@AutoApi(path = "/products")
public class Product {
    @Filterable(types = {FilterType.LIKE, FilterType.EQUALS})
    private String name;

    @Filterable(types = {FilterType.GREATER_THAN, FilterType.LESS_THAN})
    private Double price;
}
```

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
    { "id": 1, "name": "Product 1", "price": 99.99 },
    { "id": 2, "name": "Product 2", "price": 149.99 }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": false, "unsorted": true },
    "offset": 0,
    "paged": true
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
@AutoApi(path = "/users")
@Data // Lombok
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
  "timestamp": "2025-12-21T19:30:00Z",
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
  ],
  "path": "/api/users"
}
```

---

## 🎨 Personnalisation

SpringFlow génère automatiquement repositories, services et controllers, mais vous pouvez fournir vos propres implémentations personnalisées pour n'importe quelle couche. **SpringFlow détectera automatiquement vos composants custom et sautera la génération pour ces couches.**

### :material-cog: Convention de Nommage pour Détection Automatique

Pour que SpringFlow détecte vos composants personnalisés, **respectez strictement cette convention**:

| Composant | Convention | Exemple |
|-----------|------------|---------|
| Repository | `{EntityName}Repository` | `OrderRepository` pour l'entité `Order` |
| Service | `{EntityName}Service` | `InvoiceService` pour l'entité `Invoice` |
| Controller | `{EntityName}Controller` | `ShipmentController` pour l'entité `Shipment` |

:material-alert-circle: **Important**: Si le nom ne correspond pas exactement, SpringFlow générera un bean supplémentaire, ce qui causera des conflits!

### :material-strategy: Scénarios de Personnalisation

#### 1. Repository Personnalisé Uniquement

**Cas d'usage**: Requêtes complexes, méthodes JPA spécifiques, queries JPQL/native

SpringFlow génère le service et le controller, mais utilise votre repository custom.

```java
@Entity
@AutoApi(path = "/orders", description = "Order management")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    private String status;

    // getters/setters
}
```

```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>,
                                          JpaSpecificationExecutor<Order> {
    // Méthodes de requête personnalisées
    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByStatus(String status);

    List<Order> findByTotalAmountGreaterThanEqual(BigDecimal minAmount);

    // Query JPQL personnalisée
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal calculateTotalRevenueByStatus(@Param("status") String status);
}
```

:material-check-circle: **Résultat**:
- :material-close: `orderRepository` → Votre implémentation custom (détectée, génération sautée)
- :material-check: `orderService` → Généré automatiquement par SpringFlow
- :material-check: `orderController` → Généré automatiquement par SpringFlow

#### 2. Service Personnalisé avec Logique Métier

**Cas d'usage**: Validation métier, workflows, règles du domaine, intégrations

Étendez `GenericCrudService` pour hériter des méthodes CRUD et ajouter votre logique.

```java
@Entity
@AutoApi(path = "/invoices", description = "Invoice management")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime issueDate;

    private LocalDateTime dueDate;
    private String status;

    // getters/setters
}
```

```java
@Service
public class InvoiceService extends GenericCrudService<Invoice, Long> {

    public InvoiceService(JpaRepository<Invoice, Long> repository) {
        super(repository, Invoice.class);
    }

    // Hook: Validation avant création
    @Override
    protected void beforeCreate(Invoice invoice) {
        // Validation: montant positif
        if (invoice.getAmount() == null || invoice.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invoice amount must be positive");
        }

        // Auto-génération du numéro de facture
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
        }

        // Date d'émission par défaut
        if (invoice.getIssueDate() == null) {
            invoice.setIssueDate(LocalDateTime.now());
        }

        // Date d'échéance (30 jours)
        if (invoice.getDueDate() == null) {
            invoice.setDueDate(invoice.getIssueDate().plusDays(30));
        }

        // Statut par défaut
        if (invoice.getStatus() == null) {
            invoice.setStatus("DRAFT");
        }
    }

    // Hook: Validation avant mise à jour
    @Override
    protected void beforeUpdate(Invoice existing, Invoice updated) {
        // Empêcher modification des factures émises
        if ("ISSUED".equals(existing.getStatus()) || "PAID".equals(existing.getStatus())) {
            if (!existing.getInvoiceNumber().equals(updated.getInvoiceNumber())) {
                throw new IllegalStateException("Cannot change invoice number for issued invoices");
            }
            if (existing.getAmount().compareTo(updated.getAmount()) != 0) {
                throw new IllegalStateException("Cannot change amount for issued invoices");
            }
        }
    }

    // Méthode métier personnalisée
    public BigDecimal getTotalRevenue() {
        return repository.findAll().stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Invoice> findOverdueInvoices() {
        LocalDateTime now = LocalDateTime.now();
        return repository.findAll().stream()
                .filter(invoice -> !"PAID".equals(invoice.getStatus()) &&
                                   invoice.getDueDate() != null &&
                                   invoice.getDueDate().isBefore(now))
                .toList();
    }

    public Invoice issueInvoice(Long id) {
        Invoice invoice = findById(id);
        if (!"DRAFT".equals(invoice.getStatus())) {
            throw new IllegalStateException("Only draft invoices can be issued");
        }
        invoice.setStatus("ISSUED");
        invoice.setIssueDate(LocalDateTime.now());
        return repository.save(invoice);
    }
}
```

:material-check-circle: **Résultat**:
- :material-check: `invoiceRepository` → Généré automatiquement par SpringFlow
- :material-close: `invoiceService` → Votre implémentation custom (détectée, génération sautée)
- :material-check: `invoiceController` → Généré automatiquement par SpringFlow

**Hooks disponibles**:
- `beforeCreate(T entity)` - Avant création
- `afterCreate(T entity)` - Après création
- `beforeUpdate(T existing, T updated)` - Avant mise à jour
- `afterUpdate(T entity)` - Après mise à jour
- `beforeDelete(ID id)` - Avant suppression
- `afterDelete(ID id)` - Après suppression

#### 3. Controller Personnalisé avec Endpoints Additionnels

**Cas d'usage**: Endpoints non-CRUD, opérations métier spécifiques, workflows complexes

Étendez `GenericCrudController` pour hériter des endpoints CRUD et ajouter les vôtres.

```java
@Entity
@AutoApi(path = "/shipments", description = "Shipment tracking")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String trackingNumber;

    @Column(nullable = false)
    private String status;

    private LocalDateTime shippedDate;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private String carrier;

    // getters/setters
}
```

```java
@RestController
@RequestMapping("/api/shipments")
public class ShipmentController extends GenericCrudController<Shipment, Long> {

    public ShipmentController(
            @Qualifier("shipmentService") GenericCrudService<Shipment, Long> service,
            @Qualifier("shipmentDtoMapper") DtoMapper<Shipment, Long> dtoMapper,
            FilterResolver filterResolver,
            @Qualifier("shipmentMetadata") EntityMetadata metadata
    ) {
        super(service, dtoMapper, filterResolver, metadata, Shipment.class);
    }

    @Override
    protected Long getEntityId(Shipment entity) {
        return entity.getId();
    }

    // Endpoint personnalisé: Mettre à jour le statut
    @PutMapping("/{id}/update-status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        Shipment shipment = service.findById(id);
        shipment.setStatus(status);

        // Logique métier selon le statut
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case "IN_TRANSIT" -> {
                if (shipment.getShippedDate() == null) {
                    shipment.setShippedDate(now);
                }
            }
            case "DELIVERED" -> shipment.setActualDeliveryDate(now);
        }

        Shipment updated = service.save(shipment);
        return ResponseEntity.ok(dtoMapper.toOutputDto(updated));
    }

    // Endpoint personnalisé: Expédier
    @PostMapping("/{id}/ship")
    public ResponseEntity<Map<String, Object>> ship(
            @PathVariable Long id,
            @RequestParam(required = false) String carrier
    ) {
        Shipment shipment = service.findById(id);

        if (!"PENDING".equals(shipment.getStatus())) {
            throw new IllegalStateException("Only pending shipments can be shipped");
        }

        shipment.setStatus("IN_TRANSIT");
        shipment.setShippedDate(LocalDateTime.now());
        if (carrier != null) {
            shipment.setCarrier(carrier);
        }

        Shipment updated = service.save(shipment);
        return ResponseEntity.ok(dtoMapper.toOutputDto(updated));
    }

    // Endpoint personnalisé: Tracking
    @GetMapping("/{id}/tracking")
    public ResponseEntity<TrackingInfo> getTracking(@PathVariable Long id) {
        Shipment shipment = service.findById(id);
        TrackingInfo trackingInfo = new TrackingInfo(
                shipment.getTrackingNumber(),
                shipment.getStatus(),
                shipment.getCarrier(),
                shipment.getShippedDate(),
                shipment.getEstimatedDeliveryDate(),
                shipment.getActualDeliveryDate()
        );
        return ResponseEntity.ok(trackingInfo);
    }

    public record TrackingInfo(
            String trackingNumber,
            String status,
            String carrier,
            LocalDateTime shippedDate,
            LocalDateTime estimatedDeliveryDate,
            LocalDateTime actualDeliveryDate
    ) {}
}
```

:material-check-circle: **Résultat**:
- :material-check: `shipmentRepository` → Généré automatiquement par SpringFlow
- :material-check: `shipmentService` → Généré automatiquement par SpringFlow
- :material-close: `shipmentController` → Votre implémentation custom (détectée, génération sautée)

**Endpoints disponibles**:
- :material-check: `GET /api/shipments` - Liste (hérité)
- :material-check: `GET /api/shipments/{id}` - Détail (hérité)
- :material-check: `POST /api/shipments` - Création (hérité)
- :material-check: `PUT /api/shipments/{id}` - Mise à jour (hérité)
- :material-check: `DELETE /api/shipments/{id}` - Suppression (hérité)
- :material-plus-circle: `PUT /api/shipments/{id}/update-status` - Custom
- :material-plus-circle: `POST /api/shipments/{id}/ship` - Custom
- :material-plus-circle: `GET /api/shipments/{id}/tracking` - Custom

#### 4. Implémentation Complètement Personnalisée

**Cas d'usage**: Contrôle total, logique très spécifique, ne pas utiliser les patterns SpringFlow

Pour un contrôle complet, implémentez les trois couches sans étendre les classes de base.

```java
@Entity
@AutoApi(path = "/customers", description = "Customer management")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String customerCode;

    @Column(nullable = false)
    private String companyName;

    @Column(unique = true)
    private String email;

    private String status; // ACTIVE, INACTIVE, SUSPENDED

    // getters/setters
}
```

```java
// Repository custom
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerCode(String customerCode);
    Optional<Customer> findByEmail(String email);
    List<Customer> findByStatus(String status);
    boolean existsByCustomerCode(String customerCode);
    boolean existsByEmail(String email);
    long countByStatus(String status);
}

// Service custom (ne pas étendre GenericCrudService)
@Service
@Transactional
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Customer.class, id));
    }

    public Customer create(Customer customer) {
        // Validation
        if (customer.getCompanyName() == null || customer.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Company name is required");
        }

        // Auto-génération du code client
        if (customer.getCustomerCode() == null || customer.getCustomerCode().isBlank()) {
            customer.setCustomerCode(generateCustomerCode());
        }

        // Vérifier unicité
        if (repository.existsByCustomerCode(customer.getCustomerCode())) {
            throw new IllegalArgumentException("Customer code already exists");
        }

        // Statut par défaut
        if (customer.getStatus() == null) {
            customer.setStatus("ACTIVE");
        }

        return repository.save(customer);
    }

    public Customer update(Long id, Customer customer) {
        Customer existing = findById(id);

        // Ne pas permettre de changer le code client
        if (customer.getCustomerCode() != null &&
            !customer.getCustomerCode().equals(existing.getCustomerCode())) {
            throw new IllegalArgumentException("Cannot change customer code");
        }

        // Mettre à jour les champs
        if (customer.getCompanyName() != null) {
            existing.setCompanyName(customer.getCompanyName());
        }
        if (customer.getEmail() != null) {
            existing.setEmail(customer.getEmail());
        }
        if (customer.getStatus() != null) {
            existing.setStatus(customer.getStatus());
        }

        return repository.save(existing);
    }

    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(Customer.class, id);
        }
        repository.deleteById(id);
    }

    public Customer activate(Long id) {
        Customer customer = findById(id);
        customer.setStatus("ACTIVE");
        return repository.save(customer);
    }

    private String generateCustomerCode() {
        return "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public CustomerStats getStatistics() {
        long total = repository.count();
        long active = repository.countByStatus("ACTIVE");
        long inactive = repository.countByStatus("INACTIVE");
        long suspended = repository.countByStatus("SUSPENDED");
        return new CustomerStats(total, active, inactive, suspended);
    }

    public record CustomerStats(long total, long active, long inactive, long suspended) {}
}

// Controller custom (ne pas étendre GenericCrudController)
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Customer> create(@Valid @RequestBody Customer customer) {
        if (customer.getId() != null) {
            throw new IllegalArgumentException("A new customer cannot have an ID");
        }
        Customer created = service.create(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(
            @PathVariable Long id,
            @Valid @RequestBody Customer customer
    ) {
        Customer updated = service.update(id, customer);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Customer> activate(@PathVariable Long id) {
        return ResponseEntity.ok(service.activate(id));
    }

    @GetMapping("/statistics")
    public ResponseEntity<CustomerService.CustomerStats> getStatistics() {
        return ResponseEntity.ok(service.getStatistics());
    }
}
```

:material-check-circle: **Résultat**:
- :material-close: `customerRepository` → Votre implémentation custom (détectée, génération sautée)
- :material-close: `customerService` → Votre implémentation custom (détectée, génération sautée)
- :material-close: `customerController` → Votre implémentation custom (détectée, génération sautée)

### :material-lightbulb: Tableau des Cas d'Usage

| Besoin | Couche(s) Custom | Raison | Exemple |
|--------|------------------|--------|---------|
| Requêtes SQL complexes | Repository | Queries JPQL, native SQL, specifications | `OrderRepository` avec `calculateTotalRevenueByStatus()` |
| Validation métier avancée | Service | Règles domaine, workflows, états | `InvoiceService` avec validation montant et statut |
| Endpoints spécifiques | Controller | Opérations non-CRUD, actions métier | `ShipmentController` avec `/ship`, `/deliver` |
| Auto-génération de données | Service | Numéros uniques, codes, timestamps | `CustomerService` avec `generateCustomerCode()` |
| Contrôle total | Tous | Domaines complexes, patterns spécifiques | `Customer` avec logique complètement custom |
| Intégrations externes | Service | APIs tierces, messagerie, cache | Service avec appels REST, Kafka, Redis |
| Sécurité fine | Controller ou Service | Autorisation par méthode, ACL | Controller avec `@PreAuthorize` |
| Audit et logging | Service | Traçabilité, événements métier | Service avec hooks `after*` |

### :material-check-decagram: Best Practices

1. **:material-arrow-up: Étendre les classes de base** quand possible
   - Héritez de `GenericCrudService` ou `GenericCrudController`
   - Vous bénéficiez des fonctionnalités standard + vos ajouts
   - Moins de code à maintenir

2. **:material-hook: Utiliser les hooks** pour la logique transversale
   - `beforeCreate` / `afterCreate` pour validation et logging
   - `beforeUpdate` / `afterUpdate` pour cohérence des données
   - `beforeDelete` / `afterDelete` pour cleanup et vérifications

3. **:material-tag: Respecter la convention de nommage**
   - `{EntityName}Repository`, `{EntityName}Service`, `{EntityName}Controller`
   - Exactement comme le nom de l'entité (case-sensitive)
   - Sinon SpringFlow générera un bean en doublon!

4. **:material-puzzle: Mixer custom et généré** selon vos besoins
   - Repository custom pour queries → Service et Controller générés
   - Service custom pour métier → Repository et Controller générés
   - Pas besoin de tout faire custom!

5. **:material-code-braces: @Qualifier et DtoMapperFactory**
   - Si vous étendez `GenericCrudController`, utilisez `@Qualifier` pour le service et injectez `DtoMapperFactory`:
   ```java
   public ShipmentController(
       @Qualifier("shipmentService") GenericCrudService<Shipment, Long> service,
       DtoMapperFactory dtoMapperFactory,
       FilterResolver filterResolver
   ) {
       super(service,
             dtoMapperFactory.getMapper(Shipment.class, new MetadataResolver().resolve(Shipment.class)),
             filterResolver,
             new MetadataResolver().resolve(Shipment.class),
             Shipment.class);
   }
   ```

   - Si vous étendez `GenericCrudService`, utilisez `@Qualifier` pour le repository:
   ```java
   public InvoiceService(@Qualifier("invoiceRepository") JpaRepository<Invoice, Long> repository) {
       super(repository, Invoice.class);
   }
   ```

### :material-alert-decagram: Troubleshooting

**Problème**: SpringFlow génère encore un bean alors que j'ai un composant custom

:material-arrow-right: **Solution**: Vérifiez la convention de nommage
- Le bean doit s'appeler exactement `{entityName}Repository`, `{entityName}Service`, ou `{entityName}Controller`
- Respectez la casse: `ProductService` pour `Product`, pas `productservice` ou `ProductSvc`

**Problème**: `No qualifying bean of type 'GenericCrudService<MyEntity, Long>'`

:material-arrow-right: **Solution**: Ajoutez `@Qualifier` au constructeur du controller custom
```java
public MyController(
    @Qualifier("myEntityService") GenericCrudService<MyEntity, Long> service
) { ... }
```

**Problème**: `log has private access in GenericCrudService`

:material-arrow-right: **Solution**: Ajoutez votre propre logger dans la classe custom
```java
@Service
public class MyService extends GenericCrudService<MyEntity, Long> {
    private static final Logger log = LoggerFactory.getLogger(MyService.class);
    // ...
}
```

**Problème**: Mon repository custom n'a pas les méthodes de filtrage

:material-arrow-right: **Solution**: Étendez aussi `JpaSpecificationExecutor`
```java
public interface MyRepository extends JpaRepository<MyEntity, Long>,
                                       JpaSpecificationExecutor<MyEntity> {
    // ...
}
```

### Ajouter des endpoints personnalisés

Vous pouvez créer votre propre controller qui étend `GenericCrudController`:

```java
@RestController
@RequestMapping("/api/users")
public class UserController extends GenericCrudController<User, Long> {

    @Autowired
    private UserService userService;

    public UserController(UserService service, DtoMapper<User, Long> dtoMapper) {
        super(service, dtoMapper, User.class);
    }

    // Les endpoints CRUD standards sont hérités automatiquement

    // Ajoutez vos endpoints personnalisés
    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> findActiveUsers() {
        List<User> activeUsers = userService.findByActive(true);
        return ResponseEntity.ok(
            dtoMapper.toOutputDtoList(activeUsers)
        );
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateUser(@PathVariable Long id) {
        User user = userService.activate(id);
        return ResponseEntity.ok(dtoMapper.toOutputDto(user));
    }
}
```

### Service personnalisé avec hooks

```java
@Service
public class UserService extends GenericCrudService<User, Long> {

    // Méthodes CRUD héritées automatiquement

    // Hooks de lifecycle
    @Override
    protected void beforeCreate(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
        // Encoder le password, envoyer email de bienvenue, etc.
    }

    @Override
    protected void afterUpdate(User user) {
        // Logger l'action, invalider cache, etc.
    }

    @Override
    protected void beforeDelete(Long id) {
        // Vérifications avant suppression
    }

    // Logique métier personnalisée
    public User activate(Long id) {
        User user = findById(id);
        user.setActive(true);
        return save(user);
    }
}
```

---

## 🔷 Support Kotlin

SpringFlow fonctionne parfaitement avec Kotlin et les data classes.

### Exemple Kotlin

```kotlin
@Entity
@Table(name = "products")
@AutoApi(
    path = "/products",
    description = "Product management API",
    tags = ["Products"]
)
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank
    @field:Size(min = 3, max = 100)
    val name: String,

    @field:NotNull
    @field:Min(0)
    val price: Double,

    val description: String? = null,

    @field:ReadOnly
    val createdAt: LocalDateTime? = null
)
```

### Application Kotlin

```kotlin
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
```

**Note**: Aucune annotation `@EnableSpringFlow` requise! L'auto-configuration fonctionne automatiquement.

### Nullable types

```kotlin
data class User(
    val id: Long? = null,
    val name: String,           // Non-null (requis)
    val email: String?,         // Nullable (optionnel)
    val age: Int? = null        // Nullable avec default
)
```

---

## 📚 Exemples

### Exemple complet: E-Commerce

```java
// Product.java
@Entity
@Table(name = "products")
@Data  // Lombok
@AutoApi(path = "/products", description = "Product management")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    @Min(0)
    private Double price;

    @Min(0)
    private Integer stock = 0;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ReadOnly
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ReadOnly
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// Category.java
@Entity
@Table(name = "categories")
@Data
@AutoApi(path = "/categories", description = "Category management")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 50)
    private String name;

    @Size(max = 200)
    private String description;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("children")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("parent")
    private List<Category> children = new ArrayList<>();
}

// User.java
@Entity
@Table(name = "users")
@Data
@AutoApi(path = "/users", description = "User management")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    @Hidden  // ❌ Jamais exposé dans l'API
    @NotBlank
    @Size(min = 8)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @ReadOnly
    private LocalDateTime createdAt;

    public enum UserRole {
        USER, ADMIN, MODERATOR
    }
}
```

### Utilisation de l'API

```bash
# Lister les produits avec pagination
GET /api/products?page=0&size=20&sort=price,desc

# Créer un produit
POST /api/products
Content-Type: application/json
{
  "name": "iPhone 15 Pro",
  "description": "Latest Apple smartphone",
  "price": 999.99,
  "stock": 50
}

# Mettre à jour un produit
PUT /api/products/1
{
  "name": "iPhone 15 Pro Max",
  "price": 1199.99,
  "stock": 30
}

# Supprimer un produit
DELETE /api/products/1

# Créer une catégorie
POST /api/categories
{
  "name": "Electronics",
  "description": "Electronic devices and gadgets"
}

# Créer un utilisateur (le password ne sera jamais retourné)
POST /api/users
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "role": "USER"
}

# Le password n'apparaît PAS dans la réponse
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER",
  "createdAt": "2025-12-21T10:30:00Z"
  # ❌ password absent (grâce à @Hidden)
}
```

---

## 🛠️ Troubleshooting

### Problème : Les endpoints ne sont pas générés

**Solution** :
1. Vérifiez que vos entités ont `@Entity` ET `@AutoApi`
2. Vérifiez que `springflow.enabled=true` (défaut)
3. Activez les logs debug :
```yaml
logging:
  level:
    io.springflow: DEBUG
```

Vous devriez voir dans les logs:
```
SpringFlow auto-configuration activated
Scanned 3 entities with @AutoApi
Registered controller for Product
Registering 3 SpringFlow controllers with Spring MVC at base path: /api
```

### Problème : Erreur 404 sur les endpoints

**Solution** :
- Vérifiez le base-path configuré (défaut: `/api`)
- Vérifiez le path dans `@AutoApi`
- URL complète : `http://localhost:8080/{base-path}/{entity-path}`
- Exemple: `http://localhost:8080/api/products`

### Problème : Validation ne fonctionne pas

**Solution** :
- Ajoutez `spring-boot-starter-validation` dans vos dépendances
- Vérifiez que les annotations JSR-380 sont sur les **champs** (pas les getters)
- Utilisez `@field:` prefix en Kotlin

### Problème : Erreur avec Lombok

**Solution** :
- Utilisez Lombok 1.18.38+ pour Java 25
- Utilisez Lombok 1.18.30+ pour Java 17
- Ajoutez Lombok avant MapStruct dans annotation processor path

### Problème : data.sql ne s'exécute pas

**Solution** :
```yaml
spring:
  jpa:
    defer-datasource-initialization: true
```

Cela garantit que Hibernate crée les tables AVANT que data.sql s'exécute.

---

## 🤝 Contribuer

Nous accueillons les contributions ! Consultez [CONTRIBUTING.md](CONTRIBUTING.md) pour les guidelines.

### Développement local

```bash
# Cloner le repository
git clone https://github.com/tky0065/springflow.git
cd springflow

# Build
./mvnw clean install

# Run tests
./mvnw test

# Run demo
cd springflow-demo
../mvnw spring-boot:run
```

### Lancer la demo

```bash
cd springflow-demo
../mvnw spring-boot:run
```

L'application démarre sur `http://localhost:8080`

**Endpoints disponibles:**
- API: `http://localhost:8080/api/products`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:springflowdb`)

---

## 📝 Changelog

Voir [CHANGELOG.md](CHANGELOG.md) pour l'historique des versions.

**Version 0.1.1-SNAPSHOT (Phase 1 MVP)**:
- ✅ CRUD complet (GET, POST, PUT, DELETE)
- ✅ Pagination & Sorting
- ✅ Validation JSR-380
- ✅ DTO auto avec @Hidden/@ReadOnly
- ✅ Swagger/OpenAPI
- ✅ Support Java & Kotlin
- ✅ Auto-configuration Spring Boot

---

## 📄 License

SpringFlow est sous licence [Apache License 2.0](LICENSE).

---

## 🎯 Roadmap

- ✅ **Phase 1 (v0.1.1 - MVP)** : CRUD, pagination, validation, DTOs, Swagger
- ✅ **Phase 2 (v0.2.0)** : Filtres dynamiques, Security, Soft Delete, Audit
- 🔄 **Phase 3 (v0.3.0)** : GraphQL (done), Admin UI, CLI, Multi-DB, Monitoring (future)

Voir [roadmap.md](roadmap.md) pour plus de détails.

---

## ⭐ Support

Si SpringFlow vous aide, n'oubliez pas de mettre une ⭐ sur GitHub !

- 🐛 **Issues** : [GitHub Issues](https://github.com/tky0065/springflow/issues)
- 💬 **Discussions** : [GitHub Discussions](https://github.com/tky0065/springflow/discussions)

---

**Fait avec ❤️ par l'équipe SpringFlow**
