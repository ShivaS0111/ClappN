package biz.craftline.server.feature.membership.domain.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.membership.infra.entity.EmployeeProfileHistoryEntity;
import biz.craftline.server.feature.membership.infra.entity.MembershipRoleAssignmentHistoryEntity;
import biz.craftline.server.feature.membership.infra.entity.MembershipStatusHistoryEntity;
import biz.craftline.server.feature.membership.infra.repository.EmployeeProfileHistoryRepository;
import biz.craftline.server.feature.membership.infra.repository.MembershipRoleAssignmentHistoryRepository;
import biz.craftline.server.feature.membership.infra.repository.MembershipStatusHistoryRepository;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipHistoryServiceTest {

    @Mock MembershipStatusHistoryRepository statusHistoryRepository;
    @Mock MembershipRoleAssignmentHistoryRepository assignmentHistoryRepository;
    @Mock EmployeeProfileHistoryRepository profileHistoryRepository;
    @Mock SecurityContextService securityContextService;

    @InjectMocks MembershipHistoryService service;

    @BeforeEach
    void setUp() {
        lenient().when(securityContextService.getCurrentUserId()).thenReturn(99L);
        lenient().when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(assignmentHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(profileHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void recordStatusChange_withReason() {
        service.recordStatusChange(1L, "ACTIVE", "INACTIVE", "resigned");
        ArgumentCaptor<MembershipStatusHistoryEntity> cap =
                ArgumentCaptor.forClass(MembershipStatusHistoryEntity.class);
        verify(statusHistoryRepository).save(cap.capture());
        assertEquals("resigned", cap.getValue().getReason());
        assertEquals(99L, cap.getValue().getChangedByUserId());
    }

    @Test
    void recordStatusChange_skipsNoOp() {
        service.recordStatusChange(1L, "ACTIVE", "ACTIVE", "noop");
        verify(statusHistoryRepository, never()).save(any());
    }

    @Test
    void closeOpenRoleAssignments_setsValidTo() {
        MembershipRoleAssignmentHistoryEntity open = MembershipRoleAssignmentHistoryEntity.builder()
                .id(5L)
                .membershipId(1L)
                .roleId(2L)
                .scope("STORE")
                .storeId(3L)
                .userId(9L)
                .businessId(4L)
                .build();
        when(assignmentHistoryRepository.findByMembershipIdAndValidToIsNull(1L)).thenReturn(List.of(open));

        service.closeOpenRoleAssignments(1L);

        assertNotNull(open.getValidTo());
        verify(assignmentHistoryRepository).save(open);
    }

    @Test
    void recordProfileFieldChanges_writesDiffsOnly() {
        Map<String, String> before = new HashMap<>();
        before.put("jobTitle", "Cashier");
        before.put("phone", "111");
        Map<String, String> after = new HashMap<>();
        after.put("jobTitle", "Supervisor");
        after.put("phone", "111");
        after.put("leaveDate", "2026-10-11");

        service.recordProfileFieldChanges(7L, before, after);

        ArgumentCaptor<EmployeeProfileHistoryEntity> cap =
                ArgumentCaptor.forClass(EmployeeProfileHistoryEntity.class);
        verify(profileHistoryRepository, times(2)).save(cap.capture());
        List<String> fields = cap.getAllValues().stream().map(EmployeeProfileHistoryEntity::getFieldName).toList();
        assertTrue(fields.contains("jobTitle"));
        assertTrue(fields.contains("leaveDate"));
        assertFalse(fields.contains("phone"));
    }

    @Test
    void deriveEmploymentPeriods_openAndClosed() {
        Timestamp t1 = Timestamp.valueOf("2026-01-01 10:00:00");
        Timestamp t2 = Timestamp.valueOf("2026-06-01 10:00:00");
        Timestamp t3 = Timestamp.valueOf("2026-09-01 10:00:00");
        List<MembershipStatusHistoryEntity> history = List.of(
                MembershipStatusHistoryEntity.builder().newStatus("ACTIVE").changedAt(t1).build(),
                MembershipStatusHistoryEntity.builder().newStatus("INACTIVE").changedAt(t2).build(),
                MembershipStatusHistoryEntity.builder().newStatus("ACTIVE").changedAt(t3).build()
        );

        List<MembershipHistoryService.EmploymentPeriod> periods =
                MembershipHistoryService.deriveEmploymentPeriods(history);

        assertEquals(2, periods.size());
        assertEquals(t1, periods.get(0).startedAt());
        assertEquals(t2, periods.get(0).endedAt());
        assertEquals("INACTIVE", periods.get(0).endStatus());
        assertEquals(t3, periods.get(1).startedAt());
        assertNull(periods.get(1).endedAt());
    }

    @Test
    void recordRoleAssignmentChanges_opensStoreScopedRow() {
        when(assignmentHistoryRepository.findByMembershipIdAndValidToIsNull(1L)).thenReturn(List.of());
        RoleEntity role = new RoleEntity();
        role.setId(4L);
        role.setName("CASHIER");

        service.recordRoleAssignmentChanges(1L, 9L, 2L, Set.of(role), Set.of(5L));

        ArgumentCaptor<MembershipRoleAssignmentHistoryEntity> cap =
                ArgumentCaptor.forClass(MembershipRoleAssignmentHistoryEntity.class);
        verify(assignmentHistoryRepository).save(cap.capture());
        assertEquals("STORE", cap.getValue().getScope());
        assertEquals(5L, cap.getValue().getStoreId());
        assertEquals(4L, cap.getValue().getRoleId());
    }
}
