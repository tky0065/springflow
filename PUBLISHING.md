# Guide de Publication sur Maven Central

Ce document explique comment publier SpringFlow sur Maven Central Repository.

## Prérequis

### 1. Créer un compte Sonatype OSSRH

1. Créer un compte sur [Sonatype JIRA](https://issues.sonatype.org/secure/Signup!default.jspa)
2. Créer un ticket pour demander l'accès au namespace `io.github.tky0065`
   - Project: Community Support - Open Source Project Repository Hosting (OSSRH)
   - Issue Type: New Project
   - Group Id: io.github.tky0065
   - Project URL: https://github.com/tky0065/springflow
   - SCM URL: https://github.com/tky0065/springflow.git

**Note:** L'approbation peut prendre 1-2 jours ouvrables.

### 2. Générer une clé GPG

Les artifacts Maven Central doivent être signés avec GPG.

#### Installation de GPG

**macOS:**
```bash
brew install gnupg
```

**Ubuntu/Debian:**
```bash
sudo apt-get install gnupg
```

**Windows:**
Télécharger depuis [GnuPG.org](https://gnupg.org/download/)

#### Générer une clé GPG

```bash
# Générer une nouvelle clé
gpg --full-generate-key

# Sélectionner:
# - Type: RSA and RSA (default)
# - Key size: 4096
# - Expiration: 0 (pas d'expiration) ou 2y (2 ans)
# - Real name: SpringFlow Team (ou votre nom)
# - Email: tky0065@github.com (ou votre email)
# - Comment: SpringFlow Release Key (optionnel)
```

#### Publier la clé sur un serveur de clés

```bash
# Lister les clés pour obtenir l'ID
gpg --list-secret-keys --keyid-format=long

# Exemple de sortie:
# sec   rsa4096/ABCD1234EFGH5678 2025-12-22 [SC]
#       1234567890ABCDEF1234567890ABCDEF12345678
# uid                 [ultimate] SpringFlow Team <tky0065@github.com>

# Publier la clé (remplacer ABCD1234EFGH5678 par votre ID)
gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234EFGH5678
gpg --keyserver keys.openpgp.org --send-keys ABCD1234EFGH5678
gpg --keyserver pgp.mit.edu --send-keys ABCD1234EFGH5678
```

### 3. Configurer les credentials Maven

Éditer `~/.m2/settings.xml` (créer le fichier s'il n'existe pas):

```xml
<settings>
  <servers>
    <!-- Credentials Sonatype OSSRH -->
    <server>
      <id>ossrh</id>
      <username>VOTRE_USERNAME_SONATYPE</username>
      <password>VOTRE_PASSWORD_SONATYPE</password>
    </server>

    <!-- Token pour Central Publishing (nouveau système) -->
    <server>
      <id>central</id>
      <username>VOTRE_TOKEN_USERNAME</username>
      <password>VOTRE_TOKEN_PASSWORD</password>
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
        <gpg.passphrase>VOTRE_PASSPHRASE_GPG</gpg.passphrase>

        <!-- OU utiliser gpg-agent pour éviter de stocker le mot de passe -->
        <!-- <gpg.useagent>true</gpg.useagent> -->
      </properties>
    </profile>
  </profiles>
</settings>
```

**Sécurité:** Pour éviter de stocker le mot de passe en clair, vous pouvez:
1. Utiliser `gpg-agent` (recommandé)
2. Chiffrer le mot de passe avec [Maven Password Encryption](https://maven.apache.org/guides/mini/guide-encryption.html)

## Publication sur Maven Central

### Méthode 1: Publication Manuelle (Recommandée pour la première fois)

#### Étape 1: Préparer la release

```bash
# S'assurer que tout est commit
git status

# Créer un tag de release
git tag -a v0.1.0 -m "Release version 0.1.0"
git push origin v0.1.0
```

#### Étape 2: Build et déploiement

```bash
# Clean build avec tests
./mvnw clean verify

# Déployer sur OSSRH Staging (avec signature GPG)
./mvnw clean deploy -Prelease

# OU si vous voulez skip les tests (déconseillé)
./mvnw clean deploy -Prelease -DskipTests
```

**Note:** Le profil `-Prelease` active le plugin GPG pour signer les artifacts.

#### Étape 3: Vérifier et publier sur Nexus Repository Manager

1. Se connecter à [Sonatype Nexus](https://s01.oss.sonatype.org/)
2. Cliquer sur "Staging Repositories" dans le menu de gauche
3. Trouver votre repository (ex: `iogithub-tky0065-1001`)
4. Sélectionner le repository et cliquer sur "Close"
   - Cela déclenche les validations automatiques
   - Attendre que le statut devienne "Closed" (1-5 minutes)
5. Si "Close" réussit, cliquer sur "Release"
   - Les artifacts seront publiés sur Maven Central
   - Synchronisation complète: 10-30 minutes
   - Disponibilité sur search.maven.org: 2-4 heures

#### Étape 4: Vérifier la publication

Après 2-4 heures, vérifier:
- [Maven Central Search](https://search.maven.org/search?q=g:io.github.tky0065)
- [MVN Repository](https://mvnrepository.com/artifact/io.github.tky0065)

```bash
# Tester l'installation depuis Maven Central
cd /tmp
mkdir test-springflow && cd test-springflow
mvn archetype:generate -DgroupId=test -DartifactId=test -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

# Ajouter SpringFlow comme dépendance dans pom.xml
# Puis:
mvn clean install
```

### Méthode 2: Publication Automatique (avec central-publishing-maven-plugin)

Le projet est déjà configuré avec `central-publishing-maven-plugin` qui simplifie le processus.

#### Étape 1: Obtenir un token de publication

1. Se connecter à [Sonatype Central Portal](https://central.sonatype.com/)
2. Aller dans "View Account" → "Generate User Token"
3. Copier le username et le token

#### Étape 2: Configurer le token dans settings.xml

```xml
<server>
  <id>central</id>
  <username>TOKEN_USERNAME</username>
  <password>TOKEN_PASSWORD</password>
</server>
```

#### Étape 3: Publier

```bash
# Publier automatiquement (le plugin close et release automatiquement)
./mvnw clean deploy -Prelease
```

Le plugin `central-publishing-maven-plugin` avec `autoPublish=true` va:
1. Uploader les artifacts
2. Closer le staging repository
3. Releaser automatiquement si les validations passent

## Checklist avant Publication

- [ ] Tous les tests passent: `./mvnw clean test`
- [ ] Code coverage >80%: `./mvnw jacoco:report`
- [ ] README.md à jour avec la version correcte
- [ ] CHANGELOG.md contient toutes les modifications
- [ ] Version mise à jour dans tous les pom.xml
- [ ] Tag Git créé: `git tag -a v0.1.0 -m "Release 0.1.0"`
- [ ] GPG configuré et clé publiée
- [ ] Credentials Sonatype configurés dans ~/.m2/settings.xml
- [ ] Demo application testée et fonctionnelle

## Commandes Utiles

```bash
# Vérifier la signature GPG
gpg --verify target/springflow-parent-0.1.0-SNAPSHOT.pom.asc

# Lister les artifacts générés
ls -lh target/*.jar target/*.pom target/*.asc

# Build sans déployer (dry run)
./mvnw clean verify -Prelease

# Voir les détails du profil release
./mvnw help:active-profiles -Prelease

# Nettoyer complètement
./mvnw clean && rm -rf ~/.m2/repository/io/github/tky0065
```

## Dépannage

### Erreur: "gpg: signing failed: Inappropriate ioctl for device"

Solution:
```bash
export GPG_TTY=$(tty)
echo 'export GPG_TTY=$(tty)' >> ~/.zshrc  # ou ~/.bashrc
```

### Erreur: "Return code is: 401, ReasonPhrase: Unauthorized"

- Vérifier que les credentials dans `~/.m2/settings.xml` sont corrects
- Vérifier que le `<server><id>` correspond à celui dans distributionManagement
- Vérifier que votre compte Sonatype a les permissions pour le namespace

### Erreur: "No public key"

- Publier votre clé GPG sur plusieurs serveurs de clés
- Attendre 5-10 minutes après publication
- Vérifier avec: `gpg --keyserver keyserver.ubuntu.com --search-keys VOTRE_EMAIL`

### Validation échoue sur "Close"

Les causes courantes:
- Javadoc manquant ou invalide
- Sources jar manquant
- Signatures GPG invalides
- POM incomplet (licence, SCM, developers manquants)
- Version SNAPSHOT (seules les versions release sont acceptées)

### Artefacts ne se synchronisent pas

- Vérifier que "Release" a été fait sur Nexus
- Attendre 2-4 heures minimum
- Vérifier sur https://repo1.maven.org/maven2/io/github/tky0065/

## Processus de Release Complet

### Pour v0.1.0 (première release SNAPSHOT)

```bash
# 1. S'assurer que tout fonctionne
./mvnw clean verify
cd springflow-demo && ../mvnw spring-boot:run
# Tester manuellement: http://localhost:8080/api/products

# 2. Commit final
git add .
git commit -m "chore: prepare release v0.1.0-SNAPSHOT"

# 3. Créer tag
git tag -a v0.1.0-SNAPSHOT -m "Release version 0.1.0-SNAPSHOT

Phase 1 MVP - Complete REST API auto-generation from JPA entities
- All 15 modules implemented
- CRUD operations, pagination, sorting, validation
- Swagger documentation
- Kotlin support
- Lombok integration
"

# 4. Push
git push origin main
git push origin v0.1.0-SNAPSHOT

# 5. Déployer
./mvnw clean deploy -Prelease

# 6. Vérifier sur Nexus et Release
```

### Pour v0.1.0 (release finale, sans SNAPSHOT)

```bash
# 1. Mettre à jour la version dans tous les pom.xml
# Changer <version>0.1.0-SNAPSHOT</version> -> <version>0.1.0</version>

# 2. Mettre à jour README.md et CHANGELOG.md
# Changer toutes les références 0.1.0-SNAPSHOT -> 0.1.0

# 3. Commit et tag
git add .
git commit -m "chore: release v0.1.0"
git tag -a v0.1.0 -m "Release version 0.1.0"
git push origin main
git push origin v0.1.0

# 4. Déployer
./mvnw clean deploy -Prelease

# 5. Après publication sur Maven Central, créer la release GitHub
gh release create v0.1.0 --title "SpringFlow v0.1.0" --notes-file CHANGELOG.md

# 6. Passer à la version suivante (0.2.0-SNAPSHOT)
# Mettre à jour tous les pom.xml: <version>0.2.0-SNAPSHOT</version>
git add .
git commit -m "chore: prepare for next development iteration"
git push origin main
```

## Ressources

- [Maven Central Publishing Guide](https://central.sonatype.org/publish/publish-guide/)
- [Maven Central Publishing Requirements](https://central.sonatype.org/publish/requirements/)
- [GPG Signature Guide](https://central.sonatype.org/publish/requirements/gpg/)
- [Central Publishing Maven Plugin](https://central.sonatype.org/publish/publish-portal-maven/)
- [Sonatype OSSRH Guide](https://central.sonatype.org/publish/publish-guide/)

## Support

En cas de problème:
1. Vérifier les logs Maven avec `-X`: `./mvnw deploy -Prelease -X`
2. Consulter la [documentation Sonatype](https://central.sonatype.org/publish/)
3. Demander de l'aide sur [Sonatype Community](https://community.sonatype.com/)
4. Créer un ticket sur [Sonatype JIRA](https://issues.sonatype.org/)
