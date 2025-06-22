package biz.craftline.server.feature.businesstype.infra.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Date;


@Data
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

    @Column(name = "business_type")
    private Long businessType;

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

