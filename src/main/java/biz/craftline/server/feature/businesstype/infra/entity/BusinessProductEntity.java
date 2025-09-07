package biz.craftline.server.feature.businesstype.infra.entity;

import biz.craftline.server.feature.businesstype.domain.model.Category;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Date;


@Data
@Entity(name = "business_product")
public class BusinessProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    private int status;

    @ManyToOne
    @JoinColumn(name = "category", nullable = false)
    private CategoryEntity category;

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

