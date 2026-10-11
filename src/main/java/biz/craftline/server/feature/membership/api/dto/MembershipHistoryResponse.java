package biz.craftline.server.feature.membership.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

/** Combined status + role-assignment + profile change history for a membership. Read-only. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembershipHistoryResponse {
    private Long membershipId;
    private List<StatusChange> statusHistory;
    private List<RoleAssignmentChange> roleAssignmentHistory;
    private List<ProfileChange> profileHistory;
    private List<EmploymentPeriod> employmentPeriods;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusChange {
        private String oldStatus;
        private String newStatus;
        private Long changedByUserId;
        private String reason;
        private Timestamp changedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleAssignmentChange {
        private String scope;
        private Long storeId;
        private Long roleId;
        private String roleName;
        private Timestamp validFrom;
        private Timestamp validTo;
        private Long changedByUserId;
        private Timestamp changedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileChange {
        private String fieldName;
        private String oldValue;
        private String newValue;
        private Long changedByUserId;
        private Timestamp changedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmploymentPeriod {
        private Timestamp startedAt;
        private Timestamp endedAt;
        private String startStatus;
        private String endStatus;
    }
}
