package io.springflow.demo.entity;

import io.springflow.annotations.AutoApi;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Shipment entity demonstrating custom controller usage.
 * SpringFlow will:
 * - GENERATE ShipmentRepository
 * - GENERATE ShipmentService
 * - SKIP controller generation (ShipmentController is custom, extends GenericCrudController)
 */
@Entity
@Table(name = "shipments")
@Data
@AutoApi(
    path = "/shipments",
    description = "Shipment tracking with custom controller for additional endpoints"
)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tracking number is required")
    @Column(nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @NotBlank(message = "Status is required")
    @Column(nullable = false, length = 30)
    private String status; // PENDING, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, RETURNED

    @Column
    private LocalDateTime shippedDate;

    @Column
    private LocalDateTime estimatedDeliveryDate;

    @Column
    private LocalDateTime actualDeliveryDate;

    @Column(length = 200)
    private String carrier; // UPS, FedEx, DHL, USPS, etc.

    @Column(length = 500)
    private String notes;

    public Shipment() {
        this.status = "PENDING";
    }

    public Shipment(String trackingNumber) {
        this();
        this.trackingNumber = trackingNumber;
    }

    public Shipment(String trackingNumber, String carrier) {
        this(trackingNumber);
        this.carrier = carrier;
    }
}
