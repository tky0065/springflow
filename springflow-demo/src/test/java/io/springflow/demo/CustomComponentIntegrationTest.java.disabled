package io.springflow.demo;

import io.springflow.demo.controller.CustomerController;
import io.springflow.demo.controller.ShipmentController;
import io.springflow.demo.entity.Customer;
import io.springflow.demo.entity.Invoice;
import io.springflow.demo.entity.Order;
import io.springflow.demo.entity.Shipment;
import io.springflow.demo.repository.CustomerRepository;
import io.springflow.demo.repository.OrderRepository;
import io.springflow.demo.service.CustomerService;
import io.springflow.demo.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for custom component detection and functionality.
 * Tests that SpringFlow properly detects custom repositories, services, and controllers,
 * and that all custom functionality works correctly end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomComponentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        orderRepository.deleteAll();
        customerRepository.deleteAll();
    }

    /**
     * Test Scenario 1: Order Entity with Custom Repository
     * Verify that:
     * - orderRepository is custom (OrderRepository interface)
     * - orderService is generated
     * - orderController is generated
     */
    @Test
    void orderEntity_withCustomRepository_shouldUseCustomRepoAndGenerateServiceController() {
        // Verify beans exist
        assertThat(context.containsBean("orderRepository")).isTrue();
        assertThat(context.containsBean("orderService")).isTrue();
        assertThat(context.containsBean("orderController")).isTrue();

        // Verify custom repository type
        Object repo = context.getBean("orderRepository");
        assertThat(repo).isInstanceOf(OrderRepository.class);

        // Test custom repository query method
        Order order1 = new Order("ORD-001", new BigDecimal("100.00"));
        Order order2 = new Order("ORD-002", new BigDecimal("200.00"));
        orderRepository.save(order1);
        orderRepository.save(order2);

        // Test custom query method
        assertThat(orderRepository.findByOrderNumber("ORD-001")).isPresent();
        assertThat(orderRepository.findByTotalAmountGreaterThanEqual(new BigDecimal("150.00")))
                .hasSize(1)
                .first()
                .extracting(Order::getOrderNumber)
                .isEqualTo("ORD-002");
    }

    /**
     * Test Scenario 2: Invoice Entity with Custom Service
     * Verify that:
     * - invoiceRepository is generated
     * - invoiceService is custom (InvoiceService class)
     * - invoiceController is generated
     */
    @Test
    void invoiceEntity_withCustomService_shouldUseCustomServiceAndGenerateRepoController() {
        // Verify beans exist
        assertThat(context.containsBean("invoiceRepository")).isTrue();
        assertThat(context.containsBean("invoiceService")).isTrue();
        assertThat(context.containsBean("invoiceController")).isTrue();

        // Verify custom service type
        Object service = context.getBean("invoiceService");
        assertThat(service).isInstanceOf(InvoiceService.class);
    }

    /**
     * Test Scenario 3: Shipment Entity with Custom Controller
     * Verify that:
     * - shipmentRepository is generated
     * - shipmentService is generated
     * - shipmentController is custom (ShipmentController class)
     */
    @Test
    void shipmentEntity_withCustomController_shouldUseCustomControllerAndGenerateRepoService() {
        // Verify beans exist
        assertThat(context.containsBean("shipmentRepository")).isTrue();
        assertThat(context.containsBean("shipmentService")).isTrue();
        assertThat(context.containsBean("shipmentController")).isTrue();

        // Verify custom controller type
        Object controller = context.getBean("shipmentController");
        assertThat(controller).isInstanceOf(ShipmentController.class);
    }

    /**
     * Test Scenario 4: Customer Entity with All Custom Components
     * Verify that:
     * - customerRepository is custom
     * - customerService is custom
     * - customerController is custom
     */
    @Test
    void customerEntity_withAllCustomComponents_shouldUseAllCustomImplementations() {
        // Verify beans exist
        assertThat(context.containsBean("customerRepository")).isTrue();
        assertThat(context.containsBean("customerService")).isTrue();
        assertThat(context.containsBean("customerController")).isTrue();

        // Verify all are custom types
        assertThat(context.getBean("customerRepository")).isInstanceOf(CustomerRepository.class);
        assertThat(context.getBean("customerService")).isInstanceOf(CustomerService.class);
        assertThat(context.getBean("customerController")).isInstanceOf(CustomerController.class);
    }

    /**
     * Test Scenario 5: Invoice Service Custom Logic
     * Test that InvoiceService (extends GenericCrudService) provides both
     * inherited and custom functionality.
     */
    @Test
    void invoiceService_extendsGenericCrudService_shouldProvideCustomAndInheritedBehavior() {
        // Test custom validation (beforeCreate hook)
        Invoice invalidInvoice = new Invoice();
        invalidInvoice.setAmount(new BigDecimal("-10.00")); // Negative amount

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> invoiceService.save(invalidInvoice)
        )).hasMessageContaining("positive");

        // Test auto-generation of invoice number
        Invoice validInvoice = new Invoice();
        validInvoice.setAmount(new BigDecimal("100.00"));

        Invoice saved = invoiceService.save(validInvoice);
        assertThat(saved.getInvoiceNumber()).isNotNull().startsWith("INV-");
        assertThat(saved.getIssueDate()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("DRAFT");

        // Test custom business method
        BigDecimal totalRevenue = invoiceService.getTotalRevenue();
        assertThat(totalRevenue).isEqualByComparingTo(new BigDecimal("100.00"));

        // Test inherited CRUD methods
        assertThat(invoiceService.findAll()).hasSize(1);
        assertThat(invoiceService.findById(saved.getId())).isNotNull();
    }

    /**
     * Test Scenario 6: Shipment Controller Custom Endpoints
     * Test that ShipmentController (extends GenericCrudController) provides both
     * standard CRUD endpoints and custom endpoints.
     */
    @Test
    void shipmentController_extendsGenericCrudController_shouldProvideCustomAndInheritedEndpoints() throws Exception {
        // Create a shipment via standard POST endpoint (inherited)
        mockMvc.perform(post("/api/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"trackingNumber\":\"TRK-12345\",\"status\":\"PENDING\",\"carrier\":\"UPS\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trackingNumber").value("TRK-12345"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Get all shipments via standard GET endpoint (inherited)
        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].trackingNumber").value("TRK-12345"));

        // Use custom endpoint: update status
        mockMvc.perform(put("/api/shipments/1/update-status")
                .param("status", "IN_TRANSIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));
    }

    /**
     * Test Scenario 7: Custom Repository Query Methods
     * Test that OrderRepository custom query methods work correctly.
     */
    @Test
    void customRepository_customQueryMethod_shouldWorkCorrectly() {
        // Create test data
        Order order1 = new Order("ORD-001", new BigDecimal("100.00"));
        order1.setStatus("PENDING");
        Order order2 = new Order("ORD-002", new BigDecimal("200.00"));
        order2.setStatus("CONFIRMED");
        Order order3 = new Order("ORD-003", new BigDecimal("300.00"));
        order3.setStatus("CONFIRMED");

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        // Test findByStatus
        assertThat(orderRepository.findByStatus("CONFIRMED")).hasSize(2);
        assertThat(orderRepository.findByStatus("PENDING")).hasSize(1);

        // Test custom JPQL query: calculateTotalRevenueByStatus
        BigDecimal confirmedRevenue = orderRepository.calculateTotalRevenueByStatus("CONFIRMED");
        assertThat(confirmedRevenue).isEqualByComparingTo(new BigDecimal("500.00"));

        // Test count by status
        assertThat(orderRepository.countByStatus("CONFIRMED")).isEqualTo(2);
    }

    /**
     * Test Scenario 8: Custom Service Business Logic
     * Test that InvoiceService custom business methods work correctly.
     */
    @Test
    void customService_customBusinessLogic_shouldExecuteCorrectly() {
        // Create test invoices
        Invoice invoice1 = new Invoice();
        invoice1.setAmount(new BigDecimal("100.00"));
        Invoice saved1 = invoiceService.save(invoice1);

        Invoice invoice2 = new Invoice();
        invoice2.setAmount(new BigDecimal("200.00"));
        Invoice saved2 = invoiceService.save(invoice2);

        // Issue first invoice
        Invoice issued = invoiceService.issueInvoice(saved1.getId());
        assertThat(issued.getStatus()).isEqualTo("ISSUED");

        // Test revenue by status
        BigDecimal draftRevenue = invoiceService.getRevenueByStatus("DRAFT");
        assertThat(draftRevenue).isEqualByComparingTo(new BigDecimal("200.00"));

        BigDecimal issuedRevenue = invoiceService.getRevenueByStatus("ISSUED");
        assertThat(issuedRevenue).isEqualByComparingTo(new BigDecimal("100.00"));

        // Test total revenue
        BigDecimal totalRevenue = invoiceService.getTotalRevenue();
        assertThat(totalRevenue).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    /**
     * Test Scenario 9: Custom Controller Endpoints
     * Test that CustomerController (fully custom) endpoints work correctly.
     */
    @Test
    void customController_customEndpoint_shouldBeAccessible() throws Exception {
        // Create a customer via custom POST endpoint and get the ID
        String response = mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"companyName\":\"Acme Corp\",\"email\":\"contact@acme.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyName").value("Acme Corp"))
                .andExpect(jsonPath("$.customerCode").exists())
                .andExpect(jsonPath("$.customerCode").value(org.hamcrest.Matchers.startsWith("CUST-")))
                .andReturn().getResponse().getContentAsString();

        // Extract ID from response
        Long customerId = customerService.findAll().get(0).getId();

        // Get all customers via custom GET endpoint
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].companyName").value("Acme Corp"));

        // Test custom endpoint: activate (customer is already ACTIVE from creation)
        // Note: Customer is created with ACTIVE status by default, so this just confirms it
        mockMvc.perform(post("/api/customers/" + customerId + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Test custom endpoint: statistics
        mockMvc.perform(get("/api/customers/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.active").value(1));
    }

    /**
     * Test Scenario 10: Bean Type Verification
     * Verify that correct bean types are registered for each entity.
     */
    @Test
    void whenCustomComponentsExist_shouldRegisterCorrectBeanTypes() {
        // Order: custom repository, generated service and controller
        assertThat(context.getBean("orderRepository")).isInstanceOf(OrderRepository.class);
        assertThat(context.getBean("orderService")).isNotNull(); // Generated service
        assertThat(context.getBean("orderController")).isNotNull(); // Generated controller

        // Invoice: generated repository, custom service, generated controller
        assertThat(context.getBean("invoiceRepository")).isNotNull(); // Generated repository
        assertThat(context.getBean("invoiceService")).isInstanceOf(InvoiceService.class);
        assertThat(context.getBean("invoiceController")).isNotNull(); // Generated controller

        // Shipment: generated repository and service, custom controller
        assertThat(context.getBean("shipmentRepository")).isNotNull(); // Generated repository
        assertThat(context.getBean("shipmentService")).isNotNull(); // Generated service
        assertThat(context.getBean("shipmentController")).isInstanceOf(ShipmentController.class);

        // Customer: all custom
        assertThat(context.getBean("customerRepository")).isInstanceOf(CustomerRepository.class);
        assertThat(context.getBean("customerService")).isInstanceOf(CustomerService.class);
        assertThat(context.getBean("customerController")).isInstanceOf(CustomerController.class);
    }

    /**
     * Additional test: Verify customer service auto-generation logic
     */
    @Test
    void customerService_shouldAutoGenerateCustomerCode() {
        Customer customer = new Customer();
        customer.setCompanyName("Test Company");

        Customer saved = customerService.create(customer);

        assertThat(saved.getCustomerCode()).isNotNull().startsWith("CUST-").hasSize(13);
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
    }

    /**
     * Additional test: Verify invoice status transition validation
     */
    @Test
    void invoiceService_shouldValidateStatusTransitions() {
        Invoice invoice = new Invoice();
        invoice.setAmount(new BigDecimal("100.00"));
        Invoice saved = invoiceService.save(invoice);

        // Issue the invoice
        Invoice issued = invoiceService.issueInvoice(saved.getId());
        assertThat(issued.getStatus()).isEqualTo("ISSUED");

        // Mark as paid
        Invoice paid = invoiceService.markAsPaid(issued.getId());
        assertThat(paid.getStatus()).isEqualTo("PAID");

        // Try to issue again (should fail - invalid transition from PAID)
        // Create a new invoice object to avoid Hibernate managed entity issues
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> {
                    Invoice toUpdate = new Invoice();
                    toUpdate.setId(paid.getId());
                    toUpdate.setInvoiceNumber(paid.getInvoiceNumber());
                    toUpdate.setAmount(paid.getAmount());
                    toUpdate.setIssueDate(paid.getIssueDate());
                    toUpdate.setDueDate(paid.getDueDate());
                    toUpdate.setStatus("ISSUED");  // Invalid transition from PAID
                    invoiceService.update(toUpdate.getId(), toUpdate);
                }
        )).hasMessageContaining("transition");
    }
}
