package io.springflow.demo.controller;

import io.springflow.demo.entity.Customer;
import io.springflow.demo.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Fully custom controller for Customer entity.
 * Does NOT extend GenericCrudController - completely custom implementation.
 *
 * Demonstrates SpringFlow's ability to work with fully custom controllers
 * that don't follow the framework's patterns.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    /**
     * Get all customers.
     *
     * GET /api/customers
     *
     * @return list of all customers
     */
    @GetMapping
    public ResponseEntity<List<Customer>> findAll() {
        log.debug("REST request to find all customers");
        List<Customer> customers = service.findAll();
        return ResponseEntity.ok(customers);
    }

    /**
     * Get customer by ID.
     *
     * GET /api/customers/{id}
     *
     * @param id the customer ID
     * @return the customer
     */
    @GetMapping("/{id}")
    public ResponseEntity<Customer> findById(@PathVariable Long id) {
        log.debug("REST request to find customer with id: {}", id);
        Customer customer = service.findById(id);
        return ResponseEntity.ok(customer);
    }

    /**
     * Get customer by customer code.
     *
     * GET /api/customers/by-code/{customerCode}
     *
     * @param customerCode the customer code
     * @return the customer
     */
    @GetMapping("/by-code/{customerCode}")
    public ResponseEntity<Customer> findByCode(@PathVariable String customerCode) {
        log.debug("REST request to find customer with code: {}", customerCode);
        Customer customer = service.findByCustomerCode(customerCode);
        return ResponseEntity.ok(customer);
    }

    /**
     * Get all active customers.
     *
     * GET /api/customers/active
     *
     * @return list of active customers, sorted by company name
     */
    @GetMapping("/active")
    public ResponseEntity<List<Customer>> findAllActive() {
        log.debug("REST request to find all active customers");
        List<Customer> customers = service.findAllActiveCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * Search customers by company name.
     *
     * GET /api/customers/search?q=acme
     *
     * @param query search query
     * @return list of matching customers
     */
    @GetMapping("/search")
    public ResponseEntity<List<Customer>> search(@RequestParam("q") String query) {
        log.debug("REST request to search customers with query: {}", query);
        List<Customer> customers = service.searchByCompanyName(query);
        return ResponseEntity.ok(customers);
    }

    /**
     * Get customers by status.
     *
     * GET /api/customers/by-status/{status}
     *
     * @param status the customer status
     * @return list of customers with the given status
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<Customer>> findByStatus(@PathVariable String status) {
        log.debug("REST request to find customers with status: {}", status);
        List<Customer> customers = service.findByStatus(status);
        return ResponseEntity.ok(customers);
    }

    /**
     * Create a new customer.
     *
     * POST /api/customers
     *
     * @param customer the customer to create
     * @return the created customer
     */
    @PostMapping
    public ResponseEntity<Customer> create(@Valid @RequestBody Customer customer) {
        log.debug("REST request to create customer: {}", customer.getCompanyName());

        if (customer.getId() != null) {
            throw new IllegalArgumentException("A new customer cannot already have an ID");
        }

        Customer created = service.create(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing customer.
     *
     * PUT /api/customers/{id}
     *
     * @param id the customer ID
     * @param customer the updated customer data
     * @return the updated customer
     */
    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(
            @PathVariable Long id,
            @Valid @RequestBody Customer customer
    ) {
        log.debug("REST request to update customer with id: {}", id);

        Customer updated = service.update(id, customer);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a customer.
     *
     * DELETE /api/customers/{id}
     *
     * @param id the customer ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("REST request to delete customer with id: {}", id);
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activate a customer (change status to ACTIVE).
     *
     * POST /api/customers/{id}/activate
     *
     * @param id the customer ID
     * @return the updated customer
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Customer> activate(@PathVariable Long id) {
        log.debug("REST request to activate customer with id: {}", id);
        Customer customer = service.activate(id);
        return ResponseEntity.ok(customer);
    }

    /**
     * Deactivate a customer (change status to INACTIVE).
     *
     * POST /api/customers/{id}/deactivate
     *
     * @param id the customer ID
     * @return the updated customer
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Customer> deactivate(@PathVariable Long id) {
        log.debug("REST request to deactivate customer with id: {}", id);
        Customer customer = service.deactivate(id);
        return ResponseEntity.ok(customer);
    }

    /**
     * Suspend a customer (change status to SUSPENDED).
     *
     * POST /api/customers/{id}/suspend
     *
     * @param id the customer ID
     * @return the updated customer
     */
    @PostMapping("/{id}/suspend")
    public ResponseEntity<Customer> suspend(@PathVariable Long id) {
        log.debug("REST request to suspend customer with id: {}", id);
        Customer customer = service.suspend(id);
        return ResponseEntity.ok(customer);
    }

    /**
     * Get customer statistics.
     *
     * GET /api/customers/statistics
     *
     * @return customer statistics (total, active, inactive, suspended counts)
     */
    @GetMapping("/statistics")
    public ResponseEntity<CustomerService.CustomerStats> getStatistics() {
        log.debug("REST request to get customer statistics");
        CustomerService.CustomerStats stats = service.getStatistics();
        return ResponseEntity.ok(stats);
    }
}
