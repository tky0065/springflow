# Guide Rapide de Déploiement SpringFlow

Guide pratique pour publier SpringFlow sur Maven Central.

## Commandes Rapides

### 1. Publication sur Maven Central

```bash
# Vérifier que tout est prêt
./mvnw clean verify

# Déployer sur Maven Central
./mvnw clean deploy -Prelease

# Déployer sans tests (plus rapide, mais déconseillé)
./mvnw clean deploy -Prelease -DskipTests
```

### 2. Créer une release Git

```bash
# Créer un tag de version
git tag -a v0.1.0 -m "Release SpringFlow v0.1.0 - Phase 1 MVP"

# Pousser le tag
git push origin v0.1.0

# Créer une release GitHub avec notes
gh release create v0.1.0 \
  --title "SpringFlow v0.1.0 - Phase 1 MVP" \
  --notes "$(cat <<'EOF'
# SpringFlow v0.1.0 - Phase 1 MVP

Auto-generate REST APIs from JPA entities with a single @AutoApi annotation.

## Features

- **@AutoApi** - Single annotation to generate complete REST APIs
- **Full CRUD** - GET (list/single), POST, PUT, DELETE
- **Pagination & Sorting** - Spring Data integration
- **Validation** - JSR-380 Bean Validation support
- **OpenAPI/Swagger** - Automatic API documentation
- **Field Control** - @Hidden, @ReadOnly, @Filterable annotations

## Installation

Maven:
```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

Gradle:
```gradle
implementation 'io.github.tky0065:springflow-starter:0.1.0'
```

## Documentation

- [README.md](README.md) - Complete usage guide
- [CHANGELOG.md](CHANGELOG.md) - Detailed release notes

EOF
)"
```

### 3. Préparer la prochaine version

```bash
# Mettre à jour la version dans tous les pom.xml vers 0.2.0-SNAPSHOT
# Puis:
git add .
git commit -m "chore: prepare for next development iteration (0.2.0-SNAPSHOT)"
git push origin main
```

## Prérequis (Configuration Initiale)

### GPG Signature

```bash
# Vérifier si GPG est installé
gpg --version

# Installer GPG si nécessaire (macOS)
brew install gnupg

# Générer une clé GPG (si pas déjà fait)
gpg --full-generate-key

# Lister les clés
gpg --list-secret-keys --keyid-format=long

# Publier la clé sur les serveurs
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID

# Exporter GPG_TTY (si erreur "Inappropriate ioctl for device")
export GPG_TTY=$(tty)
echo 'export GPG_TTY=$(tty)' >> ~/.zshrc
```

### Configuration Maven (~/.m2/settings.xml)

```xml
<settings>
  <servers>
    <!-- Token Maven Central (nouveau système) -->
    <server>
      <id>central</id>
      <username>YOUR_TOKEN_USERNAME</username>
      <password>YOUR_TOKEN_PASSWORD</password>
    </server>
  </servers>

  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <!-- GPG Passphrase (si votre clé a un mot de passe) -->
        <gpg.passphrase>YOUR_GPG_PASSPHRASE</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

**Obtenir un token:**
1. Aller sur https://central.sonatype.com/
2. Se connecter
3. View Account → Generate User Token
4. Copier username et password dans settings.xml

## Vérifications Avant Publication

```bash
# 1. Tests passent
./mvnw clean test

# 2. Coverage >80%
./mvnw jacoco:report
# Ouvrir target/site/jacoco/index.html

# 3. Démo fonctionne
cd springflow-demo
../mvnw spring-boot:run
# Tester http://localhost:8080/swagger-ui.html

# 4. Version correcte (sans SNAPSHOT)
grep "<version>" pom.xml | head -5

# 5. Git clean
git status

# 6. README et CHANGELOG à jour
cat README.md | grep "version"
cat CHANGELOG.md | head -20
```

## Après Publication

### Vérifier la publication

```bash
# Attendre 10-30 minutes, puis vérifier:
# Maven Central Search: https://search.maven.org/search?q=g:io.github.tky0065
# MVN Repository: https://mvnrepository.com/artifact/io.github.tky0065

# Tester l'installation
cd /tmp
mkdir test-springflow && cd test-springflow

# Créer un projet test
cat > pom.xml <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>test</groupId>
    <artifactId>test</artifactId>
    <version>1.0</version>

    <dependencies>
        <dependency>
            <groupId>io.github.tky0065</groupId>
            <artifactId>springflow-starter</artifactId>
            <version>0.1.0</version>
        </dependency>
    </dependencies>
</project>
EOF

# Télécharger depuis Maven Central
mvn dependency:resolve
```

## Dépannage Rapide

### Erreur GPG "Inappropriate ioctl for device"
```bash
export GPG_TTY=$(tty)
```

### Erreur "401 Unauthorized"
```bash
# Vérifier credentials dans ~/.m2/settings.xml
# Vérifier que <server><id>central</id> correspond au POM
```

### Build échoue
```bash
# Voir logs détaillés
./mvnw deploy -Prelease -X | tee deploy.log

# Nettoyer cache Maven
rm -rf ~/.m2/repository/io/github/tky0065
./mvnw clean install
```

### Version SNAPSHOT rejetée
```bash
# Mettre à jour la version dans tous les pom.xml
# Remplacer 0.1.0-SNAPSHOT par 0.1.0
find . -name pom.xml -exec sed -i '' 's/0.1.0-SNAPSHOT/0.1.0/g' {} \;
```

## Workflow Complet de Release

```bash
# 1. Finir tous les développements
git checkout main
git pull

# 2. Mettre à jour version (enlever SNAPSHOT)
# Éditer tous les pom.xml: 0.1.0-SNAPSHOT → 0.1.0

# 3. Mettre à jour documentation
# Éditer README.md, CHANGELOG.md avec version 0.1.0

# 4. Commit de release
git add .
git commit -m "chore: release v0.1.0"

# 5. Créer tag
git tag -a v0.1.0 -m "Release SpringFlow v0.1.0 - Phase 1 MVP"

# 6. Push
git push origin main
git push origin v0.1.0

# 7. Build et vérifications
./mvnw clean verify

# 8. Déployer sur Maven Central
./mvnw clean deploy -Prelease

# 9. Créer GitHub Release
gh release create v0.1.0 --title "SpringFlow v0.1.0" --notes-file CHANGELOG.md

# 10. Passer à version suivante
# Éditer tous les pom.xml: 0.1.0 → 0.2.0-SNAPSHOT
git add .
git commit -m "chore: prepare for next development iteration (0.2.0-SNAPSHOT)"
git push origin main
```

## Ressources

- **Guide complet:** [PUBLISHING.md](PUBLISHING.md)
- **Maven Central:** https://central.sonatype.com/
- **Documentation:** https://central.sonatype.org/publish/
- **Support:** https://community.sonatype.com/

## Checklist

Avant de déployer, vérifier:

- [ ] Version sans SNAPSHOT dans tous les pom.xml
- [ ] Tests passent: `./mvnw clean test`
- [ ] README.md à jour avec version correcte
- [ ] CHANGELOG.md contient toutes les modifications
- [ ] Tag Git créé et poussé
- [ ] GPG configuré et clé publiée
- [ ] Credentials Maven Central dans ~/.m2/settings.xml
- [ ] Demo testée à http://localhost:8080/swagger-ui.html
