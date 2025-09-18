package biz.craftline.server.feature.addressmanagement.domain.model;

import lombok.Data;

@Data
public class Address {
    private Long id;
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
    private String areaName;
    private Long placeId;
    private String placeName;
    private Long countryId;
    private String countryName;
    private Long districtId;
    private String districtName;
    private Long regionId;
    private String regionName;
    private Long subRegionId;
    private String subRegionName;
    private Long landmarkId;
    private String landmarkName;
    private Long zipcodeId;
    private String zipcodeName;
    // Getters and setters
}
