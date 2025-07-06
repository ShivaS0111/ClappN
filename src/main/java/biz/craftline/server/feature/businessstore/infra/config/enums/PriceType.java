package biz.craftline.server.feature.businessstore.infra.config.enums;

public enum PriceType {
    DEFAULT,          // Regular price
    PROMOTION,        // Limited-time promotional price
    BULK,             // For minQuantity >= X
    CUSTOMER_SPECIFIC // Optional: pricing by customer tier
}
