package biz.craftline.server.feature.businessstore.api.request;

import lombok.Data;

@Data
public class AddProductLotRequest {

    private Long productId;

    private String lotCode;

    private int quantity;     // Total initially purchased
    private int blocked;      // Temporarily reserved
    private int sold;         // Permanently sold

    private double unitPrice;

    private Long currency;

    private Long country;

    private boolean active = true;

    private String purchasedAt;

    private String mfgDate;

    private String expiryAt;
}
