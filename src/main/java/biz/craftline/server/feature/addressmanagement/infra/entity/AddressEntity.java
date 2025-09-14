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

    @NotNull
    @Size(min = 2, max = 50)
    @Column(nullable = false)
    private String country;


    private String digiPin;
    private Double latitude;
    private Double longitude;

    @NotNull
    @Column(nullable = false)
    private String type; // BUSINESS, STORE, USER, EMPLOYEE, DELIVERY

    @NotNull
    @Column(nullable = false)
    private Long referenceId; // ID of the linked entity (business, store, user, etc.)
}

