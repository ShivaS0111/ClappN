package biz.craftline.server.feature.couponmanagement.domain.model;

public enum PromotionTargetType {
    BUSINESS,
    STORE,
    USER,
    PRODUCT,
    SERVICE,
    PACKAGE;

    public static PromotionTargetType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("targetType is required");
        }
        try {
            return PromotionTargetType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid targetType: " + raw);
        }
    }

    public boolean isCatalog() {
        return this == PRODUCT || this == SERVICE || this == PACKAGE;
    }
}
