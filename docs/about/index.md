# About SpringFlow

Informations sur le projet SpringFlow.

## :material-book-open-variant: Dans cette section

<div class="grid cards" markdown>

-   :material-road-variant: **[Roadmap](roadmap.md)**

    ---

    Vision et feuille de route du projet

-   :material-file-document-edit: **[Changelog](changelog.md)**

    ---

    Historique des versions et changements

-   :material-scale-balance: **[License](license.md)**

    ---

    Licence Apache 2.0

</div>

## :material-target: Vision

SpringFlow vise à **réduire 70-90% du code boilerplate** dans les applications Spring Boot en générant automatiquement l'infrastructure REST à partir des entités JPA.

## :material-star: Philosophie

### Zero Configuration

```java
@Entity
@AutoApi(path = "products")
public class Product { ... }
```

Une seule annotation suffit pour générer une API complète.

### Production Ready

- Transaction management
- Exception handling
- Input validation
- Security integration
- Performance optimizations

### Extensible & Flexible

- Override du comportement généré
- Endpoints custom
- Intégration avec code existant

## :material-chart-bar: Statistiques

- **Version actuelle**: 0.2.0
- **Tests**: 136+ tests unitaires et d'intégration
- **Coverage**: >80%
- **Java**: 17+
- **Spring Boot**: 3.2.1+

## :material-map: Feuille de Route

### :material-check-circle:{ .success } Phase 1 - MVP (Complete)
- CRUD endpoints automatiques
- Pagination & sorting
- DTO mapping
- Validation JSR-380
- OpenAPI/Swagger

### :material-check-circle:{ .success } Phase 2 - Advanced (Complete)
- Dynamic filtering
- Security integration
- Soft delete
- Audit trail
- Advanced DTO mapping

### :material-construction:{ .warning } Phase 3 - Extended (En cours)
- GraphQL support
- Admin UI
- CLI tools
- Multi-database support
- Monitoring & metrics

Voir la [roadmap complète](roadmap.md) pour plus de détails.

## :material-file-document: License

SpringFlow est publié sous [Apache License 2.0](license.md).

## :material-handshake: Communauté

- **GitHub**: [tky0065/springflow](https://github.com/tky0065/springflow)
- **Issues**: [Signaler un bug](https://github.com/tky0065/springflow/issues)
- **Discussions**: [Forum](https://github.com/tky0065/springflow/discussions)
- **Maven Central**: [Artifacts](https://central.sonatype.com/artifact/io.github.tky0065/springflow-starter)

## :material-chat: Contact

Des questions ? Suggestions ?

- Ouvrez une [issue](https://github.com/tky0065/springflow/issues)
- Démarrez une [discussion](https://github.com/tky0065/springflow/discussions)
- Consultez la [documentation](../index.md)

---

Merci d'utiliser SpringFlow ! :material-heart:
