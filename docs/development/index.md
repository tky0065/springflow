# Development

Guide for contributing to and building SpringFlow.

## :material-book-open-variant: In this section

<div class="grid cards" markdown>

-   :material-hands-pray: **[Contributing](contributing.md)**

    ---

    Contribution guide for the project

-   :material-hammer-wrench: **[Building](building.md)**

    ---

    Compiling and building the project

-   :material-test-tube: **[Testing](testing.md)**

    ---

    Testing strategy and running the test suite

-   :material-rocket-launch: **[Release Process](release.md)**

    ---

    How versions are published

</div>

## :material-rocket-launch: Developer Quick Start

### 1. Clone the repository

```bash
git clone https://github.com/tky0065/springflow.git
cd springflow
```

### 2. Build

```bash
./mvnw clean install
```

### 3. Run tests

```bash
./mvnw test
```

### 4. Run the demo

```bash
cd springflow-demo
../mvnw spring-boot:run
```

## :material-office-building: Project Structure

```
springflow/
├── springflow-annotations/   # Annotations (zero dependencies)
├── springflow-core/          # Core implementation
├── springflow-graphql/       # Optional GraphQL module
├── springflow-starter/       # Spring Boot auto-configuration
└── springflow-demo/          # Demo application
```

## :material-handshake: How to Contribute

1. **Fork** the repository
2. Create a **branch** (`git checkout -b feature/my-feature`)
3. **Commit** your changes (`git commit -m 'feat: add my feature'`)
4. **Push** to your branch (`git push origin feature/my-feature`)
5. Open a **Pull Request**

See the [Contributing Guide](contributing.md) for full details.

## Pre-PR Checklist

- [ ] Code compiles without errors
- [ ] All tests pass (`./mvnw test`)
- [ ] Coverage > 80% for new code
- [ ] Javadoc added for public APIs
- [ ] CHANGELOG.md updated
- [ ] Commit messages follow conventional commits

## :material-tools: Recommended Tools

- **IDE**: IntelliJ IDEA / Eclipse / VS Code
- **Java**: JDK 17 or higher
- **Build**: Maven 3.6+

## Developer Documentation

- [CONTRIBUTING.md](contributing.md) — full contribution guide
- [Architecture](../advanced/architecture.md) — architectural decisions

---

Questions? Open a [discussion](https://github.com/tky0065/springflow/discussions) or an [issue](https://github.com/tky0065/springflow/issues).
