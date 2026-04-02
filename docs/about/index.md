# About SpringFlow

About the SpringFlow project.

## :material-book-open-variant: In this section

<div class="grid cards" markdown>

-   :material-road-variant: **[Roadmap](roadmap.md)**

    ---

    Project vision and upcoming features

-   :material-file-document-edit: **[Changelog](changelog.md)**

    ---

    Version history and release notes

-   :material-scale-balance: **[License](license.md)**

    ---

    Apache License 2.0

</div>

## :material-target: Vision

SpringFlow aims to **eliminate 70–90% of boilerplate** in Spring Boot applications by automatically generating a complete REST infrastructure from JPA entities.

## :material-star: Philosophy

### Zero Configuration

```java
@Entity
@AutoApi(path = "products")
public class Product { ... }
```

A single annotation is all you need to generate a fully functional API.

### Production Ready

- Transaction management
- Exception handling
- Input validation
- Security integration
- Performance optimizations

### Extensible & Flexible

- Override any generated component
- Add custom endpoints
- Integrate with existing code

## :material-chart-bar: Stats

- **Current version**: 0.5.1
- **Tests**: 136+ unit and integration tests
- **Coverage**: >80%
- **Java**: 17+
- **Spring Boot**: 3.2.1+

## :material-map: What's Available

### :material-check-circle:{ .success } Core features (stable)
- Automatic CRUD endpoints
- Pagination & sorting
- DTO mapping
- JSR-380 validation
- OpenAPI/Swagger

### :material-check-circle:{ .success } Advanced features (stable)
- Dynamic filtering
- Security integration
- Soft delete
- Advanced DTO mapping

### :material-check-circle:{ .success } Extended features (stable)
- GraphQL support

### :material-progress-clock: Coming soon
- Audit trail field population
- Admin UI
- CLI tools
- Multi-database support
- Monitoring & metrics

See the [full roadmap](roadmap.md) for details.

## :material-file-document: License

SpringFlow is released under the [Apache License 2.0](license.md).

## :material-handshake: Community

- **GitHub**: [tky0065/springflow](https://github.com/tky0065/springflow)
- **Issues**: [Report a bug](https://github.com/tky0065/springflow/issues)
- **Discussions**: [Forum](https://github.com/tky0065/springflow/discussions)
- **Maven Central**: [Artifacts](https://central.sonatype.com/artifact/io.github.tky0065/springflow-starter)

## :material-chat: Contact

Questions? Suggestions?

- Open an [issue](https://github.com/tky0065/springflow/issues)
- Start a [discussion](https://github.com/tky0065/springflow/discussions)
- Browse the [documentation](../index.md)

---

Thank you for using SpringFlow!
