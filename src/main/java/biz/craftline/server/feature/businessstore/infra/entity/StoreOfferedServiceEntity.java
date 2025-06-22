package biz.craftline.server.feature.businessstore.infra.entity;

import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Date;

@Data
@Entity(name = "store_offered_service")
public class StoreOfferedServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(name = "alias_name")
    private String aliasName;

    @Column(name = "store_id")
    private Long storeId;

    private int status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "business_service", referencedColumnName = "id")
    private BusinessServiceEntity service;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "price", referencedColumnName = "id")
    private StoreServicePriceEntity price;

    private long createdBy;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

}
