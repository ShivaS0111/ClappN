package biz.craftline.server.feature.businessstore.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AddNewBusinessRequest {

    private String businessName;

    private String description;

}