# Your First Project

Ce guide vous accompagne pas à pas dans la création d'un projet complet avec SpringFlow. Vous allez créer une **API de Blog** avec des auteurs et des articles en **moins de 15 minutes**.

## :material-target: Ce que vous allez construire

Une API REST complète avec:

- :material-account: **Authors** - Gestion des auteurs
- :material-post: **BlogPosts** - Gestion des articles de blog
- :material-link-variant: **Relation** - Un auteur peut avoir plusieurs articles
- :material-api: **Endpoints CRUD** - Automatiquement générés
- :material-file-document: **Swagger UI** - Documentation interactive
- :material-check-all: **Validation** - Règles métier intégrées

## :material-numeric-1-box: Créer le Projet Spring Boot

### Option 1: Spring Initializr (Recommandé)

1. Allez sur [start.spring.io](https://start.spring.io)

2. Configurez votre projet:
   - **Project**: Maven
   - **Language**: Java
   - **Spring Boot**: 3.2.1+
   - **Java**: 17

3. Remplissez les métadonnées:
   - **Group**: `com.example`
   - **Artifact**: `blog-api`
   - **Name**: `blog-api`
   - **Package name**: `com.example.blogapi`

4. Ajoutez les dépendances:
   - Spring Web
   - Spring Data JPA
   - H2 Database
   - Validation
   - Lombok

5. Cliquez sur **Generate** et décompressez le fichier téléchargé

### Option 2: Ligne de commande

```bash
curl https://start.spring.io/starter.tgz \
  -d dependencies=web,data-jpa,h2,validation,lombok \
  -d javaVersion=17 \
  -d bootVersion=3.2.1 \
  -d type=maven-project \
  -d groupId=com.example \
  -d artifactId=blog-api \
  -d name=blog-api \
  -d packageName=com.example.blogapi \
  -d baseDir=blog-api \
  | tar -xzvf -

cd blog-api
```

## :material-numeric-2-box: Ajouter SpringFlow

Ouvrez `pom.xml` et ajoutez la dépendance SpringFlow dans la section `<dependencies>`:

```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.4.3</version>
</dependency>
```

!!! tip "Version actuelle"
    Vérifiez la dernière version sur [Maven Central](https://central.sonatype.com/artifact/io.github.tky0065/springflow-starter)

## :material-numeric-3-box: Configurer la Base de Données

Créez/modifiez `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: blog-api

  # Configuration H2 (base en mémoire pour le développement)
  datasource:
    url: jdbc:h2:mem:blogdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  # Configuration JPA
  jpa:
    hibernate:
      ddl-auto: create-drop  # Recrée les tables au démarrage
    show-sql: true           # Affiche les requêtes SQL
    properties:
      hibernate:
        format_sql: true
    defer-datasource-initialization: true

  # Console H2 (pour inspecter la base)
  h2:
    console:
      enabled: true
      path: /h2-console

# Configuration SpringFlow
springflow:
  enabled: true
  base-path: /api

  pagination:
    default-page-size: 20
    max-page-size: 100

  swagger:
    enabled: true
    title: Blog API
    description: API de gestion de blog avec SpringFlow
    version: 1.0.0
    contact-name: Votre Nom
    contact-email: votre.email@example.com

# Configuration logging
logging:
  level:
    io.springflow: DEBUG
    org.hibernate.SQL: DEBUG
```

!!! info "Base de données H2"
    H2 est une base de données en mémoire parfaite pour le développement. Pour la production, remplacez par PostgreSQL, MySQL, etc.

## :material-numeric-4-box: Créer les Entités

### Entité Author

Créez `src/main/java/com/example/blogapi/entity/Author.java`:

```java
package com.example.blogapi.entity;

import io.springflow.annotations.AutoApi;
import io.springflow.annotations.Hidden;
import io.springflow.annotations.ReadOnly;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "authors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@AutoApi(
    path = "/authors",
    description = "Author management API",
    tags = {"Authors"}
)
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    @Size(max = 500)
    private String bio;

    @Column(name = "twitter_handle")
    private String twitterHandle;

    @Hidden  // Ne sera jamais exposé dans l'API
    @Column(nullable = false)
    private String apiKey = generateApiKey();

    @ReadOnly  // Visible en lecture, non modifiable
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ReadOnly
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relation: Un auteur peut avoir plusieurs articles
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogPost> posts = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateApiKey() {
        return "AK-" + System.currentTimeMillis() + "-" +
               (int)(Math.random() * 10000);
    }
}
```

### Entité BlogPost

Créez `src/main/java/com/example/blogapi/entity/BlogPost.java`:

```java
package com.example.blogapi.entity;

import io.springflow.annotations.AutoApi;
import io.springflow.annotations.ReadOnly;
import io.springflow.annotations.Filterable;
import io.springflow.annotations.FilterType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@AutoApi(
    path = "/posts",
    description = "Blog post management API",
    tags = {"Blog Posts"}
)
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    @Filterable(types = {FilterType.LIKE, FilterType.EQUALS})
    @Column(nullable = false)
    private String title;

    @Size(max = 500)
    private String summary;

    @NotBlank(message = "Content is required")
    @Size(min = 10, message = "Content must be at least 10 characters")
    @Column(columnDefinition = "TEXT")
    private String content;

    @Filterable(types = {FilterType.EQUALS, FilterType.IN})
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.DRAFT;

    @Filterable(types = {FilterType.EQUALS, FilterType.LIKE})
    private String category;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    // Relation: Chaque article appartient à un auteur
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @ReadOnly
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ReadOnly
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ReadOnly
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == PostStatus.PUBLISHED && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == PostStatus.PUBLISHED && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }

    // Enum pour le statut
    public enum PostStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }
}
```

## :material-numeric-5-box: Ajouter des Données de Test (Optionnel)

Créez `src/main/resources/data.sql`:

```sql
-- Insérer des auteurs
INSERT INTO authors (name, email, bio, twitter_handle, api_key, created_at, updated_at)
VALUES
('Alice Johnson', 'alice@example.com', 'Passionnée de technologie et développement web', '@alicejohnson', 'AK-1234567890-5678', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Bob Smith', 'bob@example.com', 'Expert Java et architecture logicielle', '@bobsmith', 'AK-1234567891-9012', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Carol Davis', 'carol@example.com', 'Développeuse full-stack et blogueuse tech', '@caroldavis', 'AK-1234567892-3456', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insérer des articles
INSERT INTO blog_posts (title, summary, content, status, category, view_count, author_id, published_at, created_at, updated_at)
VALUES
('Introduction à SpringFlow', 'Découvrez comment SpringFlow simplifie le développement REST', 'SpringFlow est un framework révolutionnaire qui génère automatiquement des APIs REST complètes à partir de vos entités JPA. Plus besoin de créer manuellement les repositories, services et controllers!', 'PUBLISHED', 'Tutorial', 150, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Les meilleures pratiques Java 17', 'Guide complet des nouveautés Java 17', 'Java 17 apporte de nombreuses améliorations comme les records, les sealed classes, et le pattern matching. Découvrez comment les utiliser dans vos projets.', 'PUBLISHED', 'Java', 320, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Microservices avec Spring Boot', 'Architecture microservices moderne', 'Les microservices sont devenus la norme pour les applications modernes. Ce guide vous montre comment créer une architecture microservices robuste avec Spring Boot.', 'DRAFT', 'Architecture', 0, 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Guide complet de Spring Data JPA', 'Maîtrisez JPA avec Spring', 'Spring Data JPA simplifie énormément l''accès aux données. Apprenez les concepts avancés comme les specifications, les projections, et les requêtes natives.', 'PUBLISHED', 'Spring', 275, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Docker pour les développeurs', 'Containerisez vos applications facilement', 'Docker révolutionne le déploiement d''applications. Découvrez comment créer des containers optimisés pour vos applications Spring Boot.', 'DRAFT', 'DevOps', 0, 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

## :material-numeric-6-box: Lancer l'Application

### Démarrer le serveur

```bash
./mvnw spring-boot:run
```

Ou avec Gradle:

```bash
./gradlew bootRun
```

### Vérifier les logs

Vous devriez voir dans les logs:

```
INFO  AutoApiRepositoryRegistrar - Starting AutoApi Repository, Service, and Controller Registration...
INFO  AutoApiRepositoryRegistrar - Registered repository for Author
INFO  AutoApiRepositoryRegistrar - Registered service for Author
INFO  AutoApiRepositoryRegistrar - Registered controller for Author
INFO  AutoApiRepositoryRegistrar - Registered repository for BlogPost
INFO  AutoApiRepositoryRegistrar - Registered service for BlogPost
INFO  AutoApiRepositoryRegistrar - Registered controller for BlogPost
INFO  AutoApiRepositoryRegistrar - AutoApi registration completed. Registered 2 entities.
```

## :material-numeric-7-box: Tester votre API

### Accéder à Swagger UI

Ouvrez votre navigateur: **http://localhost:8080/swagger-ui.html**

Vous verrez tous vos endpoints générés automatiquement:

**Authors**:
- `GET /api/authors` - Liste des auteurs
- `GET /api/authors/{id}` - Détails d'un auteur
- `POST /api/authors` - Créer un auteur
- `PUT /api/authors/{id}` - Modifier un auteur
- `DELETE /api/authors/{id}` - Supprimer un auteur

**Blog Posts**:
- `GET /api/posts` - Liste des articles
- `GET /api/posts/{id}` - Détails d'un article
- `POST /api/posts` - Créer un article
- `PUT /api/posts/{id}` - Modifier un article
- `DELETE /api/posts/{id}` - Supprimer un article

### Tester avec cURL

#### Créer un auteur

```bash
curl -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "David Wilson",
    "email": "david@example.com",
    "bio": "Développeur passionné",
    "twitterHandle": "@davidwilson"
  }'
```

Réponse:
```json
{
  "id": 4,
  "name": "David Wilson",
  "email": "david@example.com",
  "bio": "Développeur passionné",
  "twitterHandle": "@davidwilson",
  "createdAt": "2025-12-26T15:30:00",
  "updatedAt": "2025-12-26T15:30:00"
}
```

!!! success "Notez que apiKey est caché"
    Le champ `apiKey` marqué avec `@Hidden` n'apparaît jamais dans la réponse!

#### Lister tous les auteurs

```bash
curl http://localhost:8080/api/authors
```

#### Lister avec pagination

```bash
curl "http://localhost:8080/api/authors?page=0&size=10&sort=name,asc"
```

#### Créer un article

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Mon premier article SpringFlow",
    "summary": "Un article test",
    "content": "Contenu de mon premier article créé avec SpringFlow",
    "status": "PUBLISHED",
    "category": "Tutorial",
    "author": {
      "id": 1
    }
  }'
```

#### Filtrer les articles publiés

```bash
curl "http://localhost:8080/api/posts?status=PUBLISHED"
```

#### Rechercher par titre

```bash
curl "http://localhost:8080/api/posts?title=SpringFlow"
```

### Console H2

Accédez à la console H2: **http://localhost:8080/h2-console**

- **JDBC URL**: `jdbc:h2:mem:blogdb`
- **Username**: `sa`
- **Password**: (vide)

Exécutez des requêtes SQL directement:

```sql
SELECT * FROM authors;
SELECT * FROM blog_posts;
SELECT bp.title, a.name AS author
FROM blog_posts bp
JOIN authors a ON bp.author_id = a.id;
```

## :material-numeric-8-box: Validation en Action

### Tentez de créer un auteur invalide

```bash
curl -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "A",
    "email": "invalid-email"
  }'
```

Réponse (erreur 400):
```json
{
  "timestamp": "2025-12-26T15:35:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "validationErrors": [
    {
      "field": "name",
      "message": "Name must be between 2 and 100 characters",
      "rejectedValue": "A",
      "code": "Size"
    },
    {
      "field": "email",
      "message": "Email must be valid",
      "rejectedValue": "invalid-email",
      "code": "Email"
    }
  ]
}
```

## :material-wrench: Personnalisation (Optionnel)

### Ajouter un Service Personnalisé

Si vous voulez ajouter une logique métier custom pour BlogPost:

Créez `src/main/java/com/example/blogapi/service/BlogPostService.java`:

```java
package com.example.blogapi.service;

import com.example.blogapi.entity.BlogPost;
import io.springflow.core.service.GenericCrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class BlogPostService extends GenericCrudService<BlogPost, Long> {

    private static final Logger log = LoggerFactory.getLogger(BlogPostService.class);

    public BlogPostService(@Qualifier("blogPostRepository") JpaRepository<BlogPost, Long> repository) {
        super(repository, BlogPost.class);
    }

    @Override
    protected void beforeCreate(BlogPost post) {
        log.info("Creating new blog post: {}", post.getTitle());

        // Validation custom: titre unique
        if (repository.findAll().stream()
                .anyMatch(existing -> existing.getTitle().equalsIgnoreCase(post.getTitle()))) {
            throw new IllegalArgumentException("A post with this title already exists");
        }

        // Générer un slug à partir du titre
        post.setSlug(generateSlug(post.getTitle()));
    }

    @Override
    protected void afterCreate(BlogPost post) {
        log.info("Blog post created successfully with ID: {}", post.getId());
        // Envoyer notification, invalider cache, etc.
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
```

!!! tip "SpringFlow détectera automatiquement BlogPostService"
    SpringFlow verra que vous avez un bean `blogPostService` et utilisera votre implémentation au lieu d'en générer une!

## :material-check-all: Récapitulatif

En quelques étapes simples, vous avez créé:

✅ Un projet Spring Boot complet
✅ 2 entités JPA avec relations
✅ 10 endpoints REST automatiquement générés
✅ Validation des données
✅ Documentation Swagger interactive
✅ Données de test
✅ Gestion des champs cachés et en lecture seule

**Tout ça sans écrire un seul Repository, Service ou Controller!**

## :material-lightbulb: Prochaines Étapes

### Fonctionnalités Avancées

1. **Filtrage Dynamique** - [Guide de filtrage](../guide/filtering.md)
   ```bash
   GET /api/posts?status=PUBLISHED&category=Tutorial
   ```

2. **Sécurité** - [Guide de sécurité](../guide/security.md)
   ```yaml
   springflow:
     security:
       enabled: true
       default-level: AUTHENTICATED
   ```

3. **Composants Personnalisés** - [Documentation complète](../advanced/custom-components.md)
   - Repositories custom avec requêtes complexes
   - Services custom avec logique métier
   - Controllers custom avec endpoints spécifiques

4. **Soft Delete** - [Guide soft delete](../guide/soft-delete.md)
   ```java
   @SoftDelete
   private Boolean deleted = false;
   ```

5. **Audit Trail** - [Guide auditing](../guide/auditing.md)
   ```java
   @Auditable
   @CreatedBy
   private String createdBy;
   ```

### Migration vers Production

Pour passer en production:

1. **Remplacer H2 par une vraie base de données**:

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/blogdb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate  # Ne pas recréer les tables!
```

2. **Ajouter la sécurité**:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

3. **Configurer les CORS** si nécessaire:

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("https://monsite.com")
                        .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
```

4. **Activer les métriques**:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

## :material-help-circle: Besoin d'Aide?

- :material-book-open-variant: **Documentation**: Consultez le [Guide utilisateur](../guide/index.md)
- :material-github: **Code source**: [SpringFlow sur GitHub](https://github.com/tky0065/springflow)
- :material-bug: **Problème**: [Ouvrir une issue](https://github.com/tky0065/springflow/issues)
- :material-lightbulb: **Exemples**: Explorez `springflow-demo/` dans le repository

## :material-rocket: Ressources Supplémentaires

- [Configuration complète](../guide/configuration.md)
- [Annotations disponibles](../guide/annotations.md)
- [Architecture SpringFlow](../advanced/architecture.md)
- [Best Practices](../advanced/best-practices.md)
- [Feuille de route](../about/roadmap.md)

---

**Félicitations!** Vous avez créé votre première API avec SpringFlow. Le vrai pouvoir de SpringFlow se révèle quand vous réalisez que vous pouvez ajouter 10 nouvelles entités et obtenir instantanément 50+ endpoints REST complets! 🚀
