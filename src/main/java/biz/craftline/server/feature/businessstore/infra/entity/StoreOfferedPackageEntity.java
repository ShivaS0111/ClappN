package biz.craftline.server.feature.businessstore.infra.entity;

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

@Entity
@Table(name = "store_offered_package")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreOfferedPackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    private Integer status;

    private Double price;

    private Boolean available;

    @ElementCollection
    @CollectionTable(name = "store_offered_package_products", joinColumns = @JoinColumn(name = "package_id"))
    @Column(name = "store_product_id")
    @Builder.Default
    private Set<Long> productIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "store_offered_package_services", joinColumns = @JoinColumn(name = "package_id"))
    @Column(name = "store_service_id")
    @Builder.Default
    private Set<Long> serviceIds = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
