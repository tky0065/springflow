# Audit Trail

Traçabilité automatique des créations et modifications.

## Activation

```java
@Entity
@AutoApi(path = "documents")
@Auditable
public class Document {
    @Id
    private Long id;
    
    private String title;
    
    // Ajouté automatiquement
    // @CreatedDate
    // private LocalDateTime createdAt;
    //
    // @LastModifiedDate
    // private LocalDateTime updatedAt;
    //
    // @CreatedBy
    // private String createdBy;
    //
    // @LastModifiedBy
    // private String updatedBy;
}
```

## Champs Automatiques

| Champ | Type | Description |
|-------|------|-------------|
| `createdAt` | LocalDateTime | Date de création |
| `updatedAt` | LocalDateTime | Date de dernière modification |
| `createdBy` | String | Utilisateur créateur |
| `updatedBy` | String | Dernier modificateur |

## Configuration

### Spring Data JPA Auditing

SpringFlow active automatiquement :

```java
@EnableJpaAuditing
@Configuration
public class SpringFlowAuditConfiguration {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SecurityAuditorAware();
    }
}
```

### Récupération de l'Utilisateur

Par défaut, récupère l'utilisateur depuis `SecurityContext` :

```java
SecurityContextHolder.getContext()
    .getAuthentication()
    .getName()
```

## Exemple de Response

```json
{
  "id": 1,
  "title": "Document Title",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T15:45:00",
  "createdBy": "john.doe",
  "updatedBy": "jane.smith"
}
```

## Voir Aussi

- [Annotations](annotations.md#auditable)
- [Security](security.md)
