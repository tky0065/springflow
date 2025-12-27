# Validation

Validation automatique avec JSR-380 (Bean Validation).

## Support

SpringFlow supporte toutes les annotations JSR-380 :

- `@NotNull`
- `@NotBlank`
- `@NotEmpty`
- `@Size`
- `@Min` / `@Max`
- `@Email`
- `@Pattern`
- `@Valid` (nested validation)

## Exemple

```java
@Entity
@AutoApi(path = "users")
public class User {
    @Id
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50)
    private String name;
    
    @NotBlank
    @Email(message = "Invalid email format")
    private String email;
    
    @Min(18)
    @Max(120)
    private Integer age;
    
    @Pattern(regexp = "^[A-Z]{2}$")
    private String countryCode;
}
```

## Error Format

En cas d'erreur de validation :

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 400,
  "errors": [
    {
      "field": "name",
      "message": "Name is required",
      "rejectedValue": null,
      "code": "NotBlank"
    },
    {
      "field": "email",
      "message": "Invalid email format",
      "rejectedValue": "invalid-email",
      "code": "Email"
    }
  ]
}
```

## Validation Groups

!!! info "Nouveau depuis v0.4.0"
    SpringFlow supporte maintenant les **Validation Groups** JSR-380 pour différencier les règles de validation selon le contexte (création vs mise à jour).

### Contextes de Validation

SpringFlow applique automatiquement le groupe de validation approprié :

| Endpoint | Groupe appliqué | Cas d'usage |
|----------|----------------|-------------|
| `POST /api/entities` | `ValidationGroups.Create` | Création d'entité |
| `PUT /api/entities/{id}` | `ValidationGroups.Update` | Mise à jour complète |
| `PATCH /api/entities/{id}` | `ValidationGroups.Update` | Mise à jour partielle |

### Exemple Complet

```java
import io.springflow.core.validation.ValidationGroups.Create;
import io.springflow.core.validation.ValidationGroups.Update;

@Entity
@AutoApi(path = "products")
public class Product {
    @Id
    private Long id;

    // Validé à la création ET à la mise à jour
    @NotBlank(groups = {Create.class, Update.class})
    @Size(min = 3, max = 100, groups = {Create.class, Update.class})
    private String name;

    // Requis UNIQUEMENT à la création
    @NotNull(groups = Create.class, message = "Initial category required on creation")
    private String initialCategory;

    // Validé UNIQUEMENT en mise à jour
    @Email(groups = Update.class, message = "Supplier email must be valid")
    private String supplierEmail;

    // Validé dans les deux contextes avec contraintes différentes
    @NotNull(groups = {Create.class, Update.class})
    @Min(value = 0, groups = {Create.class, Update.class})
    @Max(value = 999999, groups = Create.class)  // Limite plus stricte à la création
    private Double price;
}
```

### Scénarios d'Usage

#### Création (POST)

```bash
POST /api/products
{
  "name": "iPhone 15",
  "initialCategory": "Electronics",  # ← Requis
  "price": 999.99
}
# ✓ Validation Create appliquée
# ✓ initialCategory requis
# ✓ supplierEmail non validé (Update seulement)
```

#### Mise à Jour (PUT/PATCH)

```bash
PATCH /api/products/1
{
  "name": "iPhone 15 Pro",
  "supplierEmail": "supplier@apple.com",  # ← Validé
  "price": 1099.99
}
# ✓ Validation Update appliquée
# ✓ initialCategory non requis
# ✓ supplierEmail validé (format email)
```

### Groupes par Défaut

Si aucun groupe n'est spécifié, la validation utilise le groupe `Default` :

```java
@NotBlank  // Équivalent à @NotBlank(groups = Default.class)
private String name;
```

!!! warning "Attention"
    Lorsque vous spécifiez des groupes explicites, le groupe `Default` n'est PAS inclus automatiquement. Pour valider dans tous les contextes, ajoutez explicitement `{Create.class, Update.class}`.

### Validation Manuelle

Pour valider manuellement avec des groupes spécifiques :

```java
@Autowired
private EntityValidator entityValidator;

public void validateProduct(Product product) {
    // Validation pour création
    entityValidator.validateForCreate(product);

    // Validation pour mise à jour
    entityValidator.validateForUpdate(product);

    // Validation avec groupes custom
    entityValidator.validate(product, CustomGroup.class);
}
```

### Messages d'Erreur

Les messages d'erreur indiquent le groupe qui a échoué :

```json
{
  "timestamp": "2025-12-27T10:30:00",
  "status": 400,
  "errors": [
    {
      "field": "initialCategory",
      "message": "Initial category required on creation",
      "rejectedValue": null,
      "code": "NotNull",
      "validationGroup": "Create"
    }
  ]
}
```

### Cas d'Usage Avancés

#### Validation Conditionnelle

```java
@Entity
public class Order {
    // Validation différente selon statut
    @NotNull(groups = Create.class)
    private String customerName;

    @NotNull(groups = Update.class)  // Requis seulement en update
    private String trackingNumber;

    @Email(groups = {Create.class, Update.class})
    private String email;
}
```

#### Hiérarchie de Groupes

```java
public interface ValidationGroups {
    interface Create {}
    interface Update {}

    // Groupes custom
    interface AdminUpdate extends Update {}
    interface QuickCreate extends Create {}
}

@Min(value = 100, groups = AdminUpdate.class)  // Limite plus élevée pour admins
private Double budget;
```

## Voir Aussi

- [Annotations](annotations.md)
- [PATCH Endpoint](../api/endpoints.md#patch-partial-update)
- [Best Practices](../advanced/best-practices.md)
