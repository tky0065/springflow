# Advanced Topics

Maîtrisez les concepts avancés de SpringFlow pour optimiser vos applications.

## :material-book-open-variant: Dans cette section

<div class="grid cards" markdown>

-   :material-chart-tree: **[Architecture](architecture.md)**

    ---

    Comprendre l'architecture interne de SpringFlow

-   :material-api: **[Custom Endpoints](custom-endpoints.md)**

    ---

    Ajouter vos propres endpoints personnalisés

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

SpringFlow est conçu pour être étendu :

- Override des contrôleurs générés
- Hooks de service personnalisés
- Specifications JPA personnalisées
- Intégration avec votre architecture existante

### Performance

Points clés pour la performance :

- Fetch joins automatiques pour éviter N+1
- Cache des métadonnées d'entités
- Pagination efficace avec COUNT optimisé
- DTOs légers pour les réponses

---

**Prérequis :** Familiarité avec Spring Boot, JPA, et les concepts de base de SpringFlow.

Commencez par [Architecture](architecture.md) pour comprendre les fondations.
