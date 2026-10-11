package biz.craftline.server.feature.couponmanagement.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.feature.couponmanagement.api.dto.PromotionPreviewRequest;
import biz.craftline.server.feature.couponmanagement.api.dto.PromotionPreviewResponse;
import biz.craftline.server.feature.couponmanagement.api.dto.PromotionRequest;
import biz.craftline.server.feature.couponmanagement.api.dto.PromotionResponse;
import biz.craftline.server.feature.couponmanagement.api.dto.PromotionUsageResponse;
import biz.craftline.server.feature.couponmanagement.api.mapper.PromotionMapper;
import biz.craftline.server.feature.couponmanagement.application.service.PromotionApplyResult;
import biz.craftline.server.feature.couponmanagement.application.service.PromotionOrderContext;
import biz.craftline.server.feature.couponmanagement.domain.model.Promotion;
import biz.craftline.server.feature.couponmanagement.domain.service.PromotionService;
import biz.craftline.server.util.APIResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping("/business/{businessId}")
    @RequirePermission("coupon.read")
    public ResponseEntity<APIResponse<List<PromotionResponse>>> byBusiness(@PathVariable Long businessId) {
        List<PromotionResponse> list = promotionService.listByBusiness(businessId).stream()
                .map(PromotionMapper::toResponse)
                .toList();
        return APIResponse.ok(list);
    }

    @GetMapping("/store/{storeId}")
    @RequirePermission("coupon.read")
    public ResponseEntity<APIResponse<List<PromotionResponse>>> byStore(@PathVariable Long storeId) {
        List<PromotionResponse> list = promotionService.listByStore(storeId).stream()
                .map(PromotionMapper::toResponse)
                .toList();
        return APIResponse.ok(list);
    }

    @GetMapping("/{id}")
    @RequirePermission("coupon.read")
    public ResponseEntity<APIResponse<PromotionResponse>> get(@PathVariable Long id) {
        try {
            return APIResponse.ok(PromotionMapper.toResponse(promotionService.getById(id)));
        } catch (EntityNotFoundException e) {
            return APIResponse.error(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/usage")
    @RequirePermission("coupon.read")
    public ResponseEntity<APIResponse<PromotionUsageResponse>> usage(@PathVariable Long id) {
        try {
            return APIResponse.ok(PromotionMapper.toUsage(promotionService.getById(id)));
        } catch (EntityNotFoundException e) {
            return APIResponse.error(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @RequirePermission("coupon.create")
    public ResponseEntity<APIResponse<PromotionResponse>> create(@RequestBody PromotionRequest request) {
        try {
            Promotion created = promotionService.create(PromotionMapper.toDomain(request));
            return APIResponse.ok(PromotionMapper.toResponse(created));
        } catch (IllegalArgumentException e) {
            return APIResponse.badRequest(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @RequirePermission("coupon.update")
    public ResponseEntity<APIResponse<PromotionResponse>> update(
            @PathVariable Long id, @RequestBody PromotionRequest request) {
        try {
            Promotion updated = promotionService.update(id, PromotionMapper.toDomain(request));
            return APIResponse.ok(PromotionMapper.toResponse(updated));
        } catch (EntityNotFoundException e) {
            return APIResponse.error(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return APIResponse.badRequest(e.getMessage());
        }
    }

    @PostMapping("/{id}/activate")
    @RequirePermission("coupon.update")
    public ResponseEntity<APIResponse<PromotionResponse>> activate(@PathVariable Long id) {
        return setStatus(id, "ACTIVE");
    }

    @PostMapping("/{id}/pause")
    @RequirePermission("coupon.update")
    public ResponseEntity<APIResponse<PromotionResponse>> pause(@PathVariable Long id) {
        return setStatus(id, "PAUSED");
    }

    @PostMapping("/preview")
    @RequirePermission("coupon.read")
    public ResponseEntity<APIResponse<PromotionPreviewResponse>> preview(@RequestBody PromotionPreviewRequest request) {
        try {
            if (request.getStoreId() == null) {
                return APIResponse.badRequest("storeId is required");
            }
            if (request.getItems() == null || request.getItems().isEmpty()) {
                return APIResponse.badRequest("items are required");
            }
            List<PromotionOrderContext.Line> lines = new ArrayList<>();
            for (PromotionPreviewRequest.PreviewLine item : request.getItems()) {
                if (item.getUnitPrice() == null || item.getQuantity() <= 0) {
                    return APIResponse.badRequest("each item needs unitPrice and positive quantity");
                }
                lines.add(PromotionOrderContext.Line.builder()
                        .itemType(item.getItemType())
                        .itemId(item.getItemId())
                        .quantity(item.getQuantity())
                        .unitPrice(BigDecimal.valueOf(item.getUnitPrice()))
                        .discountAmount(BigDecimal.ZERO)
                        .build());
            }
            PromotionOrderContext ctx = PromotionOrderContext.builder()
                    .storeId(request.getStoreId())
                    .businessId(request.getBusinessId())
                    .userId(request.getUserId())
                    .couponCode(request.getCouponCode())
                    .lines(lines)
                    .build();
            PromotionApplyResult result = promotionService.preview(ctx);
            return APIResponse.ok(toPreviewResponse(result, lines));
        } catch (IllegalArgumentException e) {
            return APIResponse.badRequest(e.getMessage());
        }
    }

    private ResponseEntity<APIResponse<PromotionResponse>> setStatus(Long id, String status) {
        try {
            return APIResponse.ok(PromotionMapper.toResponse(promotionService.setStatus(id, status)));
        } catch (EntityNotFoundException e) {
            return APIResponse.error(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return APIResponse.badRequest(e.getMessage());
        }
    }

    private static PromotionPreviewResponse toPreviewResponse(
            PromotionApplyResult result, List<PromotionOrderContext.Line> lines) {
        PromotionPreviewResponse resp = new PromotionPreviewResponse();
        resp.setSubtotal(result.getSubtotal());
        resp.setTotalDiscount(result.getTotalDiscount());
        resp.setBillDiscount(result.getBillDiscount());
        resp.setBillDiscountType(result.getBillDiscountType());
        resp.setCouponCode(result.getCouponCode());
        resp.setPayable(result.getPayable());
        resp.setAppliedPromotionIds(result.getAppliedPromotionIds());
        List<PromotionPreviewResponse.LineDiscount> lineDiscounts = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            PromotionPreviewResponse.LineDiscount ld = new PromotionPreviewResponse.LineDiscount();
            ld.setItemId(lines.get(i).getItemId());
            ld.setItemType(lines.get(i).getItemType());
            ld.setDiscountAmount(i < result.getLineDiscounts().size()
                    ? result.getLineDiscounts().get(i) : BigDecimal.ZERO);
            lineDiscounts.add(ld);
        }
        resp.setLineDiscounts(lineDiscounts);
        return resp;
    }
}
