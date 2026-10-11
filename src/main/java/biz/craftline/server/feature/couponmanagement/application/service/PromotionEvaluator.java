package biz.craftline.server.feature.couponmanagement.application.service;

import biz.craftline.server.feature.couponmanagement.domain.model.PromotionApplicationLevel;
import biz.craftline.server.feature.couponmanagement.domain.model.PromotionBenefitType;
import biz.craftline.server.feature.couponmanagement.domain.model.PromotionStatus;
import biz.craftline.server.feature.couponmanagement.domain.model.PromotionTargetType;
import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionEntity;
import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionTargetEntity;
import biz.craftline.server.feature.couponmanagement.infra.entity.PromotionUsageStatsEntity;
import biz.craftline.server.feature.couponmanagement.infra.repository.PromotionRedemptionRepository;
import biz.craftline.server.feature.couponmanagement.infra.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Selects and applies MVP promotions: one coupon + one automatic, LINE then CART.
 */
@Component
@RequiredArgsConstructor
public class PromotionEvaluator {

    /** Order itemType conventions used by placeOrder. */
    private static final long ORDER_TYPE_PRODUCT = 1L;
    private static final long ORDER_TYPE_SERVICE = 3L;
    private static final long ORDER_TYPE_PACKAGE = 4L;

    private final PromotionRepository promotionRepository;
    private final PromotionRedemptionRepository redemptionRepository;

    public PromotionApplyResult evaluate(PromotionOrderContext ctx, boolean requireCouponIfProvided) {
        if (ctx == null || ctx.getStoreId() == null || ctx.getBusinessId() == null) {
            throw new IllegalArgumentException("businessId and storeId are required for promotion evaluation");
        }
        if (ctx.getLines() == null || ctx.getLines().isEmpty()) {
            throw new IllegalArgumentException("items are required for promotion evaluation");
        }

        for (PromotionOrderContext.Line line : ctx.getLines()) {
            if (line.getDiscountAmount() == null) {
                line.setDiscountAmount(BigDecimal.ZERO);
            }
        }

        BigDecimal subtotal = computeSubtotal(ctx.getLines());
        List<PromotionEntity> candidates = promotionRepository
                .findActiveForStore(ctx.getBusinessId(), ctx.getStoreId(), PromotionStatus.ACTIVE.name());

        List<PromotionEntity> eligible = candidates.stream()
                .filter(p -> isInSchedule(p, LocalDateTime.now()))
                .filter(p -> matchesScopeTargets(p, ctx))
                .filter(p -> passesUsageCaps(p, ctx.getUserId()))
                .filter(p -> passesMinCart(p, subtotal))
                .collect(Collectors.toList());

        String couponCode = normalizeCode(ctx.getCouponCode());
        PromotionEntity matchedCoupon = null;
        if (couponCode != null) {
            matchedCoupon = eligible.stream()
                    .filter(p -> p.getCode() != null && couponCode.equalsIgnoreCase(p.getCode().trim()))
                    .findFirst()
                    .orElse(null);
            if (matchedCoupon == null && requireCouponIfProvided) {
                throw new IllegalArgumentException("Invalid or ineligible coupon code: " + couponCode);
            }
        }
        final PromotionEntity couponPromo = matchedCoupon;

        PromotionEntity autoPromo = eligible.stream()
                .filter(p -> p.getCode() == null || p.getCode().isBlank())
                .filter(p -> couponPromo == null || !Objects.equals(p.getId(), couponPromo.getId()))
                .max(Comparator.comparing((PromotionEntity p) -> p.getPriority() != null ? p.getPriority() : 0)
                        .thenComparing(PromotionEntity::getId))
                .orElse(null);

        List<PromotionEntity> selected = new ArrayList<>();
        if (couponPromo != null) {
            selected.add(couponPromo);
        }
        if (autoPromo != null) {
            selected.add(autoPromo);
        }
        selected.sort(Comparator
                .comparing((PromotionEntity p) -> PromotionApplicationLevel.from(p.getApplicationLevel())
                        == PromotionApplicationLevel.LINE ? 0 : 1)
                .thenComparing((PromotionEntity p) -> p.getPriority() != null ? -p.getPriority() : 0));

        BigDecimal billDiscount = BigDecimal.ZERO;
        String billDiscountType = null;
        List<Long> appliedIds = new ArrayList<>();

        for (PromotionEntity promo : selected) {
            BigDecimal applied = applyOne(promo, ctx, subtotal);
            if (applied.compareTo(BigDecimal.ZERO) > 0
                    || PromotionApplicationLevel.from(promo.getApplicationLevel()) == PromotionApplicationLevel.LINE) {
                appliedIds.add(promo.getId());
            }
            if (PromotionApplicationLevel.from(promo.getApplicationLevel()) == PromotionApplicationLevel.CART
                    && applied.compareTo(BigDecimal.ZERO) > 0) {
                billDiscount = billDiscount.add(applied);
                billDiscountType = promo.getBenefitType();
            }
        }

        BigDecimal lineDiscountTotal = ctx.getLines().stream()
                .map(PromotionOrderContext.Line::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscount = lineDiscountTotal.add(billDiscount);
        if (totalDiscount.compareTo(subtotal) > 0) {
            // Cap bill discount so payable is never negative
            BigDecimal overflow = totalDiscount.subtract(subtotal);
            billDiscount = billDiscount.subtract(overflow).max(BigDecimal.ZERO);
            totalDiscount = lineDiscountTotal.add(billDiscount);
        }
        BigDecimal payable = subtotal.subtract(totalDiscount).max(BigDecimal.ZERO);

        List<BigDecimal> lineDiscounts = ctx.getLines().stream()
                .map(PromotionOrderContext.Line::getDiscountAmount)
                .collect(Collectors.toList());

        return PromotionApplyResult.builder()
                .subtotal(subtotal)
                .totalLineDiscount(lineDiscountTotal)
                .billDiscount(billDiscount)
                .billDiscountType(billDiscountType)
                .couponCode(couponPromo != null ? couponPromo.getCode() : null)
                .totalDiscount(totalDiscount)
                .payable(payable)
                .appliedPromotionIds(appliedIds)
                .lineDiscounts(lineDiscounts)
                .build();
    }

    private BigDecimal applyOne(PromotionEntity promo, PromotionOrderContext ctx, BigDecimal subtotal) {
        PromotionApplicationLevel level = PromotionApplicationLevel.from(promo.getApplicationLevel());
        PromotionBenefitType benefit = PromotionBenefitType.from(promo.getBenefitType());
        if (level == PromotionApplicationLevel.LINE) {
            return applyLine(promo, benefit, ctx);
        }
        return applyCart(promo, benefit, ctx, subtotal);
    }

    private BigDecimal applyLine(PromotionEntity promo, PromotionBenefitType benefit, PromotionOrderContext ctx) {
        BigDecimal total = BigDecimal.ZERO;
        for (PromotionOrderContext.Line line : ctx.getLines()) {
            if (!lineMatchesCatalog(promo, line)) {
                continue;
            }
            BigDecimal lineTotal = line.getUnitPrice()
                    .multiply(BigDecimal.valueOf(line.getQuantity()))
                    .setScale(4, RoundingMode.HALF_UP);
            BigDecimal remaining = lineTotal.subtract(line.getDiscountAmount()).max(BigDecimal.ZERO);
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal discount;
            if (benefit == PromotionBenefitType.PERCENT_OFF) {
                discount = remaining.multiply(promo.getBenefitValue())
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            } else {
                discount = promo.getBenefitValue().multiply(BigDecimal.valueOf(line.getQuantity()))
                        .setScale(4, RoundingMode.HALF_UP);
            }
            discount = cap(discount, promo.getMaxDiscountAmount(), remaining);
            line.setDiscountAmount(line.getDiscountAmount().add(discount));
            total = total.add(discount);
        }
        return total;
    }

    private BigDecimal applyCart(PromotionEntity promo, PromotionBenefitType benefit,
                                 PromotionOrderContext ctx, BigDecimal subtotal) {
        BigDecimal lineDiscounts = ctx.getLines().stream()
                .map(PromotionOrderContext.Line::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal eligible = subtotal.subtract(lineDiscounts).max(BigDecimal.ZERO);
        if (eligible.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (!passesMinCart(promo, subtotal)) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount;
        if (benefit == PromotionBenefitType.PERCENT_OFF) {
            discount = eligible.multiply(promo.getBenefitValue())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        } else {
            discount = promo.getBenefitValue().setScale(4, RoundingMode.HALF_UP);
        }
        return cap(discount, promo.getMaxDiscountAmount(), eligible);
    }

    private boolean matchesScopeTargets(PromotionEntity promo, PromotionOrderContext ctx) {
        if (promo.getStoreId() != null && !promo.getStoreId().equals(ctx.getStoreId())) {
            return false;
        }
        List<PromotionTargetEntity> targets = promo.getTargets() != null ? promo.getTargets() : List.of();
        Set<Long> storeTargets = idsOf(targets, PromotionTargetType.STORE);
        if (!storeTargets.isEmpty() && !storeTargets.contains(ctx.getStoreId())) {
            return false;
        }
        Set<Long> businessTargets = idsOf(targets, PromotionTargetType.BUSINESS);
        if (!businessTargets.isEmpty() && !businessTargets.contains(ctx.getBusinessId())) {
            return false;
        }
        Set<Long> userTargets = idsOf(targets, PromotionTargetType.USER);
        if (!userTargets.isEmpty()) {
            if (ctx.getUserId() == null || !userTargets.contains(ctx.getUserId())) {
                return false;
            }
        }
        return true;
    }

    private boolean lineMatchesCatalog(PromotionEntity promo, PromotionOrderContext.Line line) {
        List<PromotionTargetEntity> targets = promo.getTargets() != null ? promo.getTargets() : List.of();
        List<PromotionTargetEntity> catalog = targets.stream()
                .filter(t -> {
                    try {
                        return PromotionTargetType.from(t.getTargetType()).isCatalog();
                    } catch (IllegalArgumentException ex) {
                        return false;
                    }
                })
                .toList();
        if (catalog.isEmpty()) {
            return true;
        }
        for (PromotionTargetEntity t : catalog) {
            PromotionTargetType type = PromotionTargetType.from(t.getTargetType());
            if (Objects.equals(t.getTargetId(), line.getItemId()) && itemTypeMatches(type, line.getItemType())) {
                return true;
            }
        }
        return false;
    }

    private boolean itemTypeMatches(PromotionTargetType targetType, Long orderItemType) {
        long t = orderItemType != null ? orderItemType : ORDER_TYPE_PRODUCT;
        return switch (targetType) {
            case PRODUCT -> t == ORDER_TYPE_PRODUCT;
            case SERVICE -> t == ORDER_TYPE_SERVICE;
            case PACKAGE -> t == ORDER_TYPE_PACKAGE;
            default -> false;
        };
    }

    private boolean isInSchedule(PromotionEntity p, LocalDateTime now) {
        if (p.getStartAt() != null && now.isBefore(p.getStartAt())) {
            return false;
        }
        if (p.getEndAt() != null && now.isAfter(p.getEndAt())) {
            return false;
        }
        return true;
    }

    private boolean passesMinCart(PromotionEntity p, BigDecimal subtotal) {
        if (p.getMinCartAmount() == null) {
            return true;
        }
        return subtotal.compareTo(p.getMinCartAmount()) >= 0;
    }

    private boolean passesUsageCaps(PromotionEntity p, Long userId) {
        PromotionUsageStatsEntity stats = p.getUsageStats();
        long used = stats != null && stats.getRedemptionCount() != null ? stats.getRedemptionCount() : 0L;
        if (p.getMaxGlobalRedemptions() != null && used >= p.getMaxGlobalRedemptions()) {
            return false;
        }
        if (p.getMaxPerUser() != null && userId != null) {
            long userCount = redemptionRepository.countByPromotionIdAndUserId(p.getId(), userId);
            if (userCount >= p.getMaxPerUser()) {
                return false;
            }
        }
        return true;
    }

    private static Set<Long> idsOf(List<PromotionTargetEntity> targets, PromotionTargetType type) {
        Set<Long> ids = new HashSet<>();
        for (PromotionTargetEntity t : targets) {
            if (type.name().equalsIgnoreCase(t.getTargetType()) && t.getTargetId() != null) {
                ids.add(t.getTargetId());
            }
        }
        return ids;
    }

    private static BigDecimal computeSubtotal(List<PromotionOrderContext.Line> lines) {
        return lines.stream()
                .map(l -> l.getUnitPrice().multiply(BigDecimal.valueOf(l.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal cap(BigDecimal discount, BigDecimal maxDiscount, BigDecimal remaining) {
        BigDecimal d = discount.min(remaining).max(BigDecimal.ZERO);
        if (maxDiscount != null) {
            d = d.min(maxDiscount);
        }
        return d.setScale(4, RoundingMode.HALF_UP);
    }

    private static String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
