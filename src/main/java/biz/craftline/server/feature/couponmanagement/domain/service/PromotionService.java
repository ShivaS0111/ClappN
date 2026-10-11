package biz.craftline.server.feature.couponmanagement.domain.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
import biz.craftline.server.feature.couponmanagement.api.mapper.PromotionMapper;
import biz.craftline.server.feature.couponmanagement.application.service.PromotionApplyResult;
import biz.craftline.server.feature.couponmanagement.application.service.PromotionEvaluator;
import biz.craftline.server.feature.couponmanagement.application.service.PromotionOrderContext;
import biz.craftline.server.feature.couponmanagement.domain.model.Promotion;
import biz.craftline.server.feature.couponmanagement.domain.model.PromotionApplicationLevel;
import biz.craftline.server.feature.couponmanagement.domain.model.PromotionBenefitType;
import biz.craftline.server.feature.couponmanagement.domain.model.PromotionStatus;
import biz.craftline.server.feature.couponmanagement.domain.model.PromotionTarget;
import biz.craftline.server.feature.couponmanagement.domain.model.PromotionTargetType;
import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionEntity;
import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionRedemptionEntity;
import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionUsageStatsEntity;
import biz.craftline.server.feature.couponmanagement.infra.repository.PromotionRedemptionRepository;
import biz.craftline.server.feature.couponmanagement.infra.repository.PromotionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionRedemptionRepository redemptionRepository;
    private final StoreRepository storeRepository;
    private final SecurityContextService securityContextService;
    private final PromotionEvaluator promotionEvaluator;

    @Transactional(readOnly = true)
    public List<Promotion> listByBusiness(Long businessId) {
        securityContextService.validateBusinessAccess(businessId);
        return promotionRepository.findByBusinessId(businessId).stream()
                .map(PromotionMapper::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Promotion> listByStore(Long storeId) {
        securityContextService.validateStoreAccess(storeId);
        return promotionRepository.findByStoreId(storeId).stream()
                .map(PromotionMapper::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Promotion getById(Long id) {
        PromotionEntity entity = require(id);
        assertCanAccess(entity);
        return PromotionMapper.fromEntity(entity);
    }

    @Transactional
    public Promotion create(Promotion request) {
        validateRequest(request, true);
        assertCreateAccess(request);
        if (request.getCode() != null && !request.getCode().isBlank()) {
            String code = request.getCode().trim().toUpperCase(Locale.ROOT);
            promotionRepository.findByBusinessIdAndCodeIgnoreCase(request.getBusinessId(), code)
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Coupon code already exists for this business: " + code);
                    });
            request.setCode(code);
        } else {
            request.setCode(null);
        }

        PromotionEntity entity = PromotionEntity.builder()
                .name(request.getName().trim())
                .code(request.getCode())
                .benefitType(PromotionBenefitType.from(request.getBenefitType()).name())
                .applicationLevel(PromotionApplicationLevel.from(request.getApplicationLevel()).name())
                .benefitValue(request.getBenefitValue())
                .businessId(request.getBusinessId())
                .storeId(request.getStoreId())
                .minCartAmount(request.getMinCartAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .maxGlobalRedemptions(request.getMaxGlobalRedemptions())
                .maxPerUser(request.getMaxPerUser())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .status(PromotionStatus.from(request.getStatus()).name())
                .priority(request.getPriority() != null ? request.getPriority() : 100)
                .targets(new ArrayList<>())
                .build();
        PromotionMapper.replaceTargets(entity, normalizeTargets(request.getTargets()));

        PromotionUsageStatsEntity stats = PromotionUsageStatsEntity.builder()
                .promotion(entity)
                .redemptionCount(0L)
                .totalDiscountGiven(BigDecimal.ZERO)
                .build();
        entity.setUsageStats(stats);

        return PromotionMapper.fromEntity(promotionRepository.save(entity));
    }

    @Transactional
    public Promotion update(Long id, Promotion updates) {
        PromotionEntity entity = require(id);
        assertCanAccess(entity);
        validateRequest(updates, false);

        if (updates.getName() != null && !updates.getName().isBlank()) {
            entity.setName(updates.getName().trim());
        }
        if (updates.getBenefitType() != null) {
            entity.setBenefitType(PromotionBenefitType.from(updates.getBenefitType()).name());
        }
        if (updates.getApplicationLevel() != null) {
            entity.setApplicationLevel(PromotionApplicationLevel.from(updates.getApplicationLevel()).name());
        }
        if (updates.getBenefitValue() != null) {
            entity.setBenefitValue(updates.getBenefitValue());
        }
        if (updates.getStoreId() != null) {
            securityContextService.validateStoreAccess(updates.getStoreId());
            entity.setStoreId(updates.getStoreId());
        }
        if (updates.getMinCartAmount() != null) {
            entity.setMinCartAmount(updates.getMinCartAmount());
        }
        if (updates.getMaxDiscountAmount() != null) {
            entity.setMaxDiscountAmount(updates.getMaxDiscountAmount());
        }
        if (updates.getMaxGlobalRedemptions() != null) {
            entity.setMaxGlobalRedemptions(updates.getMaxGlobalRedemptions());
        }
        if (updates.getMaxPerUser() != null) {
            entity.setMaxPerUser(updates.getMaxPerUser());
        }
        if (updates.getStartAt() != null) {
            entity.setStartAt(updates.getStartAt());
        }
        if (updates.getEndAt() != null) {
            entity.setEndAt(updates.getEndAt());
        }
        if (updates.getStatus() != null) {
            entity.setStatus(PromotionStatus.from(updates.getStatus()).name());
        }
        if (updates.getPriority() != null) {
            entity.setPriority(updates.getPriority());
        }
        if (updates.getCode() != null) {
            String code = updates.getCode().isBlank() ? null : updates.getCode().trim().toUpperCase(Locale.ROOT);
            if (code != null) {
                promotionRepository.findByBusinessIdAndCodeIgnoreCase(entity.getBusinessId(), code)
                        .filter(other -> !Objects.equals(other.getId(), entity.getId()))
                        .ifPresent(other -> {
                            throw new IllegalArgumentException("Coupon code already exists for this business: " + code);
                        });
            }
            entity.setCode(code);
        }
        if (updates.getTargets() != null) {
            PromotionMapper.replaceTargets(entity, normalizeTargets(updates.getTargets()));
        }
        return PromotionMapper.fromEntity(promotionRepository.save(entity));
    }

    @Transactional
    public Promotion setStatus(Long id, String status) {
        PromotionEntity entity = require(id);
        assertCanAccess(entity);
        entity.setStatus(PromotionStatus.from(status).name());
        return PromotionMapper.fromEntity(promotionRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public PromotionApplyResult preview(PromotionOrderContext ctx) {
        resolveBusinessIfNeeded(ctx);
        securityContextService.validateStoreAccess(ctx.getStoreId());
        return promotionEvaluator.evaluate(ctx, true);
    }

    /**
     * Evaluate discounts for place-order. Mutates context line discounts.
     * When couponCode is blank, auto promos only (no error).
     */
    @Transactional(readOnly = true)
    public PromotionApplyResult evaluateForOrder(PromotionOrderContext ctx) {
        resolveBusinessIfNeeded(ctx);
        boolean requireCoupon = ctx.getCouponCode() != null && !ctx.getCouponCode().isBlank();
        return promotionEvaluator.evaluate(ctx, requireCoupon);
    }

    @Transactional
    public void redeem(List<Long> promotionIds, Long orderId, Long userId, Long storeId, BigDecimal totalDiscount) {
        if (promotionIds == null || promotionIds.isEmpty()) {
            return;
        }
        BigDecimal share = totalDiscount != null ? totalDiscount : BigDecimal.ZERO;
        BigDecimal perPromo = promotionIds.size() == 1
                ? share
                : share.divide(BigDecimal.valueOf(promotionIds.size()), 4, java.math.RoundingMode.HALF_UP);
        for (Long promoId : promotionIds) {
            PromotionEntity entity = require(promoId);
            redemptionRepository.save(PromotionRedemptionEntity.builder()
                    .promotionId(promoId)
                    .orderId(orderId)
                    .userId(userId)
                    .storeId(storeId)
                    .discountAmount(perPromo)
                    .build());
            PromotionUsageStatsEntity stats = entity.getUsageStats();
            if (stats == null) {
                stats = PromotionUsageStatsEntity.builder()
                        .promotion(entity)
                        .redemptionCount(0L)
                        .totalDiscountGiven(BigDecimal.ZERO)
                        .build();
                entity.setUsageStats(stats);
            }
            stats.setRedemptionCount(stats.getRedemptionCount() + 1);
            stats.setTotalDiscountGiven(stats.getTotalDiscountGiven().add(perPromo));
            promotionRepository.save(entity);
        }
    }

    public Long resolveBusinessId(Long storeId) {
        return storeRepository.findById(storeId)
                .map(StoreEntity::getBusiness)
                .map(b -> b != null ? b.getId() : null)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));
    }

    private void resolveBusinessIfNeeded(PromotionOrderContext ctx) {
        if (ctx.getBusinessId() == null) {
            ctx.setBusinessId(resolveBusinessId(ctx.getStoreId()));
        }
    }

    private void validateRequest(Promotion request, boolean creating) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (creating) {
            if (request.getName() == null || request.getName().isBlank()) {
                throw new IllegalArgumentException("name is required");
            }
            if (request.getBusinessId() == null) {
                throw new IllegalArgumentException("businessId is required");
            }
            if (request.getBenefitValue() == null || request.getBenefitValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("benefitValue must be positive");
            }
            PromotionBenefitType.from(request.getBenefitType());
            PromotionApplicationLevel.from(request.getApplicationLevel());
        } else if (request.getBenefitType() != null) {
            PromotionBenefitType.from(request.getBenefitType());
        }
        if (request.getApplicationLevel() != null) {
            PromotionApplicationLevel.from(request.getApplicationLevel());
        }
        if (request.getBenefitType() != null
                && PromotionBenefitType.from(request.getBenefitType()) == PromotionBenefitType.PERCENT_OFF
                && request.getBenefitValue() != null
                && request.getBenefitValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("PERCENT_OFF benefitValue cannot exceed 100");
        }
    }

    private List<PromotionTarget> normalizeTargets(List<PromotionTarget> targets) {
        if (targets == null) {
            return List.of();
        }
        List<PromotionTarget> out = new ArrayList<>();
        for (PromotionTarget t : targets) {
            if (t.getTargetId() == null) {
                throw new IllegalArgumentException("targetId is required");
            }
            PromotionTargetType type = PromotionTargetType.from(t.getTargetType());
            PromotionTarget normalized = new PromotionTarget();
            normalized.setTargetType(type.name());
            normalized.setTargetId(t.getTargetId());
            out.add(normalized);
        }
        return out;
    }

    private void assertCreateAccess(Promotion request) {
        securityContextService.validateBusinessAccess(request.getBusinessId());
        if (request.getStoreId() != null) {
            securityContextService.validateStoreAccess(request.getStoreId());
        }
    }

    private void assertCanAccess(PromotionEntity entity) {
        if (securityContextService.isSystemAdmin()) {
            return;
        }
        securityContextService.validateBusinessAccess(entity.getBusinessId());
        if (entity.getStoreId() != null) {
            try {
                securityContextService.validateStoreAccess(entity.getStoreId());
            } catch (AccessDeniedException ex) {
                throw new AccessDeniedException("You do not have access to promotion: " + entity.getId());
            }
        }
    }

    private PromotionEntity require(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Promotion not found: " + id));
    }
}
