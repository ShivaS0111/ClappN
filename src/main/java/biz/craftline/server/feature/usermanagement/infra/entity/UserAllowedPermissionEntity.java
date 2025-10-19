package biz.craftline.server.feature.usermanagement.infra.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing user-specific allowed permissions that override role permissions
 */


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_allowed_permissions")
@ToString(exclude = {"user", "permission"})
@EqualsAndHashCode(exclude = {"user", "permission"})
public class UserAllowedPermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private PermissionEntity permission;

    private Long userId;

    @Column(name = "granted_by")
    private Long grantedBy;

    @Column(name = "reason")
    private String reason;

    public UserAllowedPermissionEntity(UserEntity user, PermissionEntity permission) {
        this.userId = user.getId();
        this.permission = permission;
    }
}