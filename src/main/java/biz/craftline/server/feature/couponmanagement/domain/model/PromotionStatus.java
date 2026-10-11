package biz.craftline.server.feature.couponmanagement.domain.model;

public enum PromotionStatus {
    DRAFT,
    ACTIVE,
    PAUSED,
    EXPIRED,
    REVOKED;

    public static PromotionStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            return DRAFT;
        }
        try {
            return PromotionStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status: " + raw);
        }
    }
}
