package biz.craftline.server.feature.couponmanagement.domain.model;

public enum PromotionBenefitType {
    PERCENT_OFF,
    FIXED_OFF;

    public static PromotionBenefitType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("benefitType is required");
        }
        try {
            return PromotionBenefitType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid benefitType: " + raw + ". Allowed: PERCENT_OFF, FIXED_OFF");
        }
    }
}
