# SpringFlow - Liste Complète des Tâches

## 🏗️ PHASE 1 - MVP (Semaines 1-10)

---

### 📦 Module 1: Project Setup & Architecture (Semaine 1)

#### 1.1 Structure Multi-Module
- [x] Créer parent POM Maven
- [x] Configurer module `springflow-annotations`
- [x] Configurer module `springflow-core`
- [x] Configurer module `springflow-starter`
- [x] Configurer module `springflow-demo`
- [x] Configurer dependency management
- [x] Setup Java 17+ compilation
- [x] Setup Kotlin support

#### 1.2 Configuration Maven/Gradle
- [x] Configurer Spring Boot BOM (3.2+)
- [x] Ajouter Spring Data JPA dependencies
- [x] Ajouter SpringDoc OpenAPI dependencies
- [x] Ajouter validation dependencies (JSR-380)
- [x] Ajouter Lombok (optionnel)
- [x] Configurer Maven plugins (compiler, surefire)
- [x] Configurer profiles (dev, prod)

#### 1.3 CI/CD Setup
- [x] Créer GitHub Actions workflow
- [x] Configurer build automatique
- [x] Configurer tests automatiques
- [x] Setup SonarQube analysis
- [x] Configurer code coverage (JaCoCo)
- [ ] Setup quality gates
- [ ] Configurer artifact publication

#### 1.4 Documentation Infrastructure
- [x] Créer structure docs/
- [ ] Setup MkDocs ou similaire
- [ ] Configurer GitHub Pages
- [x] Template README.md
- [x] Template CONTRIBUTING.md
- [x] Template LICENSE

---

### 🏷️ Module 2: Core Annotations (Semaine 2)

#### 2.1 @AutoApi Annotation
- [ ] Créer interface `@AutoApi`
- [ ] Ajouter paramètre `path`
- [ ] Ajouter paramètre `expose` (enum)
- [ ] Ajouter paramètre `security`
- [ ] Ajouter paramètre `pagination`
- [ ] Ajouter paramètre `sorting`
- [ ] Ajouter paramètre `description`
- [ ] Écrire Javadoc complet

#### 2.2 @Filterable Annotation
- [ ] Créer interface `@Filterable`
- [ ] Ajouter paramètre `types` (FilterType[])
- [ ] Ajouter paramètre `paramName`
- [ ] Ajouter paramètre `description`
- [ ] Écrire Javadoc complet

#### 2.3 Annotations Complémentaires
- [ ] Créer `@Hidden` (exclure du DTO)
- [ ] Créer `@ReadOnly` (lecture seule)
- [ ] Créer `@SoftDelete` (Phase 2)
- [ ] Créer `@Auditable` (Phase 2)

#### 2.4 Enums
- [ ] Créer enum `Expose` (ALL, CREATE_UPDATE, READ_ONLY)
- [ ] Créer enum `FilterType` (EQUALS, LIKE, RANGE, IN, GT, LT, etc.)
- [ ] Créer enum `SecurityLevel` (PUBLIC, AUTHENTICATED, ROLE_BASED)
- [ ] Documenter chaque enum value

#### 2.5 Tests
- [ ] Tests annotations présentes à runtime
- [ ] Tests valeurs par défaut
- [ ] Tests combinaisons de paramètres
- [ ] Documentation examples

---

### 🔍 Module 3: Entity Scanner (Semaine 3)

#### 3.1 EntityScanner Core
- [ ] Créer classe `EntityScanner`
- [ ] Implémenter scan du classpath
- [ ] Utiliser `ClassPathScanningCandidateComponentProvider`
- [ ] Filter pour `@Entity` + `@AutoApi`
- [ ] Support scan multi-packages
- [ ] Gestion des erreurs de scan

#### 3.2 Cache Management
- [ ] Implémenter cache des entités scannées
- [ ] Utiliser `ConcurrentHashMap`
- [ ] Stratégie de cache invalidation
- [ ] Configuration cache size limit
- [ ] Métriques de cache (hits/misses)

#### 3.3 Metadata Extraction Initial
- [ ] Extraire nom de classe
- [ ] Extraire nom de table (@Table)
- [ ] Extraire annotation @AutoApi
- [ ] Valider configuration annotations
- [ ] Logger entités trouvées

#### 3.4 Tests
- [ ] Test scan package simple
- [ ] Test scan packages multiples
- [ ] Test scan avec sous-packages
- [ ] Test entités sans @AutoApi
- [ ] Test cache fonctionnel
- [ ] Test performance (>100 entités)

---

### 📊 Module 4: Metadata Resolver (Semaine 3-4)

#### 4.1 EntityMetadata Model
- [ ] Créer classe `EntityMetadata`
- [ ] Propriété `entityClass`
- [ ] Propriété `idType`
- [ ] Propriété `entityName`
- [ ] Propriété `tableName`
- [ ] Propriété `fields` (List<FieldMetadata>)
- [ ] Propriété `autoApiConfig`
- [ ] Méthodes helper (getIdField, getFieldByName, etc.)

#### 4.2 FieldMetadata Model
- [ ] Créer classe `FieldMetadata`
- [ ] Propriété `field` (Field)
- [ ] Propriété `name`
- [ ] Propriété `type`
- [ ] Propriété `nullable`
- [ ] Propriété `hidden`
- [ ] Propriété `readOnly`
- [ ] Propriété `validations` (List<Annotation>)
- [ ] Propriété `filterConfig`

#### 4.3 ID Resolution
- [ ] Détecter champ @Id
- [ ] Extraire type de l'ID
- [ ] Support @EmbeddedId
- [ ] Support @IdClass
- [ ] Valider présence de l'ID
- [ ] Détection generation strategy

#### 4.4 Validation Extraction
- [ ] Scanner annotations JSR-380
- [ ] Extraire @NotNull, @NotBlank
- [ ] Extraire @Size, @Min, @Max
- [ ] Extraire @Email, @Pattern
- [ ] Extraire validations custom
- [ ] Stocker dans FieldMetadata

#### 4.5 Relations JPA
- [ ] Créer classe `RelationMetadata`
- [ ] Détecter @OneToMany
- [ ] Détecter @ManyToOne
- [ ] Détecter @ManyToMany
- [ ] Détecter @OneToOne
- [ ] Extraire fetch type (LAZY/EAGER)
- [ ] Extraire cascade options

#### 4.6 Field Analysis
- [ ] Analyser tous les champs de l'entité
- [ ] Exclure champs static
- [ ] Exclure champs transient
- [ ] Détection @Hidden annotation
- [ ] Détection @Filterable annotation
- [ ] Support héritage (@MappedSuperclass)

#### 4.7 Tests
- [ ] Test extraction ID simple
- [ ] Test extraction ID composite
- [ ] Test extraction validations
- [ ] Test extraction relations
- [ ] Test champs hidden
- [ ] Test héritage entités

---

### 🗄️ Module 5: Repository Generation (Semaine 4)

#### 5.1 RepositoryGenerator Core
- [ ] Créer classe `RepositoryGenerator`
- [ ] Méthode `generateRepository(EntityMetadata)`
- [ ] Créer `GenericBeanDefinition`
- [ ] Configurer target type `JpaRepository<T, ID>`
- [ ] Enregistrer dans `BeanDefinitionRegistry`

#### 5.2 JpaSpecificationExecutor Support
- [ ] Ajouter interface `JpaSpecificationExecutor<T>`
- [ ] Configuration pour filtres dynamiques
- [ ] Tests avec Specifications

#### 5.3 Custom Query Methods (Optionnel Phase 1)
- [ ] Support query methods personnalisées
- [ ] Parser nom de méthode (findByXxx)
- [ ] Génération automatique (futurs)

#### 5.4 Bean Registration
- [ ] Implémenter `BeanDefinitionRegistryPostProcessor`
- [ ] Enregistrement dynamique au démarrage
- [ ] Gestion des collisions de noms
- [ ] Logging des repositories créés

#### 5.5 Tests
- [ ] Test génération repository simple
- [ ] Test injection dans service
- [ ] Test méthodes JpaRepository
- [ ] Test avec JpaSpecificationExecutor
- [ ] Test intégration avec H2
- [ ] Test avec plusieurs entités

---

### 🔧 Module 6: Service Generation (Semaine 5)

#### 6.1 GenericCrudService Abstract Class
- [ ] Créer classe `GenericCrudService<T, ID>`
- [ ] Injection `JpaRepository<T, ID>`
- [ ] Méthode `findAll(Pageable, Specification)`
- [ ] Méthode `findById(ID)`
- [ ] Méthode `save(T)`
- [ ] Méthode `update(ID, T)`
- [ ] Méthode `deleteById(ID)`
- [ ] Méthode `existsById(ID)`

#### 6.2 Service Concrete Implementation
- [ ] Générer classe concrète par entité
- [ ] Nommage: `<Entity>Service`
- [ ] Injection automatique du repository
- [ ] Enregistrement comme bean Spring

#### 6.3 Transaction Management
- [ ] Annoter méthodes avec `@Transactional`
- [ ] ReadOnly pour queries
- [ ] Isolation level configuration
- [ ] Propagation configuration

#### 6.4 Exception Handling
- [ ] Créer `EntityNotFoundException`
- [ ] Créer `DuplicateEntityException`
- [ ] Créer `ValidationException`
- [ ] Exception handler global
- [ ] Logging des erreurs

#### 6.5 Business Logic Hooks (Optionnel)
- [ ] Hook `beforeCreate(T)`
- [ ] Hook `afterCreate(T)`
- [ ] Hook `beforeUpdate(T)`
- [ ] Hook `afterUpdate(T)`
- [ ] Hook `beforeDelete(ID)`
- [ ] Hook `afterDelete(ID)`

#### 6.6 Tests
- [ ] Test CRUD operations
- [ ] Test transactions
- [ ] Test exceptions
- [ ] Test hooks (si implémentés)
- [ ] Test intégration repository
- [ ] Test avec données complexes

---

### 🌐 Module 7: Generic CRUD Controller (Semaine 6)

#### 7.1 GenericCrudController Abstract Class
- [ ] Créer classe `GenericCrudController<T, ID>`
- [ ] Injection `GenericCrudService<T, ID>`
- [ ] Injection `DtoMapper<T>`
- [ ] Configuration base path

#### 7.2 GET Endpoints
- [ ] `@GetMapping` - findAll avec pagination
- [ ] `@GetMapping("/{id}")` - findById
- [ ] Response `ResponseEntity<Page<OutputDTO>>`
- [ ] Response `ResponseEntity<OutputDTO>`
- [ ] Status codes appropriés (200, 404)

#### 7.3 POST Endpoint
- [ ] `@PostMapping` - create
- [ ] Request body `@Valid InputDTO`
- [ ] Conversion DTO → Entity
- [ ] Response `ResponseEntity<OutputDTO>`
- [ ] Status code 201 CREATED
- [ ] Header Location avec URI

#### 7.4 PUT Endpoint
- [ ] `@PutMapping("/{id}")` - update complet
- [ ] Request body `@Valid InputDTO`
- [ ] Vérifier existence entité
- [ ] Mise à jour complète
- [ ] Response `ResponseEntity<OutputDTO>`
- [ ] Status code 200 OK

#### 7.5 PATCH Endpoint
- [ ] `@PatchMapping("/{id}")` - update partiel
- [ ] Request body `Map<String, Object>`
- [ ] Validation des champs
- [ ] Mise à jour sélective
- [ ] Utiliser reflection
- [ ] Status code 200 OK

#### 7.6 DELETE Endpoint
- [ ] `@DeleteMapping("/{id}")` - delete
- [ ] Vérifier existence
- [ ] Response `ResponseEntity<Void>`
- [ ] Status code 204 NO CONTENT

#### 7.7 Error Handling
- [ ] `@ExceptionHandler` pour toutes exceptions
- [ ] Format erreur standardisé
- [ ] Status codes appropriés
- [ ] Messages i18n (Phase 3)

#### 7.8 Tests
- [ ] Test GET all avec MockMvc
- [ ] Test GET by ID (200, 404)
- [ ] Test POST (201, 400)
- [ ] Test PUT (200, 404, 400)
- [ ] Test PATCH (200, 404)
- [ ] Test DELETE (204, 404)
- [ ] Test intégration complète

---

### 📄 Module 8: Pagination & Sorting (Semaine 7)

#### 8.1 Pageable Configuration
- [ ] Configurer `PageableHandlerMethodArgumentResolver`
- [ ] Paramètre `page` (default 0)
- [ ] Paramètre `size` (default 20)
- [ ] Max page size (100)
- [ ] Configuration personnalisable

#### 8.2 Sorting Configuration
- [ ] Support paramètre `sort`
- [ ] Format: `sort=field,direction`
- [ ] Multi-field sorting
- [ ] Validation des champs sortables
- [ ] Default sort configuration

#### 8.3 Page Response
- [ ] Utiliser Spring Data `Page<T>`
- [ ] Metadata: totalElements, totalPages
- [ ] Metadata: size, number
- [ ] Content: liste éléments
- [ ] Links HATEOAS (optionnel Phase 2)

#### 8.4 Custom Pageable
- [ ] Créer `@PageableDefault` custom
- [ ] Configuration par entité
- [ ] Override via annotation

#### 8.5 Tests
- [ ] Test pagination simple
- [ ] Test changement de page
- [ ] Test changement de size
- [ ] Test sorting simple field
- [ ] Test multi-field sorting
- [ ] Test limites (max size)

---

### 🔌 Module 9: Controller Registration (Semaine 7)

#### 9.1 ImportBeanDefinitionRegistrar
- [ ] Créer `SpringFlowBeanDefinitionRegistrar`
- [ ] Implémenter interface `ImportBeanDefinitionRegistrar`
- [ ] Override `registerBeanDefinitions()`
- [ ] Scanner les entités
- [ ] Boucle sur chaque entité

#### 9.2 Dynamic Controller Generation
- [ ] Créer instance concrète de `GenericCrudController`
- [ ] Configuration path depuis `@AutoApi`
- [ ] Génération bean name unique
- [ ] Enregistrement `BeanDefinition`
- [ ] Configuration request mapping

#### 9.3 Request Mapping Configuration
- [ ] Base path `/api` (configurable)
- [ ] Entity path depuis annotation
- [ ] Support versioning (v1, v2)
- [ ] Configuration préfixes

#### 9.4 Controller Customization
- [ ] Support merge generated + custom controller
- [ ] Détection custom controller existant
- [ ] Override endpoints si custom présent
- [ ] Logging controllers enregistrés

#### 9.5 Tests
- [ ] Test registration simple entité
- [ ] Test registration multiples entités
- [ ] Test custom path
- [ ] Test collision noms
- [ ] Test endpoints accessibles
- [ ] Test intégration complète

---

### ⚙️ Module 10: Spring Boot Auto Configuration (Semaine 8)

#### 10.1 SpringFlowAutoConfiguration
- [ ] Créer classe `SpringFlowAutoConfiguration`
- [ ] Annotation `@Configuration`
- [ ] Annotation `@ConditionalOnClass`
- [ ] Import `SpringFlowBeanDefinitionRegistrar`
- [ ] Beans conditionnels

#### 10.2 Configuration Properties
- [ ] Créer `SpringFlowProperties`
- [ ] Annotation `@ConfigurationProperties("springflow")`
- [ ] Propriété `enabled` (default true)
- [ ] Propriété `basePath` (default "/api")
- [ ] Propriété `basePackages` (String[])
- [ ] Propriété `pagination.*`
- [ ] Propriété `swagger.*`

#### 10.3 Enable Annotation
- [ ] Créer `@EnableSpringFlow`
- [ ] Paramètre `basePackages`
- [ ] Paramètre `value` (alias)
- [ ] Import configuration

#### 10.4 spring.factories
- [ ] Créer `META-INF/spring.factories`
- [ ] Enregistrer `SpringFlowAutoConfiguration`
- [ ] Configuration Spring Boot 3 (spring/spring.factories)

#### 10.5 Conditional Beans
- [ ] Bean `EntityScanner` si enabled
- [ ] Bean `RepositoryGenerator` si enabled
- [ ] Bean `ServiceGenerator` si enabled
- [ ] Bean `ControllerGenerator` si enabled

#### 10.6 application.yml Example
- [ ] Créer exemple configuration
- [ ] Documentation chaque propriété
- [ ] Valeurs par défaut
- [ ] Exemples avancés

#### 10.7 Tests
- [ ] Test auto-configuration chargée
- [ ] Test avec properties custom
- [ ] Test disabled (enabled: false)
- [ ] Test beans créés
- [ ] Test sans configuration (defaults)

---

### 📚 Module 11: OpenAPI/Swagger Integration (Semaine 8)

#### 11.1 SpringDoc Configuration
- [ ] Ajouter dependency `springdoc-openapi-starter-webmvc-ui`
- [ ] Créer `OpenApiConfiguration`
- [ ] Configurer info API (title, version, description)
- [ ] Configurer servers
- [ ] Configurer security schemes (Phase 2)

#### 11.2 Schema Generation
- [ ] Auto-generate schemas pour DTOs
- [ ] Support validation constraints
- [ ] Examples values
- [ ] Description depuis Javadoc
- [ ] Support enums

#### 11.3 Endpoint Documentation
- [ ] Auto-document tous les endpoints
- [ ] Tags par entité
- [ ] Operation summary & description
- [ ] Request body schema
- [ ] Response schemas (200, 400, 404, etc.)
- [ ] Query parameters (page, size, sort)

#### 11.4 Customization
- [ ] Support `@Operation` custom
- [ ] Support `@ApiResponse` custom
- [ ] Support `@Parameter` custom
- [ ] Description depuis `@AutoApi`

#### 11.5 Swagger UI
- [ ] Activer Swagger UI
- [ ] URL: `/swagger-ui.html`
- [ ] Configuration layout
- [ ] Try it out enabled
- [ ] Configuration personnalisable

#### 11.6 Tests
- [ ] Test génération OpenAPI spec
- [ ] Test schemas présents
- [ ] Test endpoints documentés
- [ ] Test Swagger UI accessible
- [ ] Validation spec OpenAPI 3.0

---

### 🗂️ Module 12: DTO Generation (Semaine 9)

#### 12.1 DtoGenerator Core
- [ ] Créer classe `DtoGenerator`
- [ ] Méthode `generateInputDto(EntityMetadata)`
- [ ] Méthode `generateOutputDto(EntityMetadata)`
- [ ] Nommage: `<Entity>InputDTO`, `<Entity>OutputDTO`

#### 12.2 InputDTO Generation
- [ ] Inclure tous les champs sauf ID
- [ ] Exclure champs `@Hidden`
- [ ] Inclure champs `@Filterable`
- [ ] Copier validation annotations
- [ ] Support relations (IDs uniquement)
- [ ] Générer constructeurs
- [ ] Générer getters/setters

#### 12.3 OutputDTO Generation
- [ ] Inclure tous les champs avec ID
- [ ] Exclure champs sensibles
- [ ] Support relations (nested ou IDs)
- [ ] Configuration lazy/eager relations
- [ ] Timestamps (createdAt, updatedAt)
- [ ] Générer constructeurs
- [ ] Générer getters/setters

#### 12.4 DTO Mapping
- [ ] Créer interface `DtoMapper<T>`
- [ ] Méthode `T toEntity(InputDTO)`
- [ ] Méthode `OutputDTO toDto(T)`
- [ ] Méthode `void updateEntity(T, InputDTO)`
- [ ] Méthode `List<OutputDTO> toDto(List<T>)`

#### 12.5 Mapper Implementation
- [ ] Implémentation manuelle (reflection)
- [ ] Support MapStruct (Phase 2)
- [ ] Gestion null values
- [ ] Gestion collections
- [ ] Gestion relations

#### 12.6 Advanced Features
- [ ] Support JsonView (Phase 2)
- [ ] Support JsonIgnore
- [ ] Custom serialization
- [ ] Date formatting

#### 12.7 Tests
- [ ] Test génération InputDTO
- [ ] Test génération OutputDTO
- [ ] Test mapping entity → DTO
- [ ] Test mapping DTO → entity
- [ ] Test validation sur InputDTO
- [ ] Test champs hidden exclus

---

### ✅ Module 13: Validation (Semaine 9)

#### 13.1 JSR-380 Support
- [ ] Support `@NotNull`
- [ ] Support `@NotBlank`
- [ ] Support `@NotEmpty`
- [ ] Support `@Size`
- [ ] Support `@Min` / `@Max`
- [ ] Support `@Email`
- [ ] Support `@Pattern`
- [ ] Support `@Valid` (nested)

#### 13.2 Validation Error Handling
- [ ] Créer `ValidationErrorResponse`
- [ ] Champs: timestamp, status, errors
- [ ] Créer `FieldError` (field, message, rejectedValue)
- [ ] `@ExceptionHandler(MethodArgumentNotValidException)`
- [ ] Format JSON standardisé

#### 13.3 Custom Validators
- [ ] Support création validator custom
- [ ] Annotation `@Constraint`
- [ ] Implémentation `ConstraintValidator`
- [ ] Exemple: `@UniqueEmail`

#### 13.4 Validation Groups
- [ ] Support validation groups
- [ ] Groupes: Create, Update
- [ ] Configuration par endpoint

#### 13.5 i18n Messages (Phase 3)
- [ ] Messages d'erreur internationalisés
- [ ] Fichier messages.properties
- [ ] Support locales

#### 13.6 Tests
- [ ] Test validation @NotBlank
- [ ] Test validation @Email
- [ ] Test validation @Size
- [ ] Test validation @Min/@Max
- [ ] Test nested validation
- [ ] Test error response format
- [ ] Test custom validators

---

### 🔷 Module 14: Kotlin Support (Semaine 10)

#### 14.1 Kotlin Configuration
- [ ] Ajouter kotlin-maven-plugin
- [ ] Ajouter kotlin-stdlib
- [ ] Ajouter kotlin-reflect
- [ ] Compiler avant Java
- [ ] Support Kotlin 1.9+

#### 14.2 Data Class Support
- [ ] Test avec data class simple
- [ ] Test properties val/var
- [ ] Test nullable types
- [ ] Test default values
- [ ] Test copy method

#### 14.3 Annotation Support
- [ ] Support @field:NotBlank
- [ ] Support @get:NotBlank
- [ ] Kotlin annotation targets
- [ ] Test toutes annotations

#### 14.4 Null Safety
- [ ] Gestion nullable types (?)
- [ ] Conversion Entity ↔ DTO
- [ ] Validation nullable fields
- [ ] Default values null

#### 14.5 Extension Functions (Optionnel)
- [ ] Extensions pour repositories
- [ ] Extensions pour services
- [ ] DSL configuration

#### 14.6 Coroutines (Phase 2)
- [ ] Support suspend functions
- [ ] Reactive repositories
- [ ] Async operations

#### 14.7 Tests
- [ ] Demo app Kotlin complète
- [ ] Test data class entité
- [ ] Test nullable types
- [ ] Test validation Kotlin
- [ ] Test génération repositories
- [ ] Test génération controllers

---

### 📖 Module 15: Demo Application & Documentation (Semaine 10)

#### 15.1 Demo Java App
- [ ] Créer projet Spring Boot
- [ ] Ajouter springflow-starter dependency
- [ ] Annotation `@EnableSpringFlow`
- [ ] Créer 3-5 entités exemple
- [ ] Configuration application.yml
- [ ] Tests d'intégration complets

#### 15.2 Demo Kotlin App
- [ ] Créer projet Spring Boot Kotlin
- [ ] Ajouter springflow-starter dependency
- [ ] Annotation `@EnableSpringFlow`
- [ ] Créer 3-5 data classes exemple
- [ ] Configuration application.yml
- [ ] Tests d'intégration complets

#### 15.3 Example Entities
- [ ] User (simple)
- [ ] Product (avec validations)
- [ ] Order (avec relations)
- [ ] Category (hiérarchie)
- [ ] Article (avec filtres)

#### 15.4 README.md Principal
- [ ] Badges (build, coverage, version)
- [ ] Description projet
- [ ] Features principales
- [ ] Quick start (5 min)
- [ ] Installation instructions
- [ ] Example code
- [ ] Link vers docs complètes

#### 15.5 Getting Started Guide
- [ ] Prérequis (Java, Spring Boot)
- [ ] Ajout de la dépendance
- [ ] Configuration minimale
- [ ] Première entité
- [ ] Test de l'API
- [ ] Next steps

#### 15.6 Configuration Guide
- [ ] Toutes les properties
- [ ] application.yml complet
- [ ] Exemples de configuration
- [ ] Best practices
- [ ] Troubleshooting

#### 15.7 API Reference
- [ ] Annotations documentation
- [ ] Configuration properties
- [ ] Generated endpoints
- [ ] Response formats
- [ ] Error codes
- [ ] Examples

#### 15.8 Advanced Guide
- [ ] Custom endpoints
- [ ] Override defaults
- [ ] Integration avec Security
- [ ] Performance tuning
- [ ] Production deployment

#### 15.9 Examples Repository
- [ ] E-commerce example
- [ ] Blog example
- [ ] Multi-tenant example
- [ ] Microservices example

#### 15.10 Troubleshooting
- [ ] FAQ
- [ ] Common errors
- [ ] Debug tips
- [ ] Support channels

---

## 🚀 PHASE 2 - ADVANCED FEATURES (Semaines 11-18)

---

### 🔎 Module 16: Dynamic Filters (Semaines 11-12)

#### 16.1 FilterResolver Core
- [ ] Créer classe `FilterResolver`
- [ ] Méthode `buildSpecification(Map<String, String>, EntityMetadata)`
- [ ] Parser query parameters
- [ ] Mapper vers Specifications JPA

#### 16.2 Filter Types Implementation
- [ ] EQUALS: `?name=John`
- [ ] LIKE: `?name_like=Joh`
- [ ] GREATER_THAN: `?age_gt=18`
- [ ] LESS_THAN: `?age_lt=65`
- [ ] GREATER_THAN_OR_EQUAL: `?age_gte=18`
- [ ] LESS_THAN_OR_EQUAL: `?age_lte=65`
- [ ] IN: `?status_in=ACTIVE,PENDING`
- [ ] BETWEEN: `?age_between=18,65`
- [ ] IS_NULL: `?email_null=true`
- [ ] IS_NOT_NULL: `?email_null=false`

#### 16.3 Filter Configuration
- [ ] Lecture `@Filterable` sur champs
- [ ] Validation champs filterables
- [ ] Type de filtre par défaut
- [ ] Custom param names
- [ ] Case sensitivity configuration

#### 16.4 Complex Filters
- [ ] AND conditions
- [ ] OR conditions (Phase 3)
- [ ] Nested filters (Phase 3)
- [ ] Filter groups

#### 16.5 JPA Specifications
- [ ] Builder pattern pour Specifications
- [ ] Composition avec `.and()` `.or()`
- [ ] Performance optimization
- [ ] Index hints

#### 16.6 Tests
- [ ] Test chaque filter type
- [ ] Test combinaison de filtres
- [ ] Test validation
- [ ] Test performance
- [ ] Test avec pagination
- [ ] Test intégration complète

---

### 🔐 Module 17: Security Integration (Semaine 13)

#### 17.1 Security Configuration
- [ ] Paramètre `security` dans `@AutoApi`
- [ ] Sous-annotation `@Security`
- [ ] Propriété `enabled` (boolean)
- [ ] Propriété `roles` (String[])
- [ ] Propriété `authorities` (String[])

#### 17.2 Spring Security Integration
- [ ] Configuration `SecurityFilterChain`
- [ ] Endpoints publics par défaut
- [ ] Endpoints sécurisés si configuré
- [ ] Support JWT (optionnel)
- [ ] Support OAuth2 (optionnel)

#### 17.3 Method Security
- [ ] Générer `@PreAuthorize` sur méthodes
- [ ] Format: `@PreAuthorize("hasAnyRole('ADMIN', 'USER')")`
- [ ] Support expressions SpEL
- [ ] Custom security expressions

#### 17.4 Endpoint Level Security
- [ ] Sécurité différente par endpoint
- [ ] GET public, POST/PUT/DELETE sécurisés
- [ ] Configuration granulaire
- [ ] Override dans custom controller

#### 17.5 User Context
- [ ] Accès à `SecurityContext`
- [ ] Injection `Authentication`
- [ ] Récupération user courant
- [ ] Audit avec user info

#### 17.6 Tests
- [ ] Test endpoints publics
- [ ] Test endpoints sécurisés (401)
- [ ] Test avec role valide (200)
- [ ] Test avec role invalide (403)
- [ ] Test JWT tokens
- [ ] Test intégration complète

---

### 🗺️ Module 18: Advanced DTO Mapping (Semaine 14)

#### 18.1 Relations Mapping
- [ ] OneToMany mapping
- [ ] ManyToOne mapping
- [ ] ManyToMany mapping
- [ ] OneToOne mapping
- [ ] Nested DTOs
- [ ] IDs only mode

#### 18.2 Lazy Loading DTOs
- [ ] Configuration lazy/eager per relation
- [ ] Éviter N+1 queries
- [ ] EntityGraph usage
- [ ] Fetch joins

#### 18.3 MapStruct Integration
- [ ] Ajouter MapStruct dependency
- [ ] Générer mappers avec MapStruct
- [ ] Annotations `@Mapper`
- [ ] Custom mapping methods
- [ ] Performance vs reflection

#### 18.4 Circular References
- [ ] Détection circular refs
- [ ] JsonIgnore strategy
- [ ] Max depth configuration
- [ ] DTO projections

#### 18.5 Projection Support
- [ ] Interface projections
- [ ] Class projections
- [ ] Dynamic projections
- [ ] JPQL queries

#### 18.6 Tests
- [ ] Test mapping relations
- [ ] Test lazy loading
- [ ] Test circular refs
- [ ] Test MapStruct
- [ ] Test projections
- [ ] Test performance

---

### 🗑️ Module 19: Soft Delete (Semaine 15)

#### 19.1 @SoftDelete Annotation
- [ ] Créer annotation `@SoftDelete`
- [ ] Paramètre `deletedField` (default "deleted")
- [ ] Paramètre `deletedAtField` (default "deletedAt")
- [ ] Documentation

#### 19.2 Entity Enhancement
- [ ] Ajout automatique champ `deleted`
- [ ] Ajout automatique champ `deletedAt`
- [ ] Type Boolean pour deleted
- [ ] Type LocalDateTime pour deletedAt

#### 19.3 Repository Filters
- [ ] Filter automatique des deleted
- [ ] Override `findAll()` avec filter
- [ ] Override `findById()` avec filter
- [ ] Méthode `findAllIncludingDeleted()`
- [ ] Méthode `findDeletedOnly()`

#### 19.4 Delete Operation
- [ ] Override `deleteById()` → soft delete
- [ ] Set `deleted = true`
- [ ] Set `deletedAt = now()`
- [ ] Méthode `hardDelete()` pour vraie suppression

#### 19.5 Restore Operation
- [ ] Endpoint `POST /{id}/restore`
- [ ] Set `deleted = false`
- [ ] Set `deletedAt = null`
- [ ] Validation entité existe

#### 19.6 Query Parameter
- [ ] `?includeDeleted=true` pour inclure deleted
- [ ] `?deletedOnly=true` pour seulement deleted
- [ ] Configuration par défaut

#### 19.7 Tests
- [ ] Test soft delete
- [ ] Test restore
- [ ] Test queries avec filtre
- [ ] Test includeDeleted
- [ ] Test hard delete
- [ ] Test intégration complète

---

### 📊 Module 20: Audit Trail (Semaine 16)

#### 20.1 @Auditable Annotation
- [ ] Créer annotation `@Auditable`
- [ ] Configuration champs auto
- [ ] Support Spring Data JPA Auditing

#### 20.2 Audit Fields
- [ ] `createdAt` (LocalDateTime)
- [ ] `updatedAt` (LocalDateTime)
- [ ] `createdBy` (String)
- [ ] `updatedBy` (String)
- [ ] `version` (Long) pour optimistic locking

#### 20.3 Spring Data Auditing
- [ ] Activer `@EnableJpaAuditing`
- [ ] Annotations `@CreatedDate`
- [ ] Annotations `@LastModifiedDate`
- [ ] Annotations `@CreatedBy`
- [ ] Annotations `@LastModifiedBy`

#### 20.4 AuditorAware
- [ ] Implémenter `AuditorAware<String>`
- [ ] Récupération user depuis SecurityContext
- [ ] Configuration bean

#### 20.5 Tests
- [ ] Test createdAt set on create
- [ ] Test updatedAt set on update
- [ ] Test createdBy avec user
- [ ] Test updatedBy avec user
- [ ] Test version incrementée

---

### 🎯 Module 21: Custom Endpoints (Semaine 17)

#### 21.1 Custom Controller Support
- [ ] Détection controller custom existant
- [ ] Merge generated + custom methods
- [ ] Priorité aux méthodes custom
- [ ] Éviter duplications

#### 21.2 Override Defaults
- [ ] Override méthode findAll()
- [ ] Override méthode findById()
- [ ] Override méthode create()
- [ ] Override méthode update()
- [ ] Override méthode delete()
- [ ] Custom business logic

#### 21.3 Additional Endpoints
- [ ] Ajout endpoints custom dans controller
- [ ] Exemple: `GET /users/active`
- [ ] Exemple: `POST /users/{id}/activate`
- [ ] Documentation automatique

#### 21.4 Tests
- [ ] Test custom endpoint appelé
- [ ] Test override default
- [ ] Test mix generated + custom
- [ ] Test documentation

---

## 🌟 PHASE 3 - EXTENDED ECOSYSTEM (Semaines 18-26)

---

### 🎨 Module 22: GraphQL Support (Semaines 18-20)

#### 22.1 Spring GraphQL Integration
- [ ] Ajouter dependency spring-boot-starter-graphql
- [ ] Configuration GraphQL
- [ ] Auto-configuration

#### 22.2 Schema Generation
- [ ] Générer types GraphQL depuis entités
- [ ] Générer queries
- [ ] Générer mutations
- [ ] Générer input types

#### 22.3 Query Resolvers
- [ ] Query `products(page, size): ProductPage`
- [ ] Query `product(id): Product`
- [ ] Pagination GraphQL
- [ ] Filtres GraphQL

#### 22.4 Mutation Resolvers
- [ ] Mutation `createProduct(input): Product`
- [ ] Mutation `updateProduct(id, input): Product`
- [ ] Mutation `deleteProduct(id): Boolean`

#### 22.5 DataFetchers
- [ ] Auto-generate DataFetchers
- [ ] Relations loading
- [ ] N+1 problem solution
- [ ] DataLoader support

#### 22.6 Tests
- [ ] Test GraphQL queries
- [ ] Test GraphQL mutations
- [ ] Test filtres
- [ ] Test pagination
- [ ] Test relations

---

### 💻 Module 23: Admin UI (Semaines 21-23)

#### 23.1 UI Project Setup
- [ ] Créer projet React + TypeScript
- [ ] Setup Vite ou CRA
- [ ] Ajouter Material UI ou Ant Design
- [ ] Ajouter React Query
- [ ] Ajouter React Router

#### 23.2 API Client Generation
- [ ] Générer client depuis OpenAPI spec
- [ ] Utiliser openapi-generator
- [ ] TypeScript types
- [ ] React hooks

#### 23.3 Entity List View
- [ ] Table avec données
- [ ] Pagination UI
- [ ] Sorting UI
- [ ] Filtres UI
- [ ] Search bar
- [ ] Actions (edit, delete)

#### 23.4 Entity Create Form
- [ ] Form auto-généré depuis schema
- [ ] Validation frontend
- [ ] Field types appropriés
- [ ] Submit avec API

#### 23.5 Entity Edit Form
- [ ] Load existing data
- [ ] Form pré-rempli
- [ ] Validation frontend
- [ ] Update avec API

#### 23.6 Entity Delete
- [ ] Confirmation modal
- [ ] Delete avec API
- [ ] Refresh list

#### 23.7 Relation Handling
- [ ] Select pour ManyToOne
- [ ] Multi-select pour ManyToMany
- [ ] Nested entities
- [ ] Lazy loading

#### 23.8 Deployment
- [ ] Build production
- [ ] Nginx configuration
- [ ] Docker image
- [ ] Documentation deployment

---

### 🛠️ Module 24: CLI Tool (Semaine 24)

#### 24.1 CLI Framework
- [ ] Setup Picocli
- [ ] Main command class
- [ ] Subcommands structure

#### 24.2 Init Command
- [ ] `springflow init <project-name>`
- [ ] Générer structure Maven/Gradle
- [ ] Ajouter springflow dependency
- [ ] Générer application.yml
- [ ] Générer entité exemple

#### 24.3 Generate Entity Command
- [ ] `springflow generate entity <name>`
- [ ] Paramètres: fields
- [ ] Format: `name:string email:string`
- [ ] Types supportés (string, int, date, etc.)
- [ ] Génération classe Java/Kotlin

#### 24.4 Generate Module Command
- [ ] `springflow generate module <name>`
- [ ] Générer package complet
- [ ] Plusieurs entités liées
- [ ] Configuration

#### 24.5 Templates
- [ ] Templates Mustache/Freemarker
- [ ] Template entité
- [ ] Template configuration
- [ ] Template tests

#### 24.6 Tests
- [ ] Test init command
- [ ] Test generate entity
- [ ] Test generate module
- [ ] Test validation inputs

---

### 🗄️ Module 25: Multi-DB Support (Semaine 25)

#### 25.1 MongoDB Support
- [ ] Ajouter spring-boot-starter-data-mongodb
- [ ] `MongoRepository` au lieu de JpaRepository
- [ ] Adapter scanner
- [ ] Adapter metadata
- [ ] Tests avec MongoDB

#### 25.2 PostgreSQL Advanced
- [ ] JSONB support
- [ ] Array types
- [ ] Full-text search
- [ ] Custom types

#### 25.3 DB-Agnostic Layer
- [ ] Interface commune repositories
- [ ] Factory pattern
- [ ] Configuration auto-detection
- [ ] Tests multi-DB

---

### 📈 Module 26: Monitoring & Metrics (Semaine 26)

#### 26.1 Spring Actuator
- [ ] Intégrer Spring Boot Actuator
- [ ] Endpoints health, metrics
- [ ] Custom metrics
- [ ] Prometheus export

#### 26.2 Custom Metrics
- [ ] Compteur requests par entité
- [ ] Latence endpoints
- [ ] Erreurs par type
- [ ] Dashboard Grafana

---

## 📝 DOCUMENTATION & TESTING

### Documentation Continue
- [ ] Javadoc pour toutes classes publiques
- [ ] README dans chaque module
- [ ] Changelog (CHANGELOG.md)
- [ ] Migration guides
- [ ] Blog posts

### Testing Continue
- [ ] Unit tests (>80% coverage)
- [ ] Integration tests
- [ ] E2E tests
- [ ] Performance tests
- [ ] Security tests
- [ ] Mutation testing (PIT)

---

## 🚢 RELEASE & DEPLOYMENT

### Maven Central Publication
- [ ] Configuration POM pour publication
- [ ] GPG signing
- [ ] Sonatype account
- [ ] Staging repository
- [ ] Release process

### GitHub Releases
- [ ] Release notes
- [ ] Binaries
- [ ] Changelog
- [ ] Migration guide

### Docker Images
- [ ] Image demo app
- [ ] Image admin UI
- [ ] Docker Compose example
- [ ] Kubernetes manifests

---

## 📊 ESTIMATION GLOBALE

### Phase 1 (MVP)
- **Modules**: 15
- **Tâches**: ~200
- **Durée**: 10 semaines
- **Équipe**: 2-3 développeurs

### Phase 2 (Advanced)
- **Modules**: 6
- **Tâches**: ~80
- **Durée**: 7 semaines
- **Équipe**: 2 développeurs

### Phase 3 (Extended)
- **Modules**: 5
- **Tâches**: ~100
- **Durée**: 8 semaines
- **Équipe**: 2-3 développeurs

### TOTAL
- **Modules**: 26
- **Tâches**: ~380
- **Durée**: 25 semaines (~6 mois)
- **Effort**: ~3000 heures

---

**Dernière mise à jour**: 2025-12-18
**Version**: 1.0
