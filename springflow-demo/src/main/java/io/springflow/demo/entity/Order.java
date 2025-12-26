package io.springflow.demo.entity;

import io.springflow.annotations.AutoApi;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order entity demonstrating custom repository usage.
 * SpringFlow will:
 * - SKIP repository generation (OrderRepository is custom)
 * - GENERATE OrderService
 * - GENERATE OrderController
 */
@Entity
@Table(name = "orders")
@Data
@AutoApi(
    path = "/orders",
    description = "Order management with custom repository for complex queries"
)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Order number is required")
    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    @NotNull(message = "Order date is required")
    @Column(nullable = false)
    private LocalDateTime orderDate;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 20)
    private String status; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED

    @Column(length = 500)
    private String notes;

    public Order() {
        this.orderDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Order(String orderNumber, BigDecimal totalAmount) {
        this();
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
    }
}
