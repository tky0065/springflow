package io.springflow.core.scanner.testentities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Test entity WITHOUT @AutoApi annotation.
 * Should NOT be picked up by EntityScanner.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    private Long id;

    private String orderNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}

