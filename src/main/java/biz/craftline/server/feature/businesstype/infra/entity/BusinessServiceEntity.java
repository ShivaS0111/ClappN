package biz.craftline.server.feature.businesstype.infra.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
@Entity(name = "business_service")
public class BusinessServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(name = "service_name", unique = true, nullable = false)
    private String serviceName;

    @Column(name = "description")
    private String description;

    private int status;

    @ManyToOne
    @JoinColumn(name = "business_type", nullable = false)
    private BusinessTypeEntity businessType;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "business_service_categories",
            joinColumns = @JoinColumn(name = "service_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<CategoryEntity> categories = new ArrayList<>();

    private float amount;

    private Long currency;
    private Long duration;//in minutes

    private Long createdBy;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

}

