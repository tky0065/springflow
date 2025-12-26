# Building SpringFlow

Guide de compilation du projet.

## Prérequis

- **JDK**: 17 ou supérieur
- **Maven**: 3.6 ou supérieur
- **Git**: Dernière version

## Clone

```bash
git clone https://github.com/tky0065/springflow.git
cd springflow
```

## Build Complet

```bash
./mvnw clean install
```

Cela compile et installe tous les modules :
- `springflow-annotations`
- `springflow-core`
- `springflow-starter`
- `springflow-demo`

## Build Sans Tests

```bash
./mvnw clean install -DskipTests
```

## Build d'un Module Spécifique

```bash
cd springflow-core
../mvnw clean install
```

## Compilation Kotlin

Le projet supporte Kotlin. Le plugin compile automatiquement les sources `.kt` :

```bash
./mvnw clean compile
```

## Génération Javadoc

```bash
./mvnw javadoc:javadoc
```

Documentation générée dans `target/site/apidocs/`

## Build de Production

```bash
./mvnw clean install -Pprod
```

## Voir Aussi

- [Testing](testing.md)
- [Contributing](contributing.md)
