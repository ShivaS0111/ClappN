package biz.craftline.server.feature.addressmanagement.infra.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity(name = "address_management")
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    @Column(nullable = false)
    private String street;

    @NotNull
    @Size(min = 2, max = 50)
    @Column(nullable = false)
    private String city;

    @NotNull
    @Size(min = 2, max = 50)
    @Column(nullable = false)
    private String state;

    @NotNull
    @Size(min = 2, max = 20)
    @Column(nullable = false)
    private String postalCode;

    private String digiPin;
    private Double latitude;
    private Double longitude;

    @NotNull
    @Column(nullable = false)
    private String type; // BUSINESS, STORE, USER, EMPLOYEE, DELIVERY

    @NotNull
    @Column(nullable = false)
    private Long referenceId; // ID of the linked entity (business, store, user, etc.)

    @ManyToOne
    @JoinColumn(name = "area_id")
    private AreaEntity area;

    @ManyToOne
    @JoinColumn(name = "place_id")
    private PlaceEntity place;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private CountryEntity country;

    @ManyToOne
    @JoinColumn(name = "district_id")
    private DistrictEntity district;

    @ManyToOne
    @JoinColumn(name = "region_id")
    private RegionEntity region;

    @ManyToOne
    @JoinColumn(name = "subregion_id")
    private SubRegionEntity subRegion;

    @ManyToOne
    @JoinColumn(name = "landmark_id")
    private LandmarkEntity landmark;

    @ManyToOne
    @JoinColumn(name = "zipcode_id")
    private ZipcodeEntity zipcode;
}
