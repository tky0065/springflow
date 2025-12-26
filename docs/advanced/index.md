# Advanced Topics

Maîtrisez les concepts avancés de SpringFlow pour optimiser vos applications.

## :material-book-open-variant: Dans cette section

<div class="grid cards" markdown>

-   :material-chart-tree: **[Architecture](architecture.md)**

    ---

    Comprendre l'architecture interne de SpringFlow

-   :material-cog-outline: **[Custom Components](custom-components.md)**

    ---

    Repositories, Services et Controllers personnalisés - Guide complet avec 4 scénarios

-   :material-api: **[Custom Endpoints](custom-endpoints.md)**

    ---

    Exemples rapides pour ajouter des endpoints personnalisés

-   :material-speedometer: **[Performance](performance.md)**

    ---

    Optimisation et tuning des performances

-   :material-star-check: **[Best Practices](best-practices.md)**

    ---

    Patterns et recommandations

</div>

## :material-target: Pour qui ?

Cette section est destinée aux développeurs qui souhaitent :

- **Comprendre** comment SpringFlow fonctionne sous le capot
- **Personnaliser** repositories, services et controllers avec leurs propres implémentations
- **Étendre** le comportement par défaut avec des endpoints custom
- **Optimiser** les performances pour la production
- **Appliquer** les meilleures pratiques

## :material-wrench: Concepts Clés

### Architecture Runtime

SpringFlow génère les composants **au runtime** via :

- `BeanDefinitionRegistryPostProcessor` pour l'enregistrement des beans
- Reflection API pour l'introspection des entités
- Proxy dynamique pour les repositories générés

### Extensibilité

SpringFlow est conçu pour être étendu avec vos composants personnalisés :

- **Détection automatique** - SpringFlow détecte et respecte vos repositories, services et controllers custom
- **4 scénarios** - Repository seul, Service seul, Controller seul, ou tous les trois custom
- **Classes de base** - Étendez `GenericCrudService` et `GenericCrudController` pour hériter des fonctionnalités
- **Hooks de lifecycle** - beforeCreate, afterCreate, beforeUpdate, afterUpdate, beforeDelete, afterDelete
- **Specifications JPA** - Vos repositories peuvent ajouter des méthodes de requête personnalisées
- **Intégration** - Compatible avec votre architecture Spring existante

### Performance

Points clés pour la performance :

- Fetch joins automatiques pour éviter N+1
- Cache des métadonnées d'entités
- Pagination efficace avec COUNT optimisé
- DTOs légers pour les réponses

---

**Prérequis :** Familiarité avec Spring Boot, JPA, et les concepts de base de SpringFlow.

Commencez par [Architecture](architecture.md) pour comprendre les fondations.
