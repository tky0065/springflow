/**
 * Core annotations for SpringFlow framework.
 *
 * <p>This package contains all the annotations needed to auto-generate
 * complete REST APIs from JPA entities.
 *
 * <h2>Primary Annotations</h2>
 * <ul>
 *   <li>{@link io.springflow.annotations.AutoApi} - Main annotation to generate REST API</li>
 *   <li>{@link io.springflow.annotations.Filterable} - Enable dynamic filtering on fields</li>
 * </ul>
 *
 * <h2>Field Modifiers</h2>
 * <ul>
 *   <li>{@link io.springflow.annotations.Hidden} - Exclude field from DTOs</li>
 *   <li>{@link io.springflow.annotations.ReadOnly} - Make field read-only in API</li>
 * </ul>
 *
 * <h2>Phase 2 Annotations</h2>
 * <ul>
 *   <li>{@link io.springflow.annotations.SoftDelete} - Enable soft delete functionality</li>
 *   <li>{@link io.springflow.annotations.Auditable} - Enable audit trail</li>
 * </ul>
 *
 * <h2>Supporting Types</h2>
 * <ul>
 *   <li>{@link io.springflow.annotations.Expose} - Control which CRUD operations are exposed</li>
 *   <li>{@link io.springflow.annotations.FilterType} - Types of filters available</li>
 *   <li>{@link io.springflow.annotations.Security} - Security configuration</li>
 *   <li>{@link io.springflow.annotations.SecurityLevel} - Security levels</li>
 * </ul>
 *
 * @author SpringFlow
 * @since 0.1.0
 */
package io.springflow.annotations;
