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

    private String bannerUrl;

    private String galleryUrls;

    // --- Owner fields for automatic BusinessOwner creation ---
    @NotNull(message = "Owner name is required")
    @Size(min = 2, max = 100, message = "Owner name must be between 2 and 100 characters")
    private String ownerName;

    @NotNull(message = "Owner email is required")
    @Size(min = 5, max = 100, message = "Owner email must be between 5 and 100 characters")
    private String ownerEmail;

    @NotNull(message = "Owner phone is required")
    @Size(min = 7, max = 20, message = "Owner phone must be between 7 and 20 characters")
    private String ownerPhone;

    @NotNull(message = "Owner password is required")
    @Size(min = 6, max = 100, message = "Owner password must be between 6 and 100 characters")
    private String ownerPassword;

}