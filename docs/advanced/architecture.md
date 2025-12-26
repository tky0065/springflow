# Architecture

Architecture interne de SpringFlow.

## Vue d'Ensemble

SpringFlow utilise le **runtime bean generation** via Spring's `BeanDefinitionRegistryPostProcessor`.

```
@Entity + @AutoApi
        ↓
   EntityScanner (classpath scan)
        ↓
   MetadataResolver (reflection)
        ↓
   Generator (Repository/Service/Controller)
        ↓
   BeanDefinitionRegistry
```

## Composants Clés

### 1. EntityScanner

Scanne le classpath pour trouver les entités avec `@AutoApi` :

```java
@Component
public class EntityScanner {
    private final ConcurrentHashMap<String, List<Class<?>>> cache;
    
    public List<Class<?>> scan(String... basePackages) {
        // Uses ClassPathScanningCandidateComponentProvider
    }
}
```

### 2. MetadataResolver

Extrait les métadonnées via reflection :

```java
public EntityMetadata resolve(Class<?> entityClass) {
    // Walks inheritance hierarchy
    // Extracts ID, fields, relations, validations
    return EntityMetadata.builder()
        .entityClass(entityClass)
        .idType(idType)
        .fields(fieldMetadata)
        .build();
}
```

### 3. Generators

Créent les BeanDefinitions :

- **RepositoryGenerator** → `JpaRepository<T, ID>`
- **ServiceGenerator** → `GenericCrudService<T, ID>`
- **ControllerGenerator** → `GenericCrudController<T, ID>`

### 4. AutoApiRepositoryRegistrar

Orchestre tout le processus :

```java
@Override
public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
    List<Class<?>> entities = entityScanner.scan(basePackages);
    
    for (Class<?> entity : entities) {
        EntityMetadata metadata = metadataResolver.resolve(entity);
        
        registry.registerBeanDefinition(
            repositoryName,
            repositoryGenerator.generate(metadata)
        );
        // ... service, controller
    }
}
```

## Design Patterns

- **Factory Pattern** : Bean generation
- **Registry Pattern** : Bean registration
- **Metadata Pattern** : Immutable metadata records
- **Template Method** : Generic processing flow

## Performance

- **Caching** : Entity metadata cached (ConcurrentHashMap)
- **Lazy Loading** : N+1 prevention with fetch joins
- **Pagination** : Optimized COUNT queries

## Voir Aussi

- [Custom Endpoints](custom-endpoints.md)
- [Performance](performance.md)
