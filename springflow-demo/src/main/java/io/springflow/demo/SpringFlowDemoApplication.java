package io.springflow.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SpringFlow Demo Application.
 * <p>
 * This application demonstrates the capabilities of SpringFlow framework
 * by automatically generating REST APIs from JPA entities using auto-configuration.
 * </p>
 * <p>
 * Features demonstrated:
 * <ul>
 *   <li>Auto-generated CRUD endpoints</li>
 *   <li>Automatic OpenAPI/Swagger documentation</li>
 *   <li>Pagination and sorting</li>
 *   <li>JSR-380 validation</li>
 *   <li>Custom error handling</li>
 *   <li>DTO mapping with @Hidden and @ReadOnly support</li>
 * </ul>
 * </p>
 * <p>
 * Access points after startup:
 * <ul>
 *   <li>API Base: http://localhost:8080/api</li>
 *   <li>Swagger UI: http://localhost:8080/swagger-ui.html</li>
 *   <li>OpenAPI Spec: http://localhost:8080/v3/api-docs</li>
 *   <li>H2 Console: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:springflowdb, User: sa, Password: )</li>
 * </ul>
 * </p>
 * <p>
 * SpringFlow auto-configuration will automatically:
 * <ul>
 *   <li>Scan for @AutoApi annotated entities</li>
 *   <li>Generate repositories, services, and controllers</li>
 *   <li>Register REST endpoints with pagination support</li>
 *   <li>Configure OpenAPI documentation</li>
 * </ul>
 * </p>
 */
@SpringBootApplication
public class SpringFlowDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringFlowDemoApplication.class, args);
    }
}
