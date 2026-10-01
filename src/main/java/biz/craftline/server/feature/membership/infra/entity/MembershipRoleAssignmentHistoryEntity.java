package biz.craftline.server.feature.membership.infra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

/**
 * Effective-dated record of a role assigned to a membership at a given scope
 * (business-wide, or a specific store). {@code valid_to IS NULL} means the assignment
 * is currently in effect. {@code user_id}/{@code business_id} are point-in-time
 * snapshots, not live joins, so history stays correct even if the membership's
 * business later changes.
 * <p>
 * {@code membership_role} / {@code membership_store_scope} remain the current-state
 * tables used for authorization; this table is purely additive and read-only from
 * the application's perspective except when appending new rows.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "membership_role_assignment_history")
public class MembershipRoleAssignmentHistoryEntity {

    public static final String SCOPE_BUSINESS = "BUSINESS";
    public static final String SCOPE_STORE = "STORE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 16)
    private String scope;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "membership_id", nullable = false)
    private Long membershipId;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name")
    private String roleName;

    @Column(name = "valid_from", nullable = false)
    @Builder.Default
    private Timestamp validFrom = new Timestamp(System.currentTimeMillis());

    @Column(name = "valid_to")
    private Timestamp validTo;

    @Column(name = "changed_by_user_id")
    private Long changedByUserId;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private Timestamp changedAt;

    public boolean isOpen() {
        return validTo == null;
    }
}
