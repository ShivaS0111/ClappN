package biz.craftline.server.feature.addressmanagement.infra.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "area", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class AreaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "place_id")
    private PlaceEntity place;
}
