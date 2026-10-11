package biz.craftline.server.feature.couponmanagement.application.service;

import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionEntity;
import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionTargetEntity;
import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionUsageStatsEntity;
import biz.craftline.server.feature.couponmanagement.infra.repository.PromotionRedemptionRepository;
import biz.craftline.server.feature.couponmanagement.infra.repository.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionEvaluatorTest {

    @Mock PromotionRepository promotionRepository;
    @Mock PromotionRedemptionRepository redemptionRepository;
    @InjectMocks PromotionEvaluator evaluator;

    private PromotionEntity activePromo(Long id, String code, String level, String benefit, String value) {
        PromotionEntity p = PromotionEntity.builder()
                .id(id)
                .name("P" + id)
                .code(code)
                .applicationLevel(level)
                .benefitType(benefit)
                .benefitValue(new BigDecimal(value))
                .businessId(2L)
                .storeId(1L)
                .status("ACTIVE")
                .priority(100)
                .targets(new ArrayList<>())
                .usageStats(PromotionUsageStatsEntity.builder().redemptionCount(0L).totalDiscountGiven(BigDecimal.ZERO).build())
                .build();
        p.getUsageStats().setPromotion(p);
        return p;
    }

    private PromotionOrderContext ctx(Long userId, String code, PromotionOrderContext.Line... lines) {
        return PromotionOrderContext.builder()
                .businessId(2L)
                .storeId(1L)
                .userId(userId)
                .couponCode(code)
                .lines(new ArrayList<>(List.of(lines)))
                .build();
    }

    private PromotionOrderContext.Line line(long itemId, long type, int qty, String price) {
        return PromotionOrderContext.Line.builder()
                .itemId(itemId)
                .itemType(type)
                .quantity(qty)
                .unitPrice(new BigDecimal(price))
                .discountAmount(BigDecimal.ZERO)
                .build();
    }

    @Test
    void linePercent_appliesToAllMatchingItems() {
        PromotionEntity promo = activePromo(10L, "SAVE10", "LINE", "PERCENT_OFF", "10");
        when(promotionRepository.findActiveForStore(2L, 1L, "ACTIVE")).thenReturn(List.of(promo));

        PromotionApplyResult result = evaluator.evaluate(ctx(9L, "SAVE10",
                line(100L, 1L, 2, "50"),
                line(101L, 1L, 1, "100")), true);

        assertEquals(0, new BigDecimal("10.0000").compareTo(result.getLineDiscounts().get(0))); // 10% of 50*2
        assertEquals(0, new BigDecimal("10.0000").compareTo(result.getLineDiscounts().get(1))); // 10% of 100
        assertEquals(0, new BigDecimal("20.0000").compareTo(result.getTotalDiscount()));
        assertEquals(0, new BigDecimal("180.0000").compareTo(result.getPayable()));
    }

    @Test
    void cartFixed_requiresMinCart() {
        PromotionEntity promo = activePromo(11L, "FLAT100", "CART", "FIXED_OFF", "100");
        promo.setMinCartAmount(new BigDecimal("999"));
        when(promotionRepository.findActiveForStore(2L, 1L, "ACTIVE")).thenReturn(List.of(promo));

        assertThrows(IllegalArgumentException.class, () ->
                evaluator.evaluate(ctx(9L, "FLAT100", line(1L, 1L, 1, "100")), true));

        PromotionApplyResult ok = evaluator.evaluate(ctx(9L, "FLAT100", line(1L, 1L, 1, "1000")), true);
        assertEquals(0, new BigDecimal("100.0000").compareTo(ok.getBillDiscount()));
        assertEquals(0, new BigDecimal("900.0000").compareTo(ok.getPayable()));
    }

    @Test
    void userTarget_rejectsOtherUsers() {
        PromotionEntity promo = activePromo(12L, "VIP", "CART", "FIXED_OFF", "50");
        promo.getTargets().add(PromotionTargetEntity.builder()
                .promotion(promo).targetType("USER").targetId(9L).build());
        when(promotionRepository.findActiveForStore(2L, 1L, "ACTIVE")).thenReturn(List.of(promo));

        assertThrows(IllegalArgumentException.class, () ->
                evaluator.evaluate(ctx(99L, "VIP", line(1L, 1L, 1, "200")), true));

        PromotionApplyResult ok = evaluator.evaluate(ctx(9L, "VIP", line(1L, 1L, 1, "200")), true);
        assertEquals(0, new BigDecimal("50.0000").compareTo(ok.getBillDiscount()));
    }

    @Test
    void catalogTarget_onlyMatchingLines() {
        PromotionEntity promo = activePromo(13L, "P10", "LINE", "PERCENT_OFF", "10");
        promo.getTargets().add(PromotionTargetEntity.builder()
                .promotion(promo).targetType("PRODUCT").targetId(100L).build());
        when(promotionRepository.findActiveForStore(2L, 1L, "ACTIVE")).thenReturn(List.of(promo));

        PromotionApplyResult result = evaluator.evaluate(ctx(9L, "P10",
                line(100L, 1L, 1, "100"),
                line(200L, 1L, 1, "100")), true);

        assertEquals(0, new BigDecimal("10.0000").compareTo(result.getLineDiscounts().get(0)));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getLineDiscounts().get(1)));
    }

    @Test
    void invalidCoupon_throws() {
        when(promotionRepository.findActiveForStore(2L, 1L, "ACTIVE")).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () ->
                evaluator.evaluate(ctx(9L, "NOPE", line(1L, 1L, 1, "10")), true));
    }
}
