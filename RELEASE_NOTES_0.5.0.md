# SpringFlow v0.5.0 - Enhanced Logging & API Responses

**Date de release** : 2025-12-30

## 🎯 Nouveautés majeures

### PageResponse DTO
- Nouveau wrapper personnalisé pour les réponses paginées
- Élimine le warning Spring Data "Serializing PageImpl as-is not supported"
- Structure JSON stable et explicite
- Rétrocompatibilité totale avec la structure PageImpl

### Filtrage intelligent des logs
- Détection automatique des requêtes de bots/scanners
- Configuration des patterns de bots via `springflow.logging.botPatterns`
- Logs DEBUG pour les bots au lieu de ERROR (réduction du bruit)
- Distinction intelligente entre API endpoints et ressources statiques

## 📊 Changements techniques

### API
- **Breaking change** : Aucun (rétrocompatible à 100%)
- Return type de `findAll()` : `PageResponse<Map<String, Object>>`
- Structure JSON de pagination identique à PageImpl

### Configuration
- Nouvelle section `springflow.logging` dans application.yml
- `logBotRequests` : contrôle le niveau de log pour les bots
- `botPatterns` : liste personnalisable de patterns de détection

### Code
- Nouveau : `PageResponse.java` et `PageResponse.PageMetadata`
- Modifié : `GlobalExceptionHandler` avec handler `NoResourceFoundException`
- Modifié : `GenericCrudController.findAll()` return type
- Modifié : `SpringFlowProperties` avec classe interne `Logging`

## 🔧 Dépendances

- Spring Boot : 4.0.1
- Java : 17+ (testé avec Java 25)
- Kotlin : 2.2.0

## 📦 Installation

Maven :
```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.5.0</version>
</dependency>
```

Gradle :
```gradle
implementation 'io.github.tky0065:springflow-starter:0.5.0'
```

## 🧪 Tests

- 191 tests au total
- 10 nouveaux tests : GlobalExceptionHandlerTest
- 9 nouveaux tests : PageResponseTest
- Couverture : 80%+
- Tous les tests passent ✅

## 📚 Documentation

- Guide de configuration du logging
- Documentation PageResponse API
- Exemples de détection de bots

## 🔄 Migration depuis 0.4.x

**Aucune action requise** - Cette version est 100% rétrocompatible.

Optionnel : Ajouter la configuration logging dans `application.yml` pour personnaliser le filtrage des bots.

---

Pour plus de détails, consultez le [CHANGELOG.md](CHANGELOG.md).
