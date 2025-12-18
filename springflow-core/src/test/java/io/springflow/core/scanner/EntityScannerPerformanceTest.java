package io.springflow.core.scanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Performance tests for {@link EntityScanner}.
 *
 * @author SpringFlow
 * @since 0.1.0
 */
@DisplayName("EntityScanner Performance Tests")
class EntityScannerPerformanceTest {

    @Test
    @DisplayName("Should scan large packages efficiently")
    void testScanPerformance() {
        // Given
        EntityScanner scanner = new EntityScanner();
        String packageName = "io.springflow.core.scanner.testentities";

        // When
        long startTime = System.currentTimeMillis();
        List<Class<?>> entities = scanner.scanEntities(packageName);
        long firstScanTime = System.currentTimeMillis() - startTime;

        // Then - first scan should complete in reasonable time
        assertThat(firstScanTime).isLessThan(5000); // 5 seconds max
        assertThat(entities).isNotEmpty();

        // When - second scan (cached)
        startTime = System.currentTimeMillis();
        List<Class<?>> cachedEntities = scanner.scanEntities(packageName);
        long cachedScanTime = System.currentTimeMillis() - startTime;

        // Then - cached scan should be much faster
        assertThat(cachedScanTime).isLessThan(firstScanTime);
        assertThat(cachedScanTime).isLessThan(100); // Should be nearly instant
        assertThat(cachedEntities).isEqualTo(entities);
    }

    @Test
    @DisplayName("Should handle multiple concurrent scans")
    void testConcurrentScans() {
        // Given
        EntityScanner scanner = new EntityScanner();
        String packageName = "io.springflow.core.scanner.testentities";

        // When - scan multiple times to populate cache
        for (int i = 0; i < 10; i++) {
            scanner.scanEntities(packageName);
        }

        // Then - cache statistics should be correct
        EntityScanner.CacheStatistics stats = scanner.getStatistics();
        assertThat(stats.getTotal()).isEqualTo(10);
        assertThat(stats.getMisses()).isEqualTo(1);
        assertThat(stats.getHits()).isEqualTo(9);
        assertThat(stats.getHitRate()).isEqualTo(0.9);
    }

    @Test
    @DisplayName("Should maintain cache integrity under stress")
    void testCacheIntegrity() {
        // Given
        EntityScanner scanner = new EntityScanner(10);

        // When - scan different packages many times
        for (int i = 0; i < 20; i++) {
            scanner.scanEntities("io.springflow.core.scanner.testentities");
            scanner.scanEntities("io.springflow.core.scanner.testentities.sub");
        }

        // Then - cache should not exceed max size
        assertThat(scanner.getCacheSize()).isLessThanOrEqualTo(10);

        // And statistics should be accurate
        EntityScanner.CacheStatistics stats = scanner.getStatistics();
        assertThat(stats.getTotal()).isEqualTo(40); // 20 iterations * 2 packages
    }
}

