package io.springflow.core.scanner;

import io.springflow.core.scanner.testentities.Order;
import io.springflow.core.scanner.testentities.Product;
import io.springflow.core.scanner.testentities.User;
import io.springflow.core.scanner.testentities.sub.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link EntityScanner}.
 *
 * @author SpringFlow
 * @since 0.1.0
 */
@DisplayName("EntityScanner Tests")
class EntityScannerTest {

    private EntityScanner scanner;

    @BeforeEach
    void setUp() {
        scanner = new EntityScanner();
    }

    @Test
    @DisplayName("Should scan package and find entities with @AutoApi")
    void testScanPackageSimple() {
        // When
        List<Class<?>> entities = scanner.scanEntities("io.springflow.core.scanner.testentities");

        // Then
        assertThat(entities)
            .isNotNull()
            .hasSize(3)
            .contains(User.class, Product.class, Category.class)
            .doesNotContain(Order.class); // Order doesn't have @AutoApi
    }

    @Test
    @DisplayName("Should scan multiple packages")
    void testScanMultiplePackages() {
        // When
        List<Class<?>> entities = scanner.scanEntities(
            "io.springflow.core.scanner.testentities",
            "io.springflow.core.scanner.testentities.sub"
        );

        // Then
        assertThat(entities)
            .isNotNull()
            .hasSize(3)
            .contains(User.class, Product.class, Category.class)
            .doesNotContain(Order.class);
    }

    @Test
    @DisplayName("Should scan packages with sub-packages")
    void testScanWithSubPackages() {
        // When - scanning parent package should find entities in sub-packages
        List<Class<?>> entities = scanner.scanEntities("io.springflow.core.scanner.testentities");

        // Then - should find entities in both parent and sub-packages
        assertThat(entities)
            .isNotNull()
            .contains(User.class, Product.class, Category.class);
    }

    @Test
    @DisplayName("Should return empty list when no entities found")
    void testScanEmptyPackage() {
        // When
        List<Class<?>> entities = scanner.scanEntities("io.springflow.nonexistent");

        // Then
        assertThat(entities)
            .isNotNull()
            .isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when no packages specified")
    void testScanNoPackages() {
        // When
        List<Class<?>> entities = scanner.scanEntities();

        // Then
        assertThat(entities)
            .isNotNull()
            .isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when null packages specified")
    void testScanNullPackages() {
        // When
        List<Class<?>> entities = scanner.scanEntities((String[]) null);

        // Then
        assertThat(entities)
            .isNotNull()
            .isEmpty();
    }

    @Test
    @DisplayName("Should use cache for subsequent scans")
    void testCacheFunctional() {
        // Given
        String packageName = "io.springflow.core.scanner.testentities";

        // When - first scan
        List<Class<?>> firstScan = scanner.scanEntities(packageName);
        EntityScanner.CacheStatistics statsAfterFirst = scanner.getStatistics();

        // Then - should be cache miss
        assertThat(statsAfterFirst.getMisses()).isEqualTo(1);
        assertThat(statsAfterFirst.getHits()).isEqualTo(0);

        // When - second scan with same package
        List<Class<?>> secondScan = scanner.scanEntities(packageName);
        EntityScanner.CacheStatistics statsAfterSecond = scanner.getStatistics();

        // Then - should be cache hit
        assertThat(statsAfterSecond.getMisses()).isEqualTo(1);
        assertThat(statsAfterSecond.getHits()).isEqualTo(1);
        assertThat(firstScan).isEqualTo(secondScan);
    }

    @Test
    @DisplayName("Should clear cache correctly")
    void testClearCache() {
        // Given - scan and cache some entities
        scanner.scanEntities("io.springflow.core.scanner.testentities");
        assertThat(scanner.getCacheSize()).isGreaterThan(0);

        // When
        scanner.clearCache();

        // Then
        assertThat(scanner.getCacheSize()).isEqualTo(0);
        assertThat(scanner.getStatistics().getHits()).isEqualTo(0);
        assertThat(scanner.getStatistics().getMisses()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should detect @AutoApi annotation correctly")
    void testHasAutoApiAnnotation() {
        // When/Then
        assertThat(scanner.hasAutoApiAnnotation(User.class)).isTrue();
        assertThat(scanner.hasAutoApiAnnotation(Product.class)).isTrue();
        assertThat(scanner.hasAutoApiAnnotation(Category.class)).isTrue();
        assertThat(scanner.hasAutoApiAnnotation(Order.class)).isFalse();
    }

    @Test
    @DisplayName("Should handle null class in hasAutoApiAnnotation")
    void testHasAutoApiAnnotationWithNull() {
        // When/Then
        assertThat(scanner.hasAutoApiAnnotation(null)).isFalse();
    }

    @Test
    @DisplayName("Should respect cache size limit")
    void testCacheSizeLimit() {
        // Given - scanner with small cache size
        EntityScanner smallCacheScanner = new EntityScanner(2);

        // When - scan more packages than cache size
        smallCacheScanner.scanEntities("io.springflow.core.scanner.testentities");
        smallCacheScanner.scanEntities("io.springflow.core.scanner.testentities.sub");
        smallCacheScanner.scanEntities("io.springflow.core.scanner"); // Third scan

        // Then - cache size should not exceed limit
        assertThat(smallCacheScanner.getCacheSize()).isLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should calculate cache statistics correctly")
    void testCacheStatistics() {
        // Given
        String pkg1 = "io.springflow.core.scanner.testentities";
        String pkg2 = "io.springflow.core.scanner.testentities.sub";

        // When
        scanner.scanEntities(pkg1); // miss
        scanner.scanEntities(pkg1); // hit
        scanner.scanEntities(pkg2); // miss
        scanner.scanEntities(pkg1); // hit
        scanner.scanEntities(pkg2); // hit

        // Then
        EntityScanner.CacheStatistics stats = scanner.getStatistics();
        assertThat(stats.getHits()).isEqualTo(3);
        assertThat(stats.getMisses()).isEqualTo(2);
        assertThat(stats.getTotal()).isEqualTo(5);
        assertThat(stats.getHitRate()).isEqualTo(0.6);
    }

    @Test
    @DisplayName("Should provide meaningful cache statistics toString")
    void testCacheStatisticsToString() {
        // Given
        scanner.scanEntities("io.springflow.core.scanner.testentities");
        scanner.scanEntities("io.springflow.core.scanner.testentities");

        // When
        String statsString = scanner.getStatistics().toString();

        // Then
        assertThat(statsString)
            .contains("hits=1")
            .contains("misses=1")
            .contains("hitRate=50");
    }
}

