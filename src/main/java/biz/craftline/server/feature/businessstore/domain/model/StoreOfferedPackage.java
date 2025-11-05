package biz.craftline.server.feature.businessstore.domain.model;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.Set;

@Setter
@Getter
public class StoreOfferedPackage {

    // Getters and setters
    private Long id;
    private String name;
    private String description;
    private Long storeId;
    private Integer status;
    private Set<StoreOfferedProduct> products;
    private Set<StoreOfferedService> services;
    private Double price;
    private Boolean available;

}
