package biz.craftline.server.feature.businessstore.infra.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;


@Entity
@Table(name = "country")
public class CountryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 2, nullable = false)
    private String isoCode; // e.g., "IN", "US", "DE"

    @Column(nullable = false)
    private String name; // e.g., "India", "United States"

    @CreationTimestamp
    @Column(updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

}
