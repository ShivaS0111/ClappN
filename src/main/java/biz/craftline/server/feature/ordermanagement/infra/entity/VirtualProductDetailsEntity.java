package biz.craftline.server.feature.ordermanagement.infra.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "virtual_product_details")
public class VirtualProductDetailsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String licenseKey;

    @Column
    private Date activationDate;

    // Getters and setters
    // ...
}

