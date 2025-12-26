# Release Process

Processus de publication des versions.

## Versioning

SpringFlow suit [Semantic Versioning](https://semver.org/) :

- **MAJOR** : Changements incompatibles
- **MINOR** : Nouvelles fonctionnalités (compatibles)
- **PATCH** : Bug fixes

Exemple : `0.2.0`

## Checklist Pré-Release

- [ ] Tous les tests passent (`./mvnw test`)
- [ ] Coverage >80%
- [ ] CHANGELOG.md à jour
- [ ] Version mise à jour dans tous les POMs
- [ ] Documentation à jour
- [ ] Release notes rédigées

## Process

### 1. Mise à Jour de la Version

```bash
# Changer version dans tous les pom.xml
mvn versions:set -DnewVersion=0.2.0
```

### 2. Commit et Tag

```bash
git add .
git commit -m "chore(release): prepare v0.2.0 release"
git tag -a v0.2.0 -m "Version 0.2.0"
git push origin main --tags
```

### 3. Publication Maven Central

Via GitHub Release :

1. Créer release sur GitHub
2. Workflow `publish.yml` se déclenche automatiquement
3. Artifacts publiés sur Maven Central

### 4. Documentation

Le workflow `docs.yml` met à jour automatiquement le site.

## Post-Release

- [ ] Annoncer sur GitHub Discussions
- [ ] Mise à jour README badges
- [ ] Bump version vers SNAPSHOT

```bash
mvn versions:set -DnewVersion=0.3.0-SNAPSHOT
```

## Voir Aussi

- [Contributing](contributing.md)
- [Changelog](../about/changelog.md)
