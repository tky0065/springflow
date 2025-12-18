/**
 * Entity scanning components for discovering {@code @AutoApi} annotated entities.
 *
 * <p>This package contains the classpath scanner that discovers JPA entities
 * annotated with {@link io.springflow.annotations.AutoApi} for automatic
 * REST API generation.
 *
 * <h2>Main Components</h2>
 * <ul>
 *   <li>{@link io.springflow.core.scanner.EntityScanner} - Scans classpath for entities</li>
 *   <li>{@link io.springflow.core.scanner.ScanException} - Exception for scan failures</li>
 * </ul>
 *
 * @author SpringFlow
 * @since 0.1.0
 */
package io.springflow.core.scanner;
