package io.springflow.starter.annotation;

import io.springflow.starter.config.SpringFlowAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables SpringFlow framework for automatic REST API generation.
 * <p>
 * This annotation can be placed on a Spring Boot main class or any @Configuration class
 * to activate SpringFlow's automatic repository, service, and controller generation.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * &#64;SpringBootApplication
 * &#64;EnableSpringFlow(basePackages = "com.example.domain")
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * </pre>
 *
 * <p>
 * Note: When using Spring Boot, @EnableSpringFlow is optional if you have
 * springflow-starter on your classpath. Auto-configuration will activate automatically.
 * This annotation is useful when you want to explicitly specify basePackages or
 * when not using Spring Boot auto-configuration.
 * </p>
 *
 * @see SpringFlowAutoConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SpringFlowAutoConfiguration.class)
public @interface EnableSpringFlow {

    /**
     * Alias for {@link #basePackages}.
     * <p>
     * Allows for more concise annotation declarations e.g.:
     * {@code @EnableSpringFlow("com.example.domain")} instead of
     * {@code @EnableSpringFlow(basePackages = "com.example.domain")}.
     *
     * @return the base packages to scan
     */
    String[] value() default {};

    /**
     * Base packages to scan for @AutoApi annotated entities.
     * <p>
     * If not specified, SpringFlow will use Spring Boot's auto-configuration packages,
     * which typically start from the package containing the @SpringBootApplication class.
     * </p>
     * <p>
     * Use {@link #value} as an alias for this attribute.
     *
     * @return the base packages to scan
     */
    String[] basePackages() default {};
}
