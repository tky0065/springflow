# Getting Started

Bienvenue dans SpringFlow ! Cette section vous guide pas à pas pour démarrer rapidement.

## 📚 Dans cette section

<div class="grid cards" markdown>

-   :rocket: **[Quick Start](quickstart.md)**

    ---

    Créez votre première API en 5 minutes avec SpringFlow

-   :package: **[Installation](installation.md)**

    ---

    Guide détaillé d'installation et configuration

-   :hammer_and_wrench: **[First Project](first-project.md)**

    ---

    Construisez votre premier projet complet

</div>

## 🎯 Prérequis

Avant de commencer, assurez-vous d'avoir:

- ☕ **Java 17** ou supérieur
- 🍃 **Spring Boot 3.2.1** ou supérieur
- 📦 **Maven 3.6+** ou **Gradle 7.0+**

## ⚡ Installation Rapide

=== "Maven"

    ```xml
    <dependency>
        <groupId>io.github.tky0065</groupId>
        <artifactId>springflow-starter</artifactId>
        <version>0.2.0</version>
    </dependency>
    ```

=== "Gradle"

    ```gradle
    implementation 'io.github.tky0065:springflow-starter:0.2.0'
    ```

## 🚀 Exemple Minimal

```java
@Entity
@AutoApi(path = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @Min(0)
    private BigDecimal price;
}
```

**C'est tout !** SpringFlow génère automatiquement :

✅ `GET /api/products` - Liste avec pagination
✅ `GET /api/products/{id}` - Détails
✅ `POST /api/products` - Création
✅ `PUT /api/products/{id}` - Mise à jour
✅ `DELETE /api/products/{id}` - Suppression

## 📖 Parcours Recommandé

1. **[Quick Start](quickstart.md)** - Commencez ici pour créer votre première API
2. **[Installation](installation.md)** - Configuration détaillée
3. **[First Project](first-project.md)** - Projet complet avec exemples

---

Besoin d'aide ? Consultez la [documentation complète](../guide/annotations.md) ou [ouvrez une issue](https://github.com/tky0065/springflow/issues).
