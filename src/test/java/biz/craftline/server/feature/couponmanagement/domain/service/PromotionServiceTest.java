package biz.craftline.server.feature.couponmanagement.domain.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
import biz.craftline.server.feature.couponmanagement.application.service.PromotionEvaluator;
import biz.craftline.server.feature.couponmanagement.domain.model.Promotion;
import biz.craftline.server.feature.couponmanagement.domain.model.PromotionTarget;
import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionEntity;
import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionUsageStatsEntity;
import biz.craftline.server.feature.couponmanagement.infra.repository.PromotionRedemptionRepository;
import biz.craftline.server.feature.couponmanagement.infra.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock PromotionRepository promotionRepository;
    @Mock PromotionRedemptionRepository redemptionRepository;
    @Mock StoreRepository storeRepository;
    @Mock SecurityContextService securityContextService;
    @Mock PromotionEvaluator promotionEvaluator;

    @InjectMocks PromotionService promotionService;

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(securityContextService).validateBusinessAccess(anyLong());
        lenient().doNothing().when(securityContextService).validateStoreAccess(anyLong());
        lenient().when(securityContextService.isSystemAdmin()).thenReturn(true);
    }

    @Test
    void create_persistsUsageStatsAndTargets() {
        Promotion req = new Promotion();
        req.setName("Save 10");
        req.setCode("save10");
        req.setBenefitType("PERCENT_OFF");
        req.setApplicationLevel("LINE");
        req.setBenefitValue(new BigDecimal("10"));
        req.setBusinessId(2L);
        req.setStoreId(1L);
        PromotionTarget user = new PromotionTarget();
        user.setTargetType("USER");
        user.setTargetId(9L);
        req.setTargets(List.of(user));

        when(promotionRepository.findByBusinessIdAndCodeIgnoreCase(2L, "SAVE10")).thenReturn(Optional.empty());
        when(promotionRepository.save(any(PromotionEntity.class))).thenAnswer(inv -> {
            PromotionEntity e = inv.getArgument(0);
            e.setId(50L);
            return e;
        });

        Promotion created = promotionService.create(req);
        assertEquals(50L, created.getId());
        assertEquals("SAVE10", created.getCode());
        assertEquals(1, created.getTargets().size());
        assertEquals(0L, created.getRedemptionCount());

        ArgumentCaptor<PromotionEntity> cap = ArgumentCaptor.forClass(PromotionEntity.class);
        verify(promotionRepository).save(cap.capture());
        assertNotNull(cap.getValue().getUsageStats());
        assertEquals("ACTIVE".equals(cap.getValue().getStatus()) || "DRAFT".equals(cap.getValue().getStatus()), true);
    }

    @Test
    void create_rejectsDuplicateCode() {
        Promotion req = new Promotion();
        req.setName("Dup");
        req.setCode("SAVE10");
        req.setBenefitType("FIXED_OFF");
        req.setApplicationLevel("CART");
        req.setBenefitValue(BigDecimal.TEN);
        req.setBusinessId(2L);
        when(promotionRepository.findByBusinessIdAndCodeIgnoreCase(2L, "SAVE10"))
                .thenReturn(Optional.of(PromotionEntity.builder().id(1L).build()));
        assertThrows(IllegalArgumentException.class, () -> promotionService.create(req));
    }

    @Test
    void redeem_incrementsUsage() {
        PromotionEntity entity = PromotionEntity.builder()
                .id(7L)
                .name("P")
                .benefitType("FIXED_OFF")
                .applicationLevel("CART")
                .benefitValue(BigDecimal.TEN)
                .businessId(2L)
                .status("ACTIVE")
                .targets(new ArrayList<>())
                .build();
        PromotionUsageStatsEntity stats = PromotionUsageStatsEntity.builder()
                .promotion(entity)
                .redemptionCount(1L)
                .totalDiscountGiven(new BigDecimal("10"))
                .build();
        entity.setUsageStats(stats);
        when(promotionRepository.findById(7L)).thenReturn(Optional.of(entity));
        when(promotionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(redemptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        promotionService.redeem(List.of(7L), 100L, 9L, 1L, new BigDecimal("25"));

        assertEquals(2L, stats.getRedemptionCount());
        assertEquals(0, new BigDecimal("35").compareTo(stats.getTotalDiscountGiven()));
        verify(redemptionRepository).save(any());
    }

    @Test
    void setStatus_activate() {
        PromotionEntity entity = PromotionEntity.builder()
                .id(3L)
                .name("P")
                .benefitType("PERCENT_OFF")
                .applicationLevel("LINE")
                .benefitValue(BigDecimal.TEN)
                .businessId(2L)
                .status("DRAFT")
                .targets(new ArrayList<>())
                .usageStats(PromotionUsageStatsEntity.builder().redemptionCount(0L).totalDiscountGiven(BigDecimal.ZERO).build())
                .build();
        entity.getUsageStats().setPromotion(entity);
        when(promotionRepository.findById(3L)).thenReturn(Optional.of(entity));
        when(promotionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Promotion updated = promotionService.setStatus(3L, "ACTIVE");
        assertEquals("ACTIVE", updated.getStatus());
    }
}
