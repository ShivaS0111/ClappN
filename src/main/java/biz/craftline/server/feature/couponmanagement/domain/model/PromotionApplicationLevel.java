package biz.craftline.server.feature.couponmanagement.domain.model;

public enum PromotionApplicationLevel {
    LINE,
    CART;

    public static PromotionApplicationLevel from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("applicationLevel is required");
        }
        try {
            return PromotionApplicationLevel.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid applicationLevel: " + raw + ". Allowed: LINE, CART");
        }
    }
}
