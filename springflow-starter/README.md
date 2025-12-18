# SpringFlow Starter

Spring Boot starter for auto-configuration of SpringFlow framework.

## Usage

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>io.springflow</groupId>
    <artifactId>springflow-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Enable SpringFlow in your application:

```java
@SpringBootApplication
@EnableSpringFlow(basePackages = "com.example.entities")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## Configuration

Configure via `application.yml`:

```yaml
springflow:
  enabled: true
  base-path: /api
  base-packages: com.example.entities
  pagination:
    default-size: 20
    max-size: 100
```
