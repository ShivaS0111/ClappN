package biz.craftline.server.feature.inventorymanagement.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductLot {

    private Long id; // Added ID field

    private Long productId;
    private Long storeId;

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
