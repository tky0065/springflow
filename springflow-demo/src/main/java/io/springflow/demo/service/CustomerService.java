package io.springflow.demo.service;

import io.springflow.core.exception.EntityNotFoundException;
import io.springflow.demo.entity.Customer;
import io.springflow.demo.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Fully custom service for Customer entity.
 * Does NOT extend GenericCrudService - completely custom implementation.
 *
 * Demonstrates SpringFlow's ability to work with fully custom services
 * that don't follow the framework's patterns.
 */
@Service
@Transactional
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    /**
     * Find all customers.
     *
     * @return list of all customers
     */
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        log.debug("Finding all customers");
        return repository.findAll();
    }

    /**
     * Find customer by ID.
     *
     * @param id the customer ID
     * @return the customer
     * @throws EntityNotFoundException if customer not found
     */
    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        log.debug("Finding customer with id: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Customer.class, id));
    }

    /**
     * Find customer by customer code.
     *
     * @param customerCode the customer code (e.g., CUST-ABCD1234)
     * @return the customer
     * @throws EntityNotFoundException if customer not found
     */
    @Transactional(readOnly = true)
    public Customer findByCustomerCode(String customerCode) {
        log.debug("Finding customer with code: {}", customerCode);
        return repository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new RuntimeException("Customer not found with code: " + customerCode));
    }

    /**
     * Find customers by status.
     *
     * @param status the customer status
     * @return list of customers with the given status
     */
    @Transactional(readOnly = true)
    public List<Customer> findByStatus(String status) {
        log.debug("Finding customers with status: {}", status);
        return repository.findByStatus(status);
    }

    /**
     * Search customers by company name.
     *
     * @param keyword search keyword
     * @return list of matching customers
     */
    @Transactional(readOnly = true)
    public List<Customer> searchByCompanyName(String keyword) {
        log.debug("Searching customers by company name: {}", keyword);
        return repository.findByCompanyNameContainingIgnoreCase(keyword);
    }

    /**
     * Find all active customers, sorted by company name.
     *
     * @return list of active customers
     */
    @Transactional(readOnly = true)
    public List<Customer> findAllActiveCustomers() {
        log.debug("Finding all active customers");
        return repository.findAllActiveCustomersSorted();
    }

    /**
     * Create a new customer.
     * Custom business logic: auto-generate customer code if not provided.
     *
     * @param customer the customer to create
     * @return the created customer
     */
    public Customer create(Customer customer) {
        log.debug("Creating new customer: {}", customer.getCompanyName());

        // Validate required fields
        if (customer.getCompanyName() == null || customer.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Company name is required");
        }

        // Auto-generate customer code if not provided
        if (customer.getCustomerCode() == null || customer.getCustomerCode().isBlank()) {
            customer.setCustomerCode(generateCustomerCode());
        } else {
            // Check if code already exists
            if (repository.existsByCustomerCode(customer.getCustomerCode())) {
                throw new IllegalArgumentException("Customer code already exists: " + customer.getCustomerCode());
            }
        }

        // Check if email already exists
        if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
            if (repository.existsByEmail(customer.getEmail())) {
                throw new IllegalArgumentException("Email already registered: " + customer.getEmail());
            }
        }

        // Set default status if not provided
        if (customer.getStatus() == null || customer.getStatus().isBlank()) {
            customer.setStatus("ACTIVE");
        }

        Customer saved = repository.save(customer);
        log.info("Created customer with code: {} and id: {}", saved.getCustomerCode(), saved.getId());
        return saved;
    }

    /**
     * Update an existing customer.
     *
     * @param id the customer ID
     * @param customer the updated customer data
     * @return the updated customer
     */
    public Customer update(Long id, Customer customer) {
        log.debug("Updating customer with id: {}", id);

        Customer existing = findById(id);

        // Validate customer code is not changed
        if (customer.getCustomerCode() != null &&
            !customer.getCustomerCode().equals(existing.getCustomerCode())) {
            throw new IllegalArgumentException("Cannot change customer code");
        }

        // Check email uniqueness if changed
        if (customer.getEmail() != null &&
            !customer.getEmail().equals(existing.getEmail())) {
            if (repository.existsByEmail(customer.getEmail())) {
                throw new IllegalArgumentException("Email already registered: " + customer.getEmail());
            }
        }

        // Update fields
        if (customer.getCompanyName() != null) {
            existing.setCompanyName(customer.getCompanyName());
        }
        if (customer.getEmail() != null) {
            existing.setEmail(customer.getEmail());
        }
        if (customer.getPhone() != null) {
            existing.setPhone(customer.getPhone());
        }
        if (customer.getAddress() != null) {
            existing.setAddress(customer.getAddress());
        }
        if (customer.getCity() != null) {
            existing.setCity(customer.getCity());
        }
        if (customer.getCountry() != null) {
            existing.setCountry(customer.getCountry());
        }
        if (customer.getStatus() != null) {
            existing.setStatus(customer.getStatus());
        }

        Customer updated = repository.save(existing);
        log.info("Updated customer with id: {}", updated.getId());
        return updated;
    }

    /**
     * Delete a customer by ID.
     *
     * @param id the customer ID
     */
    public void deleteById(Long id) {
        log.debug("Deleting customer with id: {}", id);

        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(Customer.class, id);
        }

        repository.deleteById(id);
        log.info("Deleted customer with id: {}", id);
    }

    /**
     * Activate a customer (change status to ACTIVE).
     *
     * @param id the customer ID
     * @return the updated customer
     */
    public Customer activate(Long id) {
        Customer customer = findById(id);
        customer.setStatus("ACTIVE");
        return repository.save(customer);
    }

    /**
     * Deactivate a customer (change status to INACTIVE).
     *
     * @param id the customer ID
     * @return the updated customer
     */
    public Customer deactivate(Long id) {
        Customer customer = findById(id);
        customer.setStatus("INACTIVE");
        return repository.save(customer);
    }

    /**
     * Suspend a customer (change status to SUSPENDED).
     *
     * @param id the customer ID
     * @return the updated customer
     */
    public Customer suspend(Long id) {
        Customer customer = findById(id);
        customer.setStatus("SUSPENDED");
        return repository.save(customer);
    }

    /**
     * Get statistics about customers.
     *
     * @return customer statistics
     */
    @Transactional(readOnly = true)
    public CustomerStats getStatistics() {
        long total = repository.count();
        long active = repository.countByStatus("ACTIVE");
        long inactive = repository.countByStatus("INACTIVE");
        long suspended = repository.countByStatus("SUSPENDED");

        return new CustomerStats(total, active, inactive, suspended);
    }

    /**
     * Generate a unique customer code.
     * Format: CUST-{8 random uppercase alphanumeric characters}
     *
     * @return generated customer code
     */
    private String generateCustomerCode() {
        String code;
        do {
            code = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (repository.existsByCustomerCode(code));
        return code;
    }

    /**
     * Record class for customer statistics.
     */
    public record CustomerStats(
            long total,
            long active,
            long inactive,
            long suspended
    ) {}
}
