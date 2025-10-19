package biz.craftline.server.feature.usermanagement.infra.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "permission")
@ToString(exclude = {"roles"})
@EqualsAndHashCode(exclude = {"roles"})
public class PermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @ManyToMany(mappedBy = "permissions")
    // Tell Jackson to ignore the back-reference from the Permission back to the Role.
    @JsonBackReference
    private Set<RoleEntity> roles;

    public PermissionEntity(){}

    public PermissionEntity(String permissionName) {
        this.name =permissionName;
        this.description = "";
    }

}
