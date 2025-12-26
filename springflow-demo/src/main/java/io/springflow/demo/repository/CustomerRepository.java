package io.springflow.demo.repository;

import io.springflow.demo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Custom repository for Customer entity.
 * Part of the fully custom implementation (repository + service + controller).
 *
 * This demonstrates complete control over data access layer.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customer by unique customer code.
     *
     * @param customerCode the customer code (e.g., CUST-ABCD1234)
     * @return optional containing the customer if found
     */
    Optional<Customer> findByCustomerCode(String customerCode);

    /**
     * Find customer by email.
     *
     * @param email the customer email
     * @return optional containing the customer if found
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find customers by status.
     *
     * @param status the customer status (ACTIVE, INACTIVE, SUSPENDED)
     * @return list of customers with the given status
     */
    List<Customer> findByStatus(String status);

    /**
     * Find customers by company name containing a keyword (case-insensitive).
     *
     * @param keyword the search keyword
     * @return list of customers matching the search
     */
    List<Customer> findByCompanyNameContainingIgnoreCase(String keyword);

    /**
     * Find customers by country.
     *
     * @param country the country name
     * @return list of customers in the given country
     */
    List<Customer> findByCountry(String country);

    /**
     * Check if a customer code already exists.
     *
     * @param customerCode the customer code to check
     * @return true if exists, false otherwise
     */
    boolean existsByCustomerCode(String customerCode);

    /**
     * Check if an email is already registered.
     *
     * @param email the email to check
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Count customers by status.
     *
     * @param status the customer status
     * @return number of customers with the given status
     */
    long countByStatus(String status);

    /**
     * Find all active customers, ordered by company name.
     * Custom JPQL query for sorted results.
     *
     * @return list of active customers, sorted alphabetically
     */
    @Query("SELECT c FROM Customer c WHERE c.status = 'ACTIVE' ORDER BY c.companyName ASC")
    List<Customer> findAllActiveCustomersSorted();

    /**
     * Search customers by multiple criteria.
     * Complex custom query.
     *
     * @param keyword search term for company name
     * @param status customer status
     * @param country customer country
     * @return list of customers matching the criteria
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "(:keyword IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:country IS NULL OR c.country = :country)")
    List<Customer> searchCustomers(
        @Param("keyword") String keyword,
        @Param("status") String status,
        @Param("country") String country
    );
}
