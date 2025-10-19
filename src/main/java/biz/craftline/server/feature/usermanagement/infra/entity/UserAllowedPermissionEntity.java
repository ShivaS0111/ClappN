package biz.craftline.server.feature.usermanagement.infra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

/**
 * Entity representing user-specific allowed permissions that override role permissions
 */
@Setter
@Getter
@Entity
@Table(name = "user_allowed_permissions")
@NoArgsConstructor
@AllArgsConstructor
public class UserAllowedPermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private PermissionEntity permission;

    @Column(name = "granted_by")
    private String grantedBy; // Who granted this permission

    @Column(name = "reason")
    private String reason; // Reason for granting this permission


    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public UserAllowedPermissionEntity(UserEntity user, PermissionEntity permission) {
        this.user = user;
        this.permission = permission;
    }
}
