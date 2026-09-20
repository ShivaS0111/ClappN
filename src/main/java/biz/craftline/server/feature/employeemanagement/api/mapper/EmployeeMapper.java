package biz.craftline.server.feature.employeemanagement.api.mapper;

import biz.craftline.server.feature.employeemanagement.api.dto.EmployeeRequest;
import biz.craftline.server.feature.employeemanagement.api.dto.EmployeeResponse;
import biz.craftline.server.feature.employeemanagement.domain.model.Employee;
import biz.craftline.server.feature.membership.infra.entity.EmployeeProfileEntity;
import biz.craftline.server.feature.membership.infra.entity.MembershipEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;

import java.util.Comparator;

public class EmployeeMapper {

    public static Employee fromMembership(MembershipEntity membership) {
        Employee employee = new Employee();
        employee.setId(membership.getId());
        employee.setUserId(membership.getUserId());
        employee.setBusinessId(membership.getBusinessId());
        employee.setStatus(membership.getStatus());

        if (membership.getRoles() != null && !membership.getRoles().isEmpty()) {
            membership.getRoles().stream()
                    .min(Comparator.comparing(RoleEntity::getId, Comparator.nullsLast(Long::compareTo)))
                    .map(RoleEntity::getId)
                    .ifPresent(employee::setRoleId);
        }

        if (membership.getStoreScopes() != null && !membership.getStoreScopes().isEmpty()) {
            employee.setStoreId(membership.getStoreScopes().stream().min(Long::compareTo).orElse(null));
        }

        EmployeeProfileEntity profile = membership.getProfile();
        if (profile != null) {
            employee.setEmployeeCode(profile.getEmployeeNumber());
            employee.setJoinDate(profile.getHireDate());
            employee.setJobTitle(profile.getJobTitle());
            employee.setName(profile.getName());
            employee.setFirstName(profile.getFirstName());
            employee.setLastName(profile.getLastName());
            employee.setSurName(profile.getSurName());
            employee.setEmail(profile.getEmail());
            employee.setPhone(profile.getPhone());
            employee.setLeaveDate(profile.getLeaveDate());
            if (profile.getUserId() != null) {
                employee.setUserId(profile.getUserId());
            }
        }
        return employee;
    }

    public static EmployeeResponse toResponse(Employee employee) {
        EmployeeResponse resp = new EmployeeResponse();
        resp.setId(employee.getId());
        resp.setEmployeeCode(employee.getEmployeeCode());
        resp.setName(employee.getName());
        resp.setFirstName(employee.getFirstName());
        resp.setLastName(employee.getLastName());
        resp.setSurName(employee.getSurName());
        resp.setUserId(employee.getUserId());
        resp.setRoleId(employee.getRoleId());
        resp.setStoreId(employee.getStoreId());
        resp.setBusinessId(employee.getBusinessId());
        resp.setEmail(employee.getEmail());
        resp.setPhone(employee.getPhone());
        resp.setJoinDate(employee.getJoinDate());
        resp.setLeaveDate(employee.getLeaveDate());
        resp.setJobTitle(employee.getJobTitle());
        resp.setStatus(employee.getStatus());
        return resp;
    }

    public static Employee toDomain(EmployeeRequest req) {
        Employee employee = new Employee();
        employee.setEmployeeCode(req.getEmployeeCode());
        employee.setName(req.getName());
        employee.setFirstName(req.getFirstName());
        employee.setLastName(req.getLastName());
        employee.setSurName(req.getSurName());
        employee.setUserId(req.getUserId());
        employee.setRoleId(req.getRoleId());
        employee.setStoreId(req.getStoreId());
        employee.setBusinessId(req.getBusinessId());
        employee.setEmail(req.getEmail());
        employee.setPhone(req.getPhone());
        employee.setJoinDate(req.getJoinDate());
        employee.setLeaveDate(req.getLeaveDate());
        employee.setJobTitle(req.getJobTitle());
        employee.setStatus(req.getStatus());
        return employee;
    }
}
