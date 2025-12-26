# Development

Guide pour contribuer au développement de SpringFlow.

## 📚 Dans cette section

<div class="grid cards" markdown>

-   :material-hands-pray: **[Contributing](contributing.md)**

    ---

    Guide de contribution au projet

-   :material-hammer-wrench: **[Building](building.md)**

    ---

    Compilation et build du projet

-   :material-test-tube: **[Testing](testing.md)**

    ---

    Stratégie et exécution des tests

-   :material-rocket-launch: **[Release Process](release.md)**

    ---

    Processus de publication des versions

</div>

## 🚀 Quick Start pour Développeurs

### 1. Cloner le Repository

```bash
git clone https://github.com/tky0065/springflow.git
cd springflow
```

### 2. Build

```bash
./mvnw clean install
```

### 3. Exécuter les Tests

```bash
./mvnw test
```

### 4. Lancer la Demo

```bash
cd springflow-demo
../mvnw spring-boot:run
```

## 🏗️ Structure du Projet

```
springflow/
├── springflow-annotations/   # Annotations (zéro dépendance)
├── springflow-core/          # Implémentation
├── springflow-starter/       # Auto-configuration Spring Boot
└── springflow-demo/          # Application de démonstration
```

## 🤝 Comment Contribuer ?

1. **Fork** le repository
2. Créer une **branche** (`git checkout -b feature/amazing-feature`)
3. **Commiter** vos changements (`git commit -m 'feat: add amazing feature'`)
4. **Pusher** vers la branche (`git push origin feature/amazing-feature`)
5. Ouvrir une **Pull Request**

Consultez [Contributing Guide](contributing.md) pour plus de détails.

## 📋 Checklist Avant PR

- [ ] Code compile sans erreurs
- [ ] Tous les tests passent (`./mvnw test`)
- [ ] Coverage > 80% pour le nouveau code
- [ ] Javadoc ajoutée pour les APIs publiques
- [ ] CHANGELOG.md mis à jour
- [ ] Commit messages suivent la convention

## 🛠️ Outils Recommandés

- **IDE**: IntelliJ IDEA / Eclipse / VS Code
- **Java**: JDK 17 ou supérieur
- **Build**: Maven 3.6+
- **Git**: Dernière version

## 📖 Documentation Développeur

- [CONTRIBUTING.md](contributing.md) - Guide de contribution complet
- [Architecture Decision Records](../advanced/architecture.md) - Décisions d'architecture

---

Besoin d'aide ? Ouvrez une [discussion](https://github.com/tky0065/springflow/discussions) ou une [issue](https://github.com/tky0065/springflow/issues).
