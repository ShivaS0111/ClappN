package biz.craftline.server.feature.businessstore.infra.entity;

import biz.craftline.server.feature.address.infra.entity.AddressEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity(name = "store")
public class StoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "description")
    private String description;

    private int status;

    private long createdBy;

    private long businessType;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "business_id", referencedColumnName = "id")
    private BusinessEntity business;

    private long address;

    @JsonManagedReference("store-service")
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "store_services",
            joinColumns = @JoinColumn(name = "store_id"),
            inverseJoinColumns = @JoinColumn(name = "store_service_id"))
    private Set<StoreOfferedServiceEntity> services = new HashSet<>();

}