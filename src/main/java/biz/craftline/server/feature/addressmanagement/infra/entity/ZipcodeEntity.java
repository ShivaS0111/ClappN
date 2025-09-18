package biz.craftline.server.feature.addressmanagement.infra.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "zipcode", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class ZipcodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, unique = true)
    private String code;
}

