package biz.craftline.server.feature.businessstore.infra.entity;

import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;


@Entity(name = "store_offered_service")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreOfferedServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "alias_name")
    private String aliasName;

    @Column(name = "store_id")
    private Long storeId;

    private int status;

    @ManyToOne
    @JoinColumn(name = "business_service", referencedColumnName = "id")
    private BusinessServiceEntity service;

    private long createdBy;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @JsonBackReference("store-service")
    @ManyToMany(mappedBy = "services")
    private Set<StoreEntity> stores = new HashSet<>();
}
