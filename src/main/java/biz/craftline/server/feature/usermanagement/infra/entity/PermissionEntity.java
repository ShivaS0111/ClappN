package biz.craftline.server.feature.usermanagement.infra.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "permission")
public class PermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;
    @Column
    private String category;

    @Column
    private String type;

    @Column
    private String level;

    public PermissionEntity(){}

    public PermissionEntity(String permissionName) {
        this.name =permissionName;
        this.description = "";
    }

}
