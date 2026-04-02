package biz.craftline.server.feature.invoicemanagement.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.config.security.SecurityContextService;

import biz.craftline.server.feature.invoicemanagement.api.dto.InvoiceRequestDTO;
import biz.craftline.server.feature.invoicemanagement.domain.model.Invoice;
import biz.craftline.server.feature.invoicemanagement.infra.entity.InvoiceEntity;
import biz.craftline.server.feature.invoicemanagement.infra.mapper.InvoiceEntityMapper;
import biz.craftline.server.feature.invoicemanagement.infra.repository.InvoiceItemRepository;
import biz.craftline.server.feature.invoicemanagement.infra.repository.InvoiceRepository;
import biz.craftline.server.util.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final biz.craftline.server.feature.invoicemanagement.application.service.InvoiceServiceImpl invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceEntityMapper invoiceEntityMapper;
    private final SecurityContextService securityContextService;

    @PostMapping("/generate")
    @Operation(summary = "Generate invoice for an order")
    @RequirePermission("invoice.create")
    public ResponseEntity<APIResponse<Invoice>> generateInvoice(@RequestBody InvoiceRequestDTO request) {
        Invoice invoice = invoiceService.generate(request.getOrderId(), request.getStoreId());
        return APIResponse.success(invoice, "Invoice generated successfully");
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get invoice by order ID")
    @RequirePermission("invoice.read")
    public ResponseEntity<APIResponse<Invoice>> getByOrder(@PathVariable Long orderId) {
        return invoiceService.findByOrderId(orderId)
                .map(inv -> APIResponse.success(inv, "Invoice retrieved successfully"))
                .orElse(APIResponse.error("Invoice not found for order " + orderId, HttpStatus.NOT_FOUND));
    }

    @GetMapping
    @Operation(summary = "Get all invoices")
    @RequirePermission("invoice.read")
    public ResponseEntity<APIResponse<List<Invoice>>> getAllInvoices() {
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        List<InvoiceEntity> entities;
        if (accessibleStoreIds == null) {
            entities = invoiceRepository.findAll();
        } else if (accessibleStoreIds.isEmpty()) {
            return APIResponse.success(List.of(), "Invoices retrieved successfully");
        } else {
            entities = invoiceRepository.findByStoreIdIn(accessibleStoreIds);
        }
        List<Invoice> invoices = entities.stream()
                .map(e -> invoiceEntityMapper.toDomain(e, invoiceItemRepository.findByInvoiceId(e.getId())))
                .collect(Collectors.toList());
        return APIResponse.success(invoices, "Invoices retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID")
    @RequirePermission("invoice.read")
    public ResponseEntity<APIResponse<Invoice>> getById(@PathVariable Long id) {
        return invoiceRepository.findById(id)
                .map(e -> APIResponse.success(
                        invoiceEntityMapper.toDomain(e, invoiceItemRepository.findByInvoiceId(e.getId())),
                        "Invoice retrieved successfully"))
                .orElse(APIResponse.error("Invoice not found", HttpStatus.NOT_FOUND));
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get invoices by store ID")
    @RequirePermission("invoice.read")
    public ResponseEntity<APIResponse<List<Invoice>>> getByStore(@PathVariable Long storeId) {
        securityContextService.validateStoreAccess(storeId);
        List<Invoice> invoices = invoiceRepository.findByStoreId(storeId).stream()
                .map(e -> invoiceEntityMapper.toDomain(e, invoiceItemRepository.findByInvoiceId(e.getId())))
                .collect(Collectors.toList());
        return APIResponse.success(invoices, "Invoices retrieved successfully");
    }
}
