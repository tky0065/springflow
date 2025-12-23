# Guide Rapide - Déployer sur Maven Central

## Version 0.1.0 ✅ DÉJÀ PUBLIÉE

La version 0.1.0 est déjà disponible sur Maven Central!

Vérifier: https://search.maven.org/search?q=g:io.github.tky0065

## Publier une Nouvelle Version

### Étape 1: Mettre à jour la version

Décider de la nouvelle version (0.1.1 pour patch, 0.2.0 pour minor):

```bash
# Pour un patch (bugfix)
NEW_VERSION="0.1.1"

# OU pour une version minor (nouvelles fonctionnalités)
NEW_VERSION="0.2.0"

# Mettre à jour tous les pom.xml
find . -name pom.xml -exec sed -i '' "s/<version>0.1.0<\/version>/<version>$NEW_VERSION<\/version>/g" {} \;

# Vérifier les changements
git diff pom.xml
```

### Étape 2: Mettre à jour la documentation

```bash
# README.md
# Changer toutes les références de version
sed -i '' 's/0.1.0/0.1.1/g' README.md

# CHANGELOG.md
# Ajouter une nouvelle section pour la version
cat >> CHANGELOG.md <<'CHANGELOG'

## [0.1.1] - 2025-12-23

### Fixed
- Correction des paths dupliqués dans Swagger UI (/api/api → /api)
- Fix de l'OpenAPI server URL configuration
- Résolution du problème "No operations defined in spec!"

### Changed
- Amélioration de la documentation OpenAPI
- Optimisation du SpringFlowOpenApiCustomizer

CHANGELOG
```

### Étape 3: Commit et Tag

```bash
# Commit
git add .
git commit -m "chore: release v0.1.1 - Fix Swagger paths"

# Créer tag
git tag -a v0.1.1 -m "$(cat <<'TAG'
SpringFlow v0.1.1 - Swagger Fixes

Bugfix release corrigeant les problèmes d'affichage dans Swagger UI.

Fixes:
- Correction des paths dupliqués (/api/api → /api)
- Fix OpenAPI server URL
- Résolution "No operations defined in spec!"
TAG
)"

# Push
git push origin main
git push origin v0.1.1
```

### Étape 4: Déployer

```bash
# Exporter GPG_TTY
export GPG_TTY=$(tty)

# Build et deploy (sans tests pour aller plus vite)
./mvnw clean deploy -Prelease -Dmaven.test.skip=true

# Ou avec tests si vous avez le temps
./mvnw clean deploy -Prelease
```

### Étape 5: Créer GitHub Release

```bash
gh release create v0.1.1 \
  --title "SpringFlow v0.1.1 - Swagger Fixes" \
  --notes "$(cat <<'NOTES'
## Corrections

Cette version corrige les problèmes d'affichage dans Swagger UI.

### Fixed
- ✅ Correction des paths dupliqués `/api/api/categories` → `/api/categories`
- ✅ Fix de la configuration OpenAPI server URL
- ✅ Résolution du problème "No operations defined in spec!"
- ✅ Les endpoints apparaissent maintenant correctement dans Swagger UI

### Installation

Maven:
```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.1.1</version>
</dependency>
```

Gradle:
```gradle
implementation 'io.github.tky0065:springflow-starter:0.1.1'
```

### Vérification

Swagger UI maintenant disponible avec tous les endpoints documentés!

NOTES
)"
```

## Commandes Utiles

### Vérifier la version actuelle
```bash
grep "<version>" pom.xml | grep -v "<?xml" | head -5
```

### Vérifier sur Maven Central
```bash
# Recherche générale
open "https://search.maven.org/search?q=g:io.github.tky0065"

# Version spécifique
open "https://mvnrepository.com/artifact/io.github.tky0065/springflow-starter"
```

### Tester l'installation
```bash
cd /tmp
cat > pom.xml <<'POM'
<?xml version="1.0"?>
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>test</groupId>
    <artifactId>test</artifactId>
    <version>1.0</version>
    <dependencies>
        <dependency>
            <groupId>io.github.tky0065</groupId>
            <artifactId>springflow-starter</artifactId>
            <version>0.1.1</version>
        </dependency>
    </dependencies>
</project>
POM

mvn dependency:resolve
```

### Rollback si nécessaire
```bash
# Supprimer le tag local et distant
git tag -d v0.1.1
git push origin :refs/tags/v0.1.1

# Revenir au commit précédent
git reset --hard HEAD~1
git push origin main --force
```

## Dépannage

### Erreur: Version already exists
```bash
# Incrémentez la version et recommencez
# 0.1.1 → 0.1.2 ou 0.2.0
```

### Erreur: GPG signing failed
```bash
# Exporter GPG_TTY
export GPG_TTY=$(tty)
echo 'export GPG_TTY=$(tty)' >> ~/.zshrc

# Vérifier la clé GPG
gpg --list-secret-keys
```

### Erreur: 401 Unauthorized
```bash
# Vérifier credentials dans ~/.m2/settings.xml
cat ~/.m2/settings.xml | grep -A5 "<server>"

# Regénérer le token sur https://central.sonatype.com/
# View Account → Generate User Token
```

## Notes

- **Délai de publication**: 10-30 minutes après le deploy
- **Synchronisation Maven Central**: 2-4 heures
- **Module demo**: Publié mais non nécessaire pour les utilisateurs (dependency optional)
- **Tests**: Peuvent être skippés avec `-Dmaven.test.skip=true` pour aller plus vite

