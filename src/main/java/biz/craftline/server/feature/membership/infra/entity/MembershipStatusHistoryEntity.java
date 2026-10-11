package biz.craftline.server.feature.membership.infra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

/**
 * Append-only audit trail of membership status transitions (e.g. ACTIVE -> INACTIVE on
 * employee deactivation, INACTIVE -> ACTIVE on rehire). {@link MembershipEntity#getStatus()}
 * always holds the current value; this table lets callers reconstruct "start/end" periods.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "membership_status_history")
public class MembershipStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "membership_id", nullable = false)
    private Long membershipId;

    @Column(name = "old_status", length = 32)
    private String oldStatus;

    @Column(name = "new_status", nullable = false, length = 32)
    private String newStatus;

    @Column(name = "changed_by_user_id")
    private Long changedByUserId;

    /** Optional human-readable reason (exit, rehire, transfer, etc.). */
    @Column(length = 512)
    private String reason;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private Timestamp changedAt;
}
