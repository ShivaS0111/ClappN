package biz.craftline.server.feature.address.infra.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Entity representing an address.
 */
@Data
@Entity(name = "address")
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    /** Street address */
    @NotNull(message = "Street is required")
    @Size(min = 2, max = 100, message = "Street must be between 2 and 100 characters")
    @Column(nullable = false)
    private String street;

    /** City */
    @NotNull(message = "City is required")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    @Column(nullable = false)
    private String city;

    /** State */
    @NotNull(message = "State is required")
    @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters")
    @Column(nullable = false)
    private String state;

    /** Postal code */
    @NotNull(message = "Postal code is required")
    @Size(min = 2, max = 20, message = "Postal code must be between 2 and 20 characters")
    @Column(nullable = false)
    private String postalCode;

    /** Country */
    @NotNull(message = "Country is required")
    @Size(min = 2, max = 50, message = "Country must be between 2 and 50 characters")
    @Column(nullable = false)
    private String country;
}
