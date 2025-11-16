package biz.craftline.server.feature.invoicemanagement.api.controller;


import biz.craftline.server.feature.invoicemanagement.api.dto.InvoiceRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final biz.craftline.server.feature.invoicemanagement.application.service.InvoiceServiceImpl invoiceService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateInvoice(@RequestBody InvoiceRequestDTO request) {
        return ResponseEntity.ok(invoiceService.generate(request.getOrderId(), request.getStoreId()));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(invoiceService.findByOrderId(orderId));
    }
}
