package biz.craftline.server.feature.businessstore.infra.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "store")
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

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "business_type")
    private Long businessType;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @ManyToOne
    @JoinColumn(name = "business_id", referencedColumnName = "id")
    @JsonBackReference
    private BusinessEntity business;

    @Column(name = "address_id")
    private Long addressId;

    private String address;
    private String email;
    private String phone;
    private String manager;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "store_services",
            joinColumns = @JoinColumn(name = "store_id"),
            inverseJoinColumns = @JoinColumn(name = "store_service_id"))
    @JsonManagedReference("store-service")
    private Set<StoreOfferedServiceEntity> services = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "store_products",
            joinColumns = @JoinColumn(name = "store_id"),
            inverseJoinColumns = @JoinColumn(name = "store_product_id"))
    @JsonManagedReference("store-product")
    private Set<StoreOfferedProductEntity> products = new HashSet<>();
}