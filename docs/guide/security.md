# Security Integration

Intégration avec Spring Security pour sécuriser vos endpoints.

## Configuration

### Niveau Entité

```java
@Entity
@AutoApi(
    path = "users",
    security = @Security(
        enabled = true,
        roles = {"ADMIN", "USER"}
    )
)
public class User { ... }
```

### Niveaux de Sécurité

| Niveau | Description |
|--------|-------------|
| `PUBLIC` | Accès public |
| `AUTHENTICATED` | Utilisateur authentifié |
| `ROLE_BASED` | Basé sur les rôles |

## Exemple avec Roles

```java
@AutoApi(
    path = "admin/users",
    security = @Security(
        enabled = true,
        roles = {"ADMIN"}
    )
)
```

Génère automatiquement :

```java
@PreAuthorize("hasAnyRole('ADMIN')")
public ResponseEntity<...> findAll(...) { ... }
```

## Coming Soon

Phase 2 apportera :
- JWT support
- OAuth2 integration
- Custom security expressions
- Endpoint-level security

## Voir Aussi

- [Annotations](annotations.md#security)
- [Configuration](configuration.md)
