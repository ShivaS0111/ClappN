package biz.craftline.server.feature.ordermanagement.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Returned when client-claimed discount/totals do not match server recomputation.
 * Client must confirm an option (if proceedable) or reconfigure the cart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountMismatchDetails {

    /** false when client claimed more discount than entitled — cannot proceed with claim. */
    private boolean proceedable;
    private String reasonCode;
    private String reason;

    private AmountSnapshot client;
    private AmountSnapshot server;

    @Builder.Default
    private List<ResolutionOption> options = new ArrayList<>();

    /**
     * Re-submit place-order with the same payload plus {@code discountResolution}
     * set to one of the option codes (except RECONFIGURE).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AmountSnapshot {
        private BigDecimal subtotal;
        private BigDecimal billDiscount;
        private BigDecimal totalDiscount;
        private BigDecimal totalAmount;
        private String couponCode;
        @Builder.Default
        private List<BigDecimal> lineDiscounts = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResolutionOption {
        /** APPLY_SERVER_FULL | APPLY_CLIENT_CLAIMED | RECONFIGURE */
        private String code;
        private String label;
        private String description;
    }

    public static final String REASON_CLIENT_HIGHER = "CLIENT_DISCOUNT_HIGHER";
    public static final String REASON_CLIENT_LOWER = "CLIENT_DISCOUNT_LOWER";
    public static final String OPT_APPLY_SERVER_FULL = "APPLY_SERVER_FULL";
    public static final String OPT_APPLY_CLIENT_CLAIMED = "APPLY_CLIENT_CLAIMED";
    public static final String OPT_RECONFIGURE = "RECONFIGURE";
}
