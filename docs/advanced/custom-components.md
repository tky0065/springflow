# Composants Personnalisés

SpringFlow génère automatiquement les repositories, services et controllers, mais vous pouvez fournir vos propres implémentations personnalisées pour n'importe quelle couche. **SpringFlow détectera automatiquement vos composants custom et sautera la génération pour ces couches.**

## :material-cog: Convention de Nommage

Pour que SpringFlow détecte vos composants personnalisés, **respectez strictement cette convention**:

| Composant | Convention | Exemple |
|-----------|------------|---------|
| Repository | `{EntityName}Repository` | `OrderRepository` pour l'entité `Order` |
| Service | `{EntityName}Service` | `InvoiceService` pour l'entité `Invoice` |
| Controller | `{EntityName}Controller` | `ShipmentController` pour l'entité `Shipment` |

!!! warning "Important"
    Si le nom ne correspond pas exactement, SpringFlow générera un bean supplémentaire, ce qui causera des conflits!

## :material-strategy: Scénarios de Personnalisation

### 1. Repository Personnalisé Uniquement

**Cas d'usage**: Requêtes complexes, méthodes JPA spécifiques, queries JPQL/native

SpringFlow génère le service et le controller, mais utilise votre repository custom.

#### Entité

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

#### Repository Custom

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

    long countByStatus(String status);
}
```

#### Résultat

- :material-close-circle: `orderRepository` → Votre implémentation custom (détectée, génération sautée)
- :material-check-circle: `orderService` → Généré automatiquement par SpringFlow
- :material-check-circle: `orderController` → Généré automatiquement par SpringFlow

---

### 2. Service Personnalisé avec Logique Métier

**Cas d'usage**: Validation métier, workflows, règles du domaine, intégrations

Étendez `GenericCrudService` pour hériter des méthodes CRUD et ajouter votre logique.

#### Entité

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
    private String description;

    // getters/setters
}
```

#### Service Custom

```java
@Service
public class InvoiceService extends GenericCrudService<Invoice, Long> {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    public InvoiceService(@Qualifier("invoiceRepository") JpaRepository<Invoice, Long> repository) {
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

        validateStatusTransition(existing.getStatus(), updated.getStatus());
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

    public Invoice markAsPaid(Long id) {
        Invoice invoice = findById(id);
        if (!"ISSUED".equals(invoice.getStatus()) && !"OVERDUE".equals(invoice.getStatus())) {
            throw new IllegalStateException("Only issued or overdue invoices can be marked as paid");
        }
        invoice.setStatus("PAID");
        return repository.save(invoice);
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || currentStatus.equals(newStatus)) {
            return;
        }

        boolean validTransition = switch (currentStatus) {
            case "DRAFT" -> "ISSUED".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "ISSUED" -> "PAID".equals(newStatus) || "OVERDUE".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "OVERDUE" -> "PAID".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "PAID", "CANCELLED" -> false; // Terminal states
            default -> true;
        };

        if (!validTransition) {
            throw new IllegalStateException(
                String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }
    }
}
```

!!! tip "@Qualifier Required"
    Utilisez `@Qualifier("invoiceRepository")` dans le constructeur pour éviter l'ambiguïté avec les types génériques JpaRepository.

#### Résultat

- :material-check-circle: `invoiceRepository` → Généré automatiquement par SpringFlow
- :material-close-circle: `invoiceService` → Votre implémentation custom (détectée, génération sautée)
- :material-check-circle: `invoiceController` → Généré automatiquement par SpringFlow

#### Hooks Disponibles

- `beforeCreate(T entity)` - Avant création
- `afterCreate(T entity)` - Après création
- `beforeUpdate(T existing, T updated)` - Avant mise à jour
- `afterUpdate(T entity)` - Après mise à jour
- `beforeDelete(ID id)` - Avant suppression
- `afterDelete(ID id)` - Après suppression

---

### 3. Controller Personnalisé avec Endpoints Additionnels

**Cas d'usage**: Endpoints non-CRUD, opérations métier spécifiques, workflows complexes

Étendez `GenericCrudController` pour hériter des endpoints CRUD et ajouter les vôtres.

#### Entité

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
    private String notes;

    // getters/setters
}
```

#### Controller Custom

```java
@RestController
@RequestMapping("/api/shipments")
public class ShipmentController extends GenericCrudController<Shipment, Long> {

    private static final Logger log = LoggerFactory.getLogger(ShipmentController.class);

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
        log.debug("Updating status for shipment {} to {}", id, status);

        validateStatus(status);

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
            case "OUT_FOR_DELIVERY" -> {
                if (shipment.getEstimatedDeliveryDate() == null) {
                    shipment.setEstimatedDeliveryDate(now.plusDays(1));
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
        log.debug("Shipping shipment {} with carrier {}", id, carrier);

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

    // Endpoint personnalisé: Livrer
    @PostMapping("/{id}/deliver")
    public ResponseEntity<Map<String, Object>> deliver(@PathVariable Long id) {
        log.debug("Delivering shipment {}", id);

        Shipment shipment = service.findById(id);

        if ("PENDING".equals(shipment.getStatus())) {
            throw new IllegalStateException("Shipment must be shipped before being delivered");
        }

        if ("DELIVERED".equals(shipment.getStatus())) {
            throw new IllegalStateException("Shipment is already delivered");
        }

        shipment.setStatus("DELIVERED");
        shipment.setActualDeliveryDate(LocalDateTime.now());

        Shipment updated = service.save(shipment);
        return ResponseEntity.ok(dtoMapper.toOutputDto(updated));
    }

    // Endpoint personnalisé: Tracking
    @GetMapping("/{id}/tracking")
    public ResponseEntity<TrackingInfo> getTracking(@PathVariable Long id) {
        log.debug("Getting tracking info for shipment {}", id);

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

    private void validateStatus(String status) {
        String[] validStatuses = {"PENDING", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED", "RETURNED"};
        for (String validStatus : validStatuses) {
            if (validStatus.equals(status)) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + status +
                ". Valid values are: " + String.join(", ", validStatuses));
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

!!! important "Injection de Dépendances"
    - Utilisez `@Qualifier("shipmentService")` pour le service
    - Injectez `DtoMapperFactory` et créez le mapper dans le super()
    - Créez `MetadataResolver` localement (ce n'est pas un bean Spring)

#### Résultat

- :material-check-circle: `shipmentRepository` → Généré automatiquement par SpringFlow
- :material-check-circle: `shipmentService` → Généré automatiquement par SpringFlow
- :material-close-circle: `shipmentController` → Votre implémentation custom (détectée, génération sautée)

**Endpoints disponibles**:

- :material-check-circle: `GET /api/shipments` - Liste (hérité)
- :material-check-circle: `GET /api/shipments/{id}` - Détail (hérité)
- :material-check-circle: `POST /api/shipments` - Création (hérité)
- :material-check-circle: `PUT /api/shipments/{id}` - Mise à jour (hérité)
- :material-check-circle: `DELETE /api/shipments/{id}` - Suppression (hérité)
- :material-plus-circle: `PUT /api/shipments/{id}/update-status` - Custom
- :material-plus-circle: `POST /api/shipments/{id}/ship` - Custom
- :material-plus-circle: `POST /api/shipments/{id}/deliver` - Custom
- :material-plus-circle: `GET /api/shipments/{id}/tracking` - Custom

---

### 4. Implémentation Complètement Personnalisée

**Cas d'usage**: Contrôle total, logique très spécifique, ne pas utiliser les patterns SpringFlow

Pour un contrôle complet, implémentez les trois couches sans étendre les classes de base.

#### Entité

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

    private String phone;
    private String address;
    private String city;
    private String country;
    private String status; // ACTIVE, INACTIVE, SUSPENDED

    // getters/setters
}
```

#### Repository Custom

```java
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerCode(String customerCode);
    Optional<Customer> findByEmail(String email);
    List<Customer> findByStatus(String status);
    List<Customer> findByCompanyNameContainingIgnoreCase(String keyword);

    @Query("SELECT c FROM Customer c WHERE c.status = 'ACTIVE' ORDER BY c.companyName")
    List<Customer> findAllActiveCustomersSorted();

    boolean existsByCustomerCode(String customerCode);
    boolean existsByEmail(String email);
    long countByStatus(String status);
}
```

#### Service Custom

```java
@Service
@Transactional
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
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

    @Transactional(readOnly = true)
    public Customer findByCustomerCode(String customerCode) {
        return repository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new RuntimeException("Customer not found with code: " + customerCode));
    }

    public Customer create(Customer customer) {
        // Validation
        if (customer.getCompanyName() == null || customer.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Company name is required");
        }

        // Auto-génération du code client
        if (customer.getCustomerCode() == null || customer.getCustomerCode().isBlank()) {
            customer.setCustomerCode(generateCustomerCode());
        } else if (repository.existsByCustomerCode(customer.getCustomerCode())) {
            throw new IllegalArgumentException("Customer code already exists");
        }

        // Vérifier unicité email
        if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
            if (repository.existsByEmail(customer.getEmail())) {
                throw new IllegalArgumentException("Email already registered");
            }
        }

        // Statut par défaut
        if (customer.getStatus() == null) {
            customer.setStatus("ACTIVE");
        }

        Customer saved = repository.save(customer);
        log.info("Created customer with code: {} and id: {}", saved.getCustomerCode(), saved.getId());
        return saved;
    }

    public Customer update(Long id, Customer customer) {
        Customer existing = findById(id);

        // Ne pas permettre de changer le code client
        if (customer.getCustomerCode() != null &&
            !customer.getCustomerCode().equals(existing.getCustomerCode())) {
            throw new IllegalArgumentException("Cannot change customer code");
        }

        // Vérifier email uniqueness si changé
        if (customer.getEmail() != null &&
            !customer.getEmail().equals(existing.getEmail())) {
            if (repository.existsByEmail(customer.getEmail())) {
                throw new IllegalArgumentException("Email already registered");
            }
        }

        // Mettre à jour les champs
        if (customer.getCompanyName() != null) existing.setCompanyName(customer.getCompanyName());
        if (customer.getEmail() != null) existing.setEmail(customer.getEmail());
        if (customer.getPhone() != null) existing.setPhone(customer.getPhone());
        if (customer.getAddress() != null) existing.setAddress(customer.getAddress());
        if (customer.getCity() != null) existing.setCity(customer.getCity());
        if (customer.getCountry() != null) existing.setCountry(customer.getCountry());
        if (customer.getStatus() != null) existing.setStatus(customer.getStatus());

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

    public Customer deactivate(Long id) {
        Customer customer = findById(id);
        customer.setStatus("INACTIVE");
        return repository.save(customer);
    }

    public Customer suspend(Long id) {
        Customer customer = findById(id);
        customer.setStatus("SUSPENDED");
        return repository.save(customer);
    }

    @Transactional(readOnly = true)
    public CustomerStats getStatistics() {
        long total = repository.count();
        long active = repository.countByStatus("ACTIVE");
        long inactive = repository.countByStatus("INACTIVE");
        long suspended = repository.countByStatus("SUSPENDED");
        return new CustomerStats(total, active, inactive, suspended);
    }

    private String generateCustomerCode() {
        String code;
        do {
            code = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (repository.existsByCustomerCode(code));
        return code;
    }

    public record CustomerStats(long total, long active, long inactive, long suspended) {}
}
```

#### Controller Custom

```java
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> findAll() {
        log.debug("REST request to find all customers");
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> findById(@PathVariable Long id) {
        log.debug("REST request to find customer with id: {}", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/by-code/{customerCode}")
    public ResponseEntity<Customer> findByCode(@PathVariable String customerCode) {
        log.debug("REST request to find customer with code: {}", customerCode);
        return ResponseEntity.ok(service.findByCustomerCode(customerCode));
    }

    @PostMapping
    public ResponseEntity<Customer> create(@Valid @RequestBody Customer customer) {
        log.debug("REST request to create customer: {}", customer.getCompanyName());

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
        log.debug("REST request to update customer with id: {}", id);
        Customer updated = service.update(id, customer);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("REST request to delete customer with id: {}", id);
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Customer> activate(@PathVariable Long id) {
        return ResponseEntity.ok(service.activate(id));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Customer> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(service.deactivate(id));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<Customer> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(service.suspend(id));
    }

    @GetMapping("/statistics")
    public ResponseEntity<CustomerService.CustomerStats> getStatistics() {
        return ResponseEntity.ok(service.getStatistics());
    }
}
```

#### Résultat

- :material-close-circle: `customerRepository` → Votre implémentation custom
- :material-close-circle: `customerService` → Votre implémentation custom
- :material-close-circle: `customerController` → Votre implémentation custom

---

## :material-lightbulb: Tableau des Cas d'Usage

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

---

## :material-check-decagram: Best Practices

### 1. Étendre les classes de base

Héritez de `GenericCrudService` ou `GenericCrudController` quand possible:

- Vous bénéficiez des fonctionnalités standard + vos ajouts
- Moins de code à maintenir
- Compatible avec les futures évolutions de SpringFlow

### 2. Utiliser les hooks

Pour la logique transversale:

- `beforeCreate` / `afterCreate` pour validation et logging
- `beforeUpdate` / `afterUpdate` pour cohérence des données
- `beforeDelete` / `afterDelete` pour cleanup et vérifications

### 3. Respecter la convention de nommage

- `{EntityName}Repository`, `{EntityName}Service`, `{EntityName}Controller`
- Exactement comme le nom de l'entité (case-sensitive)
- Sinon SpringFlow générera un bean en doublon!

### 4. Mixer custom et généré

- Repository custom pour queries → Service et Controller générés
- Service custom pour métier → Repository et Controller générés
- Pas besoin de tout faire custom!

### 5. @Qualifier et DtoMapperFactory

**Pour GenericCrudController:**

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

**Pour GenericCrudService:**

```java
public InvoiceService(@Qualifier("invoiceRepository") JpaRepository<Invoice, Long> repository) {
    super(repository, Invoice.class);
}
```

### 6. Logger privé dans les classes custom

Les loggers dans `GenericCrudService` et `GenericCrudController` sont `private`, donc:

```java
@Service
public class MyService extends GenericCrudService<MyEntity, Long> {
    private static final Logger log = LoggerFactory.getLogger(MyService.class);
    // ...
}
```

---

## :material-alert-decagram: Troubleshooting

### SpringFlow génère encore un bean alors que j'ai un composant custom

**Cause**: Nom du bean incorrect

**Solution**: Vérifiez la convention de nommage

- Le bean doit s'appeler exactement `{entityName}Repository`, `{entityName}Service`, ou `{entityName}Controller`
- Respectez la casse: `ProductService` pour `Product`, pas `productservice` ou `ProductSvc`

### No qualifying bean of type 'GenericCrudService<MyEntity, Long>'

**Cause**: Ambiguïté avec les types génériques

**Solution**: Ajoutez `@Qualifier` au constructeur du controller custom

```java
public MyController(
    @Qualifier("myEntityService") GenericCrudService<MyEntity, Long> service
) { ... }
```

### No qualifying bean of type 'DtoMapper<MyEntity, Long>'

**Cause**: DtoMapper n'est pas enregistré comme bean, il est créé à la demande

**Solution**: Injectez `DtoMapperFactory` et créez le mapper:

```java
public MyController(
    DtoMapperFactory dtoMapperFactory,
    // ...
) {
    super(service,
          dtoMapperFactory.getMapper(MyEntity.class, new MetadataResolver().resolve(MyEntity.class)),
          // ...
    );
}
```

### No qualifying bean of type 'MetadataResolver'

**Cause**: `MetadataResolver` n'est pas un bean Spring

**Solution**: Créez une instance locale:

```java
new MetadataResolver().resolve(MyEntity.class)
```

### log has private access in GenericCrudService

**Cause**: Le logger dans la classe de base est `private`

**Solution**: Ajoutez votre propre logger:

```java
@Service
public class MyService extends GenericCrudService<MyEntity, Long> {
    private static final Logger log = LoggerFactory.getLogger(MyService.class);
    // ...
}
```

### Mon repository custom n'a pas les méthodes de filtrage

**Cause**: Le repository n'étend pas `JpaSpecificationExecutor`

**Solution**: Étendez aussi `JpaSpecificationExecutor`:

```java
public interface MyRepository extends JpaRepository<MyEntity, Long>,
                                       JpaSpecificationExecutor<MyEntity> {
    // ...
}
```

### Les hooks beforeUpdate/afterUpdate ne sont pas appelés

**Cause**: Vous utilisez directement `repository.save()` au lieu de `service.update()`

**Solution**: Utilisez les méthodes du service:

```java
// ❌ Mauvais
repository.save(entity);

// ✅ Bon
service.update(id, entity);
```

---

## :material-information: Voir Aussi

- [Architecture](architecture.md) - Comprendre l'architecture de SpringFlow
- [Best Practices](best-practices.md) - Recommandations générales
- [Annotations](../guide/annotations.md) - Liste complète des annotations
- [Filtering](../guide/filtering.md) - Filtrage dynamique avec JpaSpecificationExecutor
