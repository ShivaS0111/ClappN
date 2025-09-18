package biz.craftline.server.feature.addressmanagement.api.request;

import lombok.Data;

@Data
public class AddressRequest {
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String type;
    private Long referenceId;

    private String digiPin;
    private Double latitude;
    private Double longitude;
    private Long areaId;
    private Long placeId;
    private Long countryId;
    private Long districtId;
    private Long regionId;
    private Long subRegionId;
    private Long landmarkId;
    private Long zipcodeId;
    // Getters and setters
}
