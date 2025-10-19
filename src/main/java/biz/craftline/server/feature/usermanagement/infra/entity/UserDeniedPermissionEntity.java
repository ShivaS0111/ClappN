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
@Table(name = "user_denied_permissions")
@ToString(exclude = {"user", "permission"})
@EqualsAndHashCode(exclude = {"user", "permission"})
public class UserDeniedPermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private PermissionEntity permission;

    @Column(name = "denied_by")
    private String deniedBy;

    @Column(name = "reason")
    private String reason;

    public UserDeniedPermissionEntity(UserEntity user, PermissionEntity permission) {
        this.userId = user.getId();
        this.permission = permission;
    }
}