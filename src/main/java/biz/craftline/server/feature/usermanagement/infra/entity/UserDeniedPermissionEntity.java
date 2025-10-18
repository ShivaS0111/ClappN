package biz.craftline.server.feature.usermanagement.infra.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entity representing user-specific allowed permissions that override role permissions
 */
@Data
@Entity
@Table(name = "user_denied_permissions")
@NoArgsConstructor
@AllArgsConstructor
public class UserDeniedPermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private PermissionEntity permission;

    @Column(name = "denied_by")
    private String deniedBy; // Who deniedBy this permission

    @Column(name = "reason")
    private String reason; // Reason for denied this permission

    public UserDeniedPermissionEntity(UserEntity user, PermissionEntity permission) {
        this.user = user;
        this.permission = permission;
    }
}