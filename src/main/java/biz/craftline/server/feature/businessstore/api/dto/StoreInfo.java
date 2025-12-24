package biz.craftline.server.feature.businessstore.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StoreInfo {
    String name;
    String description;
    String address;
    String category;
    String storeManager;
    String establishedDate;
    String email;
    String phone;
}
