package biz.craftline.server.feature.addressmanagement.infra.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "region", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class RegionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
}

