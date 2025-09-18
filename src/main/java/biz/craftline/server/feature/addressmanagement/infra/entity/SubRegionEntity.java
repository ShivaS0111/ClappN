package biz.craftline.server.feature.addressmanagement.infra.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "subregion", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class SubRegionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
}

