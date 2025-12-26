# User Guide

Guide complet d'utilisation de SpringFlow pour maîtriser toutes les fonctionnalités.

## :material-book-open-variant: Table des matières

### Core Features

<div class="grid cards" markdown>

-   :material-label: **[Annotations](annotations.md)**

    ---

    Référence complète des annotations SpringFlow

-   :material-cog: **[Configuration](configuration.md)**

    ---

    Options de configuration via YAML et annotations

-   :material-sync: **[DTO Mapping](dto-mapping.md)**

    ---

    Mapping automatique entre entités et DTOs

</div>

### Data Management

<div class="grid cards" markdown>

-   :material-magnify: **[Filtering](filtering.md)**

    ---

    Filtrage dynamique avec @Filterable

-   :material-file-document: **[Pagination & Sorting](pagination.md)**

    ---

    Pagination et tri des résultats

-   :material-check-circle: **[Validation](validation.md)**

    ---

    Validation des données avec JSR-380

</div>

### Advanced Features

<div class="grid cards" markdown>

-   :material-lock: **[Security](security.md)**

    ---

    Intégration Spring Security et contrôle d'accès

-   :material-delete: **[Soft Delete](soft-delete.md)**

    ---

    Suppression logique avec restauration

-   :material-clipboard-text: **[Auditing](auditing.md)**

    ---

    Traçabilité automatique (createdAt, updatedAt, etc.)

-   :material-language-kotlin: **[Kotlin Support](kotlin.md)**

    ---

    Utilisation avec Kotlin et data classes

</div>

## :material-target: Par où commencer ?

Si vous débutez avec SpringFlow, nous recommandons de lire dans cet ordre :

1. **[Annotations](annotations.md)** - Comprendre `@AutoApi` et les annotations de champs
2. **[Configuration](configuration.md)** - Personnaliser le comportement
3. **[DTO Mapping](dto-mapping.md)** - Comprendre le mapping automatique
4. **[Pagination](pagination.md)** - Gérer les grandes collections

Ensuite, explorez les fonctionnalités avancées selon vos besoins !

## :material-magnify: Recherche Rapide

**Je veux...**

- Créer une API simple → [Annotations](annotations.md)
- Filtrer les résultats → [Filtering](filtering.md)
- Paginer les résultats → [Pagination](pagination.md)
- Valider les données → [Validation](validation.md)
- Sécuriser mes endpoints → [Security](security.md)
- Garder l'historique → [Auditing](auditing.md)
- Permettre la restauration → [Soft Delete](soft-delete.md)
- Utiliser Kotlin → [Kotlin Support](kotlin.md)

---

Besoin d'aide ? Consultez les [exemples avancés](../advanced/architecture.md) ou la [référence API](../api/annotations.md).
