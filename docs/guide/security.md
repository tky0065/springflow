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

### Fine-grained Security with @SecuredApi

For more control over security at the method level, use the `@SecuredApi` annotation.

```java
@Entity
@AutoApi(path = "products")
@SecuredApi(
    findAll = "hasRole('USER')",
    create = "hasRole('ADMIN')",
    update = "hasRole('ADMIN')",
    delete = "hasRole('ADMIN')"
)
public class Product { ... }
```

| Paramètre | Description |
|-----------|-------------|
| `findAll` | Expression pour la liste |
| `findById`| Expression pour le détail |
| `create`  | Expression pour la création |
| `update`  | Expression pour la mise à jour |
| `patch`   | Expression pour PATCH |
| `delete`  | Expression pour suppression |
| `restore` | Expression pour restauration |
| `hardDelete`| Expression pour suppression physique |

## Coming Soon

- JWT support
- OAuth2 integration
- Custom security expressions per endpoint via custom controllers

## See Also

- [Annotations](annotations.md#security)
- [Configuration](configuration.md)
