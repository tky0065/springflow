# Custom Endpoints

!!! info "Documentation déplacée"
    La documentation complète sur les composants personnalisés (repositories, services, et controllers) a été déplacée vers [Composants Personnalisés](custom-components.md).

    Cette page ne contient que des exemples rapides. Pour la documentation complète avec tous les scénarios, troubleshooting et best practices, consultez:

    **[→ Documentation Complète des Composants Personnalisés](custom-components.md)**

---

## Exemples Rapides

### Ajouter des Endpoints Custom au Controller

```java
@RestController
@RequestMapping("/api/products")
public class ProductController extends GenericCrudController<Product, Long> {

    public ProductController(
        @Qualifier("productService") GenericCrudService<Product, Long> service,
        DtoMapperFactory dtoMapperFactory,
        FilterResolver filterResolver
    ) {
        super(service,
              dtoMapperFactory.getMapper(Product.class, new MetadataResolver().resolve(Product.class)),
              filterResolver,
              new MetadataResolver().resolve(Product.class),
              Product.class);
    }

    @Override
    protected Long getEntityId(Product entity) {
        return entity.getId();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(
        @RequestParam String keyword
    ) {
        // Implémentation custom
        List<Product> products = service.findAll().stream()
            .filter(p -> p.getName().contains(keyword))
            .toList();
        return ResponseEntity.ok(dtoMapper.toOutputDtoList(products));
    }
}
```

### Ajouter de la Logique Métier dans le Service

```java
@Service
public class ProductService extends GenericCrudService<Product, Long> {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    public ProductService(@Qualifier("productRepository") JpaRepository<Product, Long> repository) {
        super(repository, Product.class);
    }

    @Override
    protected void beforeCreate(Product entity) {
        entity.setCreatedAt(LocalDateTime.now());
        // Validation custom
        if (entity.getPrice() != null && entity.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }

    @Override
    protected void afterCreate(Product entity) {
        log.info("Product created: {} (ID: {})", entity.getName(), entity.getId());
        // Envoyer notification, invalider cache, etc.
    }
}
```

---

## :material-information: Documentation Complète

Pour une documentation complète incluant:

- ✅ Convention de nommage pour la détection automatique
- ✅ 4 scénarios détaillés avec code complet
- ✅ Repository, Service, et Controller personnalisés
- ✅ Hooks de lifecycle disponibles
- ✅ Tableau des cas d'usage
- ✅ Best practices et patterns recommandés
- ✅ Troubleshooting avec solutions complètes
- ✅ Exemples réels de production

**Consultez: [Composants Personnalisés](custom-components.md)**

---

## Voir Aussi

- **[Composants Personnalisés](custom-components.md)** - Documentation complète
- [Architecture](architecture.md) - Architecture de SpringFlow
- [Best Practices](best-practices.md) - Recommandations
