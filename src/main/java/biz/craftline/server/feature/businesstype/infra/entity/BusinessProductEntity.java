package biz.craftline.server.feature.businesstype.infra.entity;

import biz.craftline.server.feature.businesstype.domain.model.Brand;
import biz.craftline.server.feature.businesstype.domain.model.Category;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity(name = "business_product")
public class BusinessProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    private int status;

    @ManyToOne
    @JoinColumn(name = "business_type", referencedColumnName = "id")
    private BusinessTypeEntity businessType;

    @ManyToOne
    @JoinColumn(name = "brand", referencedColumnName = "id")
    private BrandEntity brand;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "business_product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<CategoryEntity> categories = new ArrayList<>();

    private float amount;

    private Long currency;

    private Long createdBy;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

}

