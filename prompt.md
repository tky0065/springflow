# Prompt d'Implémentation SpringFlow

## Contexte

Tu es un agent d'implémentation pour le projet **SpringFlow**. Ton rôle est d'implémenter les tâches définies dans `tasks.md` en suivant les spécifications.

## Ressources Disponibles

- **spec.md** : Spécifications complètes du projet
- **roadmap.md** : Vision et phases du projet
- **plan.md** : Architecture et plan d'implémentation détaillé
- **tasks.md** : Liste complète des tâches à réaliser
- **README.md** : Documentation utilisateur

## Instructions

### 1. Workflow

1. **Lire** `tasks.md` et identifier la prochaine tâche non complétée `[ ]`
2. **Consulter** `spec.md` et `plan.md` pour comprendre les requis
3. **Implémenter** la tâche (code, tests, doc)
4. **Tester** que l'implémentation fonctionne
5. **Marquer** la tâche comme terminée `[x]` dans `tasks.md`
6. **Committer** avec message: `feat(module): description de la tâche`
7. **commmite messages** : Pas de 🤖 Generated with [Claude Code](https://claude.com/claude-code) Co-Authored-By: Claude <noreply@anthropic.com>>

### 2. Ordre d'Exécution

Respecter l'ordre des modules dans `tasks.md`:
- Phase 1: Modules 1-15 (MVP)
- Phase 2: Modules 16-21 (Advanced)
- Phase 3: Modules 22-26 (Extended)

**⚠️ Ne pas sauter de modules**, les dépendances sont critiques.

### 3. Standards de Code

- **Java 17+**, Spring Boot 3.2+
- **Tests** : Coverage >80%
- **Javadoc** : Sur toutes les classes publiques
- **Naming** : Conventions Spring (camelCase, PascalCase)
- **Logs** : SLF4J avec niveaux appropriés

### 4. Structure des Commits

```
feat(module-name): description courte

- Détail 1
- Détail 2
- Marque tâche X comme complétée
```

### 5. Mise à Jour de tasks.md

Après chaque tâche complétée, remplacer `- [ ]` par `- [x]`:

```markdown
#### 2.1 @AutoApi Annotation
- [x] Créer interface `@AutoApi`
- [x] Ajouter paramètre `path`
- [ ] Ajouter paramètre `expose` (enum)
```

### 6. En Cas de Blocage

Si une tâche est bloquée:
1. Ajouter un commentaire dans `tasks.md` avec `⚠️ BLOCKED: raison`
2. Documenter le problème
3. Passer à la prochaine tâche non-dépendante si possible

### 7. Tests Obligatoires

Pour chaque module:
- ✅ Tests unitaires
- ✅ Tests d'intégration
- ✅ Test dans `springflow-demo`

### 8. Documentation

À chaque module complété:
- Javadoc complet
- README dans le module si nécessaire
- Mise à jour du README principal si feature majeure

## Commandes Utiles

```bash
# Build
./mvnw clean install

# Tests
./mvnw test

# Tests d'un module
./mvnw test -pl springflow-core

# Run demo
cd springflow-demo && ./mvnw spring-boot:run

# Vérifier coverage
./mvnw jacoco:report
```

## Checklist par Tâche

- [ ] Code implémenté
- [ ] Tests écrits et passent
- [ ] Javadoc ajoutée
- [ ] Demo testée (si applicable)
- [ ] Tâche marquée `[x]` dans tasks.md
- [ ] Commit effectué

## Exemple de Session

```bash
# 1. Identifier tâche
cat tasks.md | grep "- \[ \]" | head -1

# 2. Implémenter
# ... code ...

# 3. Tester
./mvnw test

# 4. Marquer complété
# Éditer tasks.md: [ ] → [x]

# 5. Commit
git add .
git commit -m "feat(annotations): créer @AutoApi annotation"
```

## Important

- **Qualité > Vitesse** : Code propre et testé
- **Respecter l'architecture** définie dans plan.md
- **Pas de raccourcis** : Chaque tâche compte
- **Communication** : Documenter les décisions importantes

---

**Objectif** : Implémenter SpringFlow selon spec.md, une tâche à la fois, en maintenant tasks.md à jour.

**Statut actuel** : Consulter tasks.md pour voir la progression.
