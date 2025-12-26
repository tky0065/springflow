package io.springflow.demo.repository;

import io.springflow.demo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Custom repository for Order entity.
 * Demonstrates SpringFlow's ability to detect and skip repository generation
 * when a custom repository is provided.
 *
 * This repository adds custom query methods beyond standard CRUD operations.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    /**
     * Find order by order number (unique identifier).
     *
     * @param orderNumber the order number
     * @return optional containing the order if found
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders within a date range.
     * Custom query method for reporting and analytics.
     *
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return list of orders in the date range
     */
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find orders by status.
     *
     * @param status the order status (PENDING, CONFIRMED, SHIPPED, etc.)
     * @return list of orders with the given status
     */
    List<Order> findByStatus(String status);

    /**
     * Find orders with total amount greater than or equal to a minimum value.
     * Useful for identifying high-value orders.
     *
     * @param minAmount the minimum total amount
     * @return list of orders meeting the criteria
     */
    List<Order> findByTotalAmountGreaterThanEqual(BigDecimal minAmount);

    /**
     * Find orders by status and date range.
     * Complex query combining multiple criteria.
     *
     * @param status the order status
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return list of orders matching the criteria
     */
    List<Order> findByStatusAndOrderDateBetween(String status, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Calculate total revenue for orders in a given status.
     * Custom JPQL query for business analytics.
     *
     * @param status the order status
     * @return total revenue (sum of totalAmount), or 0 if no orders
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal calculateTotalRevenueByStatus(@Param("status") String status);

    /**
     * Count orders by status.
     *
     * @param status the order status
     * @return number of orders with the given status
     */
    long countByStatus(String status);

    /**
     * Find recent orders (last N days).
     * Custom JPQL query with dynamic date calculation.
     *
     * @param daysAgo number of days to look back
     * @return list of recent orders
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :cutoffDate ORDER BY o.orderDate DESC")
    List<Order> findRecentOrders(@Param("cutoffDate") LocalDateTime cutoffDate);
}
