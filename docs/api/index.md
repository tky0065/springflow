# API Reference

Documentation technique complète de l'API SpringFlow.

## 📚 Références

<div class="grid cards" markdown>

-   :label: **[Annotations API](annotations.md)**

    ---

    Toutes les annotations avec paramètres détaillés

-   :gear: **[Configuration Properties](configuration.md)**

    ---

    Propriétés YAML springflow.*

-   :material-api: **[Generated Endpoints](endpoints.md)**

    ---

    Endpoints REST générés automatiquement

</div>

## 🎯 Vue d'ensemble

### Annotations Principales

| Annotation | Description | Cible |
|------------|-------------|-------|
| `@AutoApi` | Active la génération d'API | Entité |
| `@Filterable` | Active le filtrage dynamique | Champ |
| `@Hidden` | Exclut du DTO | Champ |
| `@ReadOnly` | Lecture seule | Champ |
| `@SoftDelete` | Suppression logique | Entité |
| `@Auditable` | Traçabilité automatique | Entité |

### Configuration Properties

```yaml
springflow:
  enabled: true
  base-path: /api
  base-packages: com.example.myapp
  pagination:
    default-page-size: 20
    max-page-size: 100
  swagger:
    enabled: true
```

### Endpoints Générés

Pour chaque entité avec `@AutoApi`, SpringFlow génère :

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/{path}` | Liste avec pagination |
| `GET` | `/api/{path}/{id}` | Détails d'une entité |
| `POST` | `/api/{path}` | Création |
| `PUT` | `/api/{path}/{id}` | Mise à jour complète |
| `DELETE` | `/api/{path}/{id}` | Suppression |

## 📖 Documentation Détaillée

- **[Annotations API](annotations.md)** - Paramètres, exemples, cas d'usage
- **[Configuration Properties](configuration.md)** - Toutes les options de configuration
- **[Generated Endpoints](endpoints.md)** - Format des requêtes et réponses

---

Pour des exemples pratiques, consultez le [User Guide](../guide/index.md).
