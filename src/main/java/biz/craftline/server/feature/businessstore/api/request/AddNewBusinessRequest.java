package biz.craftline.server.feature.businessstore.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AddNewBusinessRequest {

    @NotNull(message = "Business name is required")
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    private String businessName;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    private int status;
    private String contact;
    private String email;
    private String website;
    private String address;
    private Double latitude;
    private Double longitude;

}