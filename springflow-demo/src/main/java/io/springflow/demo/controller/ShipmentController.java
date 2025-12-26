package io.springflow.demo.controller;

import io.springflow.core.controller.GenericCrudController;
import io.springflow.core.filter.FilterResolver;
import io.springflow.core.mapper.DtoMapper;
import io.springflow.core.mapper.DtoMapperFactory;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.MetadataResolver;
import io.springflow.core.service.GenericCrudService;
import io.springflow.demo.entity.Shipment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Custom controller for Shipment entity that extends GenericCrudController.
 * Demonstrates SpringFlow's ability to detect and skip controller generation
 * when a custom controller is provided.
 *
 * This controller:
 * - Extends GenericCrudController to inherit standard REST CRUD endpoints
 * - Adds custom endpoints for shipment-specific operations
 */
@RestController
@RequestMapping("/api/shipments")
public class ShipmentController extends GenericCrudController<Shipment, Long> {

    private static final Logger log = LoggerFactory.getLogger(ShipmentController.class);

    public ShipmentController(
            @Qualifier("shipmentService") GenericCrudService<Shipment, Long> service,
            DtoMapperFactory dtoMapperFactory,
            FilterResolver filterResolver
    ) {
        super(service,
              dtoMapperFactory.getMapper(Shipment.class, new MetadataResolver().resolve(Shipment.class)),
              filterResolver,
              new MetadataResolver().resolve(Shipment.class),
              Shipment.class);
    }

    @Override
    protected Long getEntityId(Shipment entity) {
        return entity.getId();
    }

    /**
     * Custom endpoint: Update shipment status.
     * Allows updating only the status field without affecting other fields.
     *
     * POST /api/shipments/{id}/update-status?status=IN_TRANSIT
     *
     * @param id the shipment ID
     * @param status the new status
     * @return updated shipment as DTO
     */
    @PutMapping("/{id}/update-status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        log.debug("Updating status for shipment {} to {}", id, status);

        // Validate status
        validateStatus(status);

        Shipment shipment = service.findById(id);
        shipment.setStatus(status);

        // Set dates based on status
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case "IN_TRANSIT" -> {
                if (shipment.getShippedDate() == null) {
                    shipment.setShippedDate(now);
                }
            }
            case "OUT_FOR_DELIVERY" -> {
                if (shipment.getEstimatedDeliveryDate() == null) {
                    shipment.setEstimatedDeliveryDate(now.plusDays(1));
                }
            }
            case "DELIVERED" -> shipment.setActualDeliveryDate(now);
        }

        Shipment updated = service.save(shipment);

        return ResponseEntity.ok(dtoMapper.toOutputDto(updated));
    }

    /**
     * Custom endpoint: Ship a shipment (mark as IN_TRANSIT).
     * Sets shipped date and updates status.
     *
     * POST /api/shipments/{id}/ship?carrier=UPS
     *
     * @param id the shipment ID
     * @param carrier optional carrier name
     * @return updated shipment as DTO
     */
    @PostMapping("/{id}/ship")
    public ResponseEntity<Map<String, Object>> ship(
            @PathVariable Long id,
            @RequestParam(required = false) String carrier
    ) {
        log.debug("Shipping shipment {} with carrier {}", id, carrier);

        Shipment shipment = service.findById(id);

        if (!"PENDING".equals(shipment.getStatus())) {
            throw new IllegalStateException("Only pending shipments can be shipped");
        }

        shipment.setStatus("IN_TRANSIT");
        shipment.setShippedDate(LocalDateTime.now());

        if (carrier != null) {
            shipment.setCarrier(carrier);
        }

        Shipment updated = service.save(shipment);

        return ResponseEntity.ok(dtoMapper.toOutputDto(updated));
    }

    /**
     * Custom endpoint: Deliver a shipment (mark as DELIVERED).
     * Sets actual delivery date and updates status.
     *
     * POST /api/shipments/{id}/deliver
     *
     * @param id the shipment ID
     * @return updated shipment as DTO
     */
    @PostMapping("/{id}/deliver")
    public ResponseEntity<Map<String, Object>> deliver(@PathVariable Long id) {
        log.debug("Delivering shipment {}", id);

        Shipment shipment = service.findById(id);

        if ("PENDING".equals(shipment.getStatus())) {
            throw new IllegalStateException("Shipment must be shipped before being delivered");
        }

        if ("DELIVERED".equals(shipment.getStatus())) {
            throw new IllegalStateException("Shipment is already delivered");
        }

        shipment.setStatus("DELIVERED");
        shipment.setActualDeliveryDate(LocalDateTime.now());

        Shipment updated = service.save(shipment);

        return ResponseEntity.ok(dtoMapper.toOutputDto(updated));
    }

    /**
     * Custom endpoint: Return a shipment (mark as RETURNED).
     *
     * POST /api/shipments/{id}/return?reason=damaged
     *
     * @param id the shipment ID
     * @param reason optional return reason
     * @return updated shipment as DTO
     */
    @PostMapping("/{id}/return")
    public ResponseEntity<Map<String, Object>> returnShipment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        log.debug("Returning shipment {} with reason: {}", id, reason);

        Shipment shipment = service.findById(id);

        shipment.setStatus("RETURNED");

        if (reason != null) {
            String currentNotes = shipment.getNotes();
            String updatedNotes = (currentNotes != null ? currentNotes + " | " : "") +
                                  "Return reason: " + reason;
            shipment.setNotes(updatedNotes);
        }

        Shipment updated = service.save(shipment);

        return ResponseEntity.ok(dtoMapper.toOutputDto(updated));
    }

    /**
     * Custom endpoint: Get shipment tracking information.
     * Returns a simplified tracking view.
     *
     * GET /api/shipments/{id}/tracking
     *
     * @param id the shipment ID
     * @return tracking information
     */
    @GetMapping("/{id}/tracking")
    public ResponseEntity<TrackingInfo> getTracking(@PathVariable Long id) {
        log.debug("Getting tracking info for shipment {}", id);

        Shipment shipment = service.findById(id);

        TrackingInfo trackingInfo = new TrackingInfo(
                shipment.getTrackingNumber(),
                shipment.getStatus(),
                shipment.getCarrier(),
                shipment.getShippedDate(),
                shipment.getEstimatedDeliveryDate(),
                shipment.getActualDeliveryDate()
        );

        return ResponseEntity.ok(trackingInfo);
    }

    /**
     * Validate shipment status value.
     *
     * @param status the status to validate
     */
    private void validateStatus(String status) {
        String[] validStatuses = {"PENDING", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED", "RETURNED"};
        for (String validStatus : validStatuses) {
            if (validStatus.equals(status)) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + status +
                ". Valid values are: " + String.join(", ", validStatuses));
    }

    /**
     * Record for tracking information response.
     */
    public record TrackingInfo(
            String trackingNumber,
            String status,
            String carrier,
            LocalDateTime shippedDate,
            LocalDateTime estimatedDeliveryDate,
            LocalDateTime actualDeliveryDate
    ) {}
}
