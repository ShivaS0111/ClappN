package biz.craftline.server.feature.businesstype.infra.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Date;
import java.util.List;

@Data
@Entity(name = "business_type")
public class BusinessTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(name = "business_name", unique = true, nullable = false)
    private String businessName;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private Integer  status = 0;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
}

