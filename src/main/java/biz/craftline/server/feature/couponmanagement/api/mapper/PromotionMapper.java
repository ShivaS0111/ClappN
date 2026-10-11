package biz.craftline.server.feature.couponmanagement.api.mapper;

import biz.craftline.server.feature.couponmanagement.api.dto.PromotionRequest;
import biz.craftline.server.feature.couponmanagement.api.dto.PromotionResponse;
import biz.craftline.server.feature.couponmanagement.api.dto.PromotionTargetDTO;
import biz.craftline.server.feature.couponmanagement.api.dto.PromotionUsageResponse;
import biz.craftline.server.feature.couponmanagement.domain.model.Promotion;
import biz.craftline.server.feature.couponmanagement.domain.model.PromotionTarget;
import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionEntity;
import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionTargetEntity;
import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionUsageStatsEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class PromotionMapper {

    private PromotionMapper() {}

    public static Promotion fromEntity(PromotionEntity e) {
        Promotion p = new Promotion();
        p.setId(e.getId());
        p.setName(e.getName());
        p.setCode(e.getCode());
        p.setBenefitType(e.getBenefitType());
        p.setApplicationLevel(e.getApplicationLevel());
        p.setBenefitValue(e.getBenefitValue());
        p.setBusinessId(e.getBusinessId());
        p.setStoreId(e.getStoreId());
        p.setMinCartAmount(e.getMinCartAmount());
        p.setMaxDiscountAmount(e.getMaxDiscountAmount());
        p.setMaxGlobalRedemptions(e.getMaxGlobalRedemptions());
        p.setMaxPerUser(e.getMaxPerUser());
        p.setStartAt(e.getStartAt());
        p.setEndAt(e.getEndAt());
        p.setStatus(e.getStatus());
        p.setPriority(e.getPriority());
        if (e.getTargets() != null) {
            p.setTargets(e.getTargets().stream().map(t -> {
                PromotionTarget pt = new PromotionTarget();
                pt.setTargetType(t.getTargetType());
                pt.setTargetId(t.getTargetId());
                return pt;
            }).collect(Collectors.toList()));
        }
        PromotionUsageStatsEntity stats = e.getUsageStats();
        if (stats != null) {
            p.setRedemptionCount(stats.getRedemptionCount());
            p.setTotalDiscountGiven(stats.getTotalDiscountGiven());
        } else {
            p.setRedemptionCount(0L);
            p.setTotalDiscountGiven(BigDecimal.ZERO);
        }
        return p;
    }

    public static Promotion toDomain(PromotionRequest req) {
        Promotion p = new Promotion();
        p.setName(req.getName());
        p.setCode(req.getCode());
        p.setBenefitType(req.getBenefitType());
        p.setApplicationLevel(req.getApplicationLevel());
        p.setBenefitValue(req.getBenefitValue());
        p.setBusinessId(req.getBusinessId());
        p.setStoreId(req.getStoreId());
        p.setMinCartAmount(req.getMinCartAmount());
        p.setMaxDiscountAmount(req.getMaxDiscountAmount());
        p.setMaxGlobalRedemptions(req.getMaxGlobalRedemptions());
        p.setMaxPerUser(req.getMaxPerUser());
        p.setStartAt(req.getStartAt());
        p.setEndAt(req.getEndAt());
        p.setStatus(req.getStatus());
        p.setPriority(req.getPriority());
        if (req.getTargets() != null) {
            List<PromotionTarget> targets = new ArrayList<>();
            for (PromotionTargetDTO dto : req.getTargets()) {
                PromotionTarget t = new PromotionTarget();
                t.setTargetType(dto.getTargetType());
                t.setTargetId(dto.getTargetId());
                targets.add(t);
            }
            p.setTargets(targets);
        }
        return p;
    }

    public static PromotionResponse toResponse(Promotion p) {
        PromotionResponse r = new PromotionResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setCode(p.getCode());
        r.setBenefitType(p.getBenefitType());
        r.setApplicationLevel(p.getApplicationLevel());
        r.setBenefitValue(p.getBenefitValue());
        r.setBusinessId(p.getBusinessId());
        r.setStoreId(p.getStoreId());
        r.setMinCartAmount(p.getMinCartAmount());
        r.setMaxDiscountAmount(p.getMaxDiscountAmount());
        r.setMaxGlobalRedemptions(p.getMaxGlobalRedemptions());
        r.setMaxPerUser(p.getMaxPerUser());
        r.setStartAt(p.getStartAt());
        r.setEndAt(p.getEndAt());
        r.setStatus(p.getStatus());
        r.setPriority(p.getPriority());
        r.setRedemptionCount(p.getRedemptionCount());
        r.setTotalDiscountGiven(p.getTotalDiscountGiven());
        if (p.getTargets() != null) {
            r.setTargets(p.getTargets().stream().map(t -> {
                PromotionTargetDTO dto = new PromotionTargetDTO();
                dto.setTargetType(t.getTargetType());
                dto.setTargetId(t.getTargetId());
                return dto;
            }).collect(Collectors.toList()));
        }
        return r;
    }

    public static PromotionUsageResponse toUsage(Promotion p) {
        PromotionUsageResponse u = new PromotionUsageResponse();
        u.setPromotionId(p.getId());
        u.setName(p.getName());
        u.setCode(p.getCode());
        u.setBenefitType(p.getBenefitType());
        u.setApplicationLevel(p.getApplicationLevel());
        u.setConfiguredBenefitValue(p.getBenefitValue());
        u.setMaxDiscountAmount(p.getMaxDiscountAmount());
        u.setMaxGlobalRedemptions(p.getMaxGlobalRedemptions());
        u.setMaxPerUser(p.getMaxPerUser());
        u.setStatus(p.getStatus());
        long used = p.getRedemptionCount() != null ? p.getRedemptionCount() : 0L;
        BigDecimal given = p.getTotalDiscountGiven() != null ? p.getTotalDiscountGiven() : BigDecimal.ZERO;
        u.setRedemptionCount(used);
        u.setTotalDiscountGiven(given);
        if (p.getMaxGlobalRedemptions() != null) {
            u.setRemainingRedemptions(Math.max(0, p.getMaxGlobalRedemptions() - used));
        }
        // maxDiscountAmount is a per-order cap, not a lifetime budget — remainingBudget left null in MVP
        return u;
    }

    public static void replaceTargets(PromotionEntity entity, List<PromotionTarget> targets) {
        entity.getTargets().clear();
        if (targets == null) {
            return;
        }
        for (PromotionTarget t : targets) {
            PromotionTargetEntity te = PromotionTargetEntity.builder()
                    .promotion(entity)
                    .targetType(t.getTargetType())
                    .targetId(t.getTargetId())
                    .build();
            entity.getTargets().add(te);
        }
    }
}
