# Contributing to SpringFlow

Thank you for your interest in contributing to SpringFlow!

## Development Setup

### Prerequisites
- Java 17 or higher
- Maven 3.8+ (or use included Maven wrapper)
- Git

### Getting Started

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/springflow.git
   cd springflow
   ```

3. Build the project:
   ```bash
   ./mvnw clean install
   ```

4. Run tests:
   ```bash
   ./mvnw test
   ```

## Project Structure

```
springflow/
├── springflow-annotations/  # Core annotations
├── springflow-core/         # Framework implementation
├── springflow-starter/      # Spring Boot auto-configuration
└── springflow-demo/         # Demo application
```

## Development Workflow

1. Create a feature branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes following the coding standards

3. Write/update tests for your changes

4. Run the full test suite:
   ```bash
   ./mvnw clean verify
   ```

5. Commit your changes:
   ```bash
   git commit -m "feat(module): description of changes"
   ```

6. Push to your fork and create a Pull Request

## Commit Message Convention

We follow conventional commits:

- `feat(module): add new feature`
- `fix(module): fix bug`
- `docs: update documentation`
- `test: add tests`
- `refactor: refactor code`
- `chore: update dependencies`

## Coding Standards

- **Java 17+** features are encouraged
- Follow Spring Boot conventions
- Use **Lombok** for boilerplate reduction
- Write **Javadoc** for all public APIs
- Maintain **test coverage > 80%**
- Use **SLF4J** for logging

## Testing

- Unit tests: Test individual components in isolation
- Integration tests: Test component interactions
- Use `@SpringBootTest` for full application context tests
- Use H2 in-memory database for testing

## Code Quality

Before submitting a PR, ensure:

- [ ] Code compiles without warnings
- [ ] All tests pass
- [ ] Code coverage is maintained or improved
- [ ] No SonarQube critical issues
- [ ] Javadoc is complete and accurate

## Documentation

- Update README.md if adding user-facing features
- Update module-specific READMEs as needed
- Add examples to the demo application
- Update API reference documentation

## Pull Request Process

1. Update the CHANGELOG.md with your changes
2. Ensure CI/CD pipeline passes
3. Request review from maintainers
4. Address review feedback
5. Squash commits if requested
6. Wait for approval and merge

## Getting Help

- Create an issue for bugs or feature requests
- Join discussions in GitHub Discussions
- Check existing documentation in `/docs`

## Code of Conduct

- Be respectful and inclusive
- Welcome newcomers
- Focus on constructive feedback
- Help others learn and grow

## License

By contributing to SpringFlow, you agree that your contributions will be licensed under the project's license.
