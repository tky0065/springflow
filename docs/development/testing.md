# Testing

Stratégie et exécution des tests.

## Exécuter les Tests

### Tous les Tests

```bash
./mvnw test
```

### Tests d'un Module

```bash
cd springflow-core
../mvnw test
```

### Test Spécifique

```bash
./mvnw test -Dtest=EntityScannerTest
```

### Méthode Spécifique

```bash
./mvnw test -Dtest=EntityScannerTest#shouldScanEntities
```

## Coverage

### Générer le Rapport

```bash
./mvnw clean test jacoco:report
```

Rapport dans : `target/site/jacoco/index.html`

### Objectif

- **Target**: >80% coverage
- **Current**: ~85% sur springflow-core

## Structure des Tests

```
springflow-core/src/test/java/
├── io.springflow.core.scanner/
│   ├── EntityScannerTest.java
│   └── EntityScannerPerformanceTest.java
├── io.springflow.core.metadata/
│   └── MetadataResolverTest.java
├── io.springflow.core.mapper/
│   ├── EntityDtoMapperTest.java
│   ├── DtoMapperFactoryTest.java
│   └── CircularReferenceTest.java
├── io.springflow.core.filter/
│   ├── FilterResolverTest.java
│   └── NPlusOneQueryTest.java
└── ...
```

## Types de Tests

### Unit Tests

Tests des composants isolés :

```java
@Test
void shouldScanEntities() {
    List<Class<?>> entities = entityScanner.scan("io.springflow");
    assertThat(entities).isNotEmpty();
}
```

### Integration Tests

Tests avec Spring context :

```java
@SpringBootTest
@DataJpaTest
class NPlusOneQueryTest {
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    void shouldAvoidNPlusOne() {
        // Test with actual database
    }
}
```

### Performance Tests

```java
@Test
@Disabled("Performance test - run manually")
void testScanPerformance() {
    // Benchmark
}
```

## Voir Aussi

- [Building](building.md)
- [Contributing](contributing.md)
