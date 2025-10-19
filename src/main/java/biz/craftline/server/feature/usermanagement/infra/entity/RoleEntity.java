package biz.craftline.server.feature.usermanagement.infra.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "role")
@ToString(exclude = {"permissions", "users"})
@EqualsAndHashCode(exclude = {"permissions", "users"})
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permission",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @JsonManagedReference
    private Set<PermissionEntity> permissions;

    @ManyToMany(mappedBy = "roles")
    // This is the crucial part: tell Jackson to ignore the back-reference
    // from the Role back to the User to prevent the serialization loop.
    @JsonBackReference
    private Set<UserEntity> users;

}
