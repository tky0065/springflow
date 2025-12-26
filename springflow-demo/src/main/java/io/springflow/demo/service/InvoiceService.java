package io.springflow.demo.service;

import io.springflow.core.service.GenericCrudService;
import io.springflow.demo.entity.Invoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Custom service for Invoice entity that extends GenericCrudService.
 * Demonstrates SpringFlow's ability to detect and skip service generation
 * when a custom service is provided.
 *
 * This service:
 * - Extends GenericCrudService to inherit standard CRUD operations
 * - Overrides lifecycle hooks for custom validation and business logic
 * - Adds custom business methods
 */
@Service
public class InvoiceService extends GenericCrudService<Invoice, Long> {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    public InvoiceService(@Qualifier("invoiceRepository") JpaRepository<Invoice, Long> repository) {
        super(repository, Invoice.class);
    }

    /**
     * Hook: Custom validation before creating an invoice.
     * Validates business rules and auto-generates invoice number.
     */
    @Override
    protected void beforeCreate(Invoice invoice) {
        // Validation: Amount must be positive
        if (invoice.getAmount() == null || invoice.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invoice amount must be positive");
        }

        // Auto-generate invoice number if not provided
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber(generateInvoiceNumber());
        }

        // Set issue date if not provided
        if (invoice.getIssueDate() == null) {
            invoice.setIssueDate(LocalDateTime.now());
        }

        // Calculate due date (30 days from issue date)
        if (invoice.getDueDate() == null) {
            invoice.setDueDate(invoice.getIssueDate().plusDays(30));
        }

        // Set default status
        if (invoice.getStatus() == null || invoice.getStatus().isBlank()) {
            invoice.setStatus("DRAFT");
        }
    }

    /**
     * Hook: Log invoice creation.
     */
    @Override
    protected void afterCreate(Invoice invoice) {
        log.info("Invoice created: {} with number {} for amount {}",
                invoice.getId(), invoice.getInvoiceNumber(), invoice.getAmount());
    }

    /**
     * Hook: Validate invoice update.
     * Prevent changing invoice number or amount for issued invoices.
     */
    @Override
    protected void beforeUpdate(Invoice existing, Invoice updated) {
        // If invoice is already issued, prevent changing critical fields
        if ("ISSUED".equals(existing.getStatus()) || "PAID".equals(existing.getStatus())) {
            if (!existing.getInvoiceNumber().equals(updated.getInvoiceNumber())) {
                throw new IllegalStateException("Cannot change invoice number for issued invoices");
            }
            if (existing.getAmount().compareTo(updated.getAmount()) != 0) {
                throw new IllegalStateException("Cannot change amount for issued invoices");
            }
        }

        // Update the status transition logic
        validateStatusTransition(existing.getStatus(), updated.getStatus());
    }

    /**
     * Custom business method: Calculate total revenue from all invoices.
     *
     * @return total revenue (sum of all invoice amounts)
     */
    public BigDecimal getTotalRevenue() {
        return repository.findAll().stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Custom business method: Calculate revenue by status.
     *
     * @param status the invoice status
     * @return total revenue for invoices with the given status
     */
    public BigDecimal getRevenueByStatus(String status) {
        return repository.findAll().stream()
                .filter(invoice -> status.equals(invoice.getStatus()))
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Custom business method: Find overdue invoices.
     * Invoices with status != PAID and dueDate < now.
     *
     * @return list of overdue invoices
     */
    public List<Invoice> findOverdueInvoices() {
        LocalDateTime now = LocalDateTime.now();
        return repository.findAll().stream()
                .filter(invoice -> !"PAID".equals(invoice.getStatus()) &&
                                   !"CANCELLED".equals(invoice.getStatus()) &&
                                   invoice.getDueDate() != null &&
                                   invoice.getDueDate().isBefore(now))
                .toList();
    }

    /**
     * Custom business method: Issue an invoice (change status from DRAFT to ISSUED).
     *
     * @param id the invoice ID
     * @return the updated invoice
     */
    public Invoice issueInvoice(Long id) {
        Invoice invoice = findById(id);

        if (!"DRAFT".equals(invoice.getStatus())) {
            throw new IllegalStateException("Only draft invoices can be issued");
        }

        invoice.setStatus("ISSUED");
        invoice.setIssueDate(LocalDateTime.now());

        return repository.save(invoice);
    }

    /**
     * Custom business method: Mark invoice as paid.
     *
     * @param id the invoice ID
     * @return the updated invoice
     */
    public Invoice markAsPaid(Long id) {
        Invoice invoice = findById(id);

        if (!"ISSUED".equals(invoice.getStatus()) && !"OVERDUE".equals(invoice.getStatus())) {
            throw new IllegalStateException("Only issued or overdue invoices can be marked as paid");
        }

        invoice.setStatus("PAID");

        return repository.save(invoice);
    }

    /**
     * Generate a unique invoice number.
     * Format: INV-{timestamp}
     *
     * @return generated invoice number
     */
    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }

    /**
     * Validate invoice status transitions.
     * Business rule: DRAFT → ISSUED → PAID (or OVERDUE → PAID)
     *                DRAFT → CANCELLED, ISSUED → CANCELLED
     *
     * @param currentStatus current status
     * @param newStatus new status
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || currentStatus.equals(newStatus)) {
            return; // No change or initial state
        }

        boolean validTransition = switch (currentStatus) {
            case "DRAFT" -> "ISSUED".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "ISSUED" -> "PAID".equals(newStatus) || "OVERDUE".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "OVERDUE" -> "PAID".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "PAID", "CANCELLED" -> false; // Terminal states
            default -> true; // Allow unknown transitions
        };

        if (!validTransition) {
            throw new IllegalStateException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }
    }
}
