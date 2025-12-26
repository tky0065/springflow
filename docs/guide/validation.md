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

## Voir Aussi

- [Annotations](annotations.md)
- [Best Practices](../advanced/best-practices.md)
