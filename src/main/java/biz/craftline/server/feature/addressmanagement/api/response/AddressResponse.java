
package biz.craftline.server.feature.addressmanagement.api.response;

import lombok.Data;

@Data
public class AddressResponse {

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
    // Getters and setters
}