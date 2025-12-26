# Custom Endpoints

Ajouter vos propres endpoints personnalisés.

## Approche 1 : Étendre le Contrôleur Généré

```java
@RestController
@RequestMapping("/api/products")
public class ProductController extends GenericCrudController<Product, Long> {
    
    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(
        @RequestParam String keyword
    ) {
        // Custom logic
    }
    
    @GetMapping("/popular")
    public ResponseEntity<List<Product>> popular() {
        // Custom logic
    }
}
```

## Approche 2 : Contrôleur Complètement Custom

SpringFlow détecte automatiquement les contrôleurs custom et skip la génération.

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService service;  // Service généré disponible
    
    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        // Custom implementation
    }
}
```

## Override de Méthodes

```java
@RestController
@RequestMapping("/api/products")
public class ProductController extends GenericCrudController<Product, Long> {
    
    @Override
    public ResponseEntity<Product> create(Map<String, Object> inputDto) {
        // Custom validation or logic
        return super.create(inputDto);
    }
}
```

## Business Logic Hooks

Dans le service :

```java
@Service
public class ProductService extends GenericCrudService<Product, Long> {
    
    @Override
    protected void beforeCreate(Product entity) {
        // Custom logic before save
        entity.setCreatedAt(LocalDateTime.now());
    }
    
    @Override
    protected void afterCreate(Product entity) {
        // Custom logic after save
        sendNotification(entity);
    }
}
```

## Voir Aussi

- [Architecture](architecture.md)
- [Best Practices](best-practices.md)
