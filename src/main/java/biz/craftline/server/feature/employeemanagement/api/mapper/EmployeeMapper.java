package biz.craftline.server.feature.employeemanagement.api.mapper;

import biz.craftline.server.feature.employeemanagement.api.dto.EmployeeDto;
import biz.craftline.server.feature.employeemanagement.api.dto.EmployeeRequest;
import biz.craftline.server.feature.employeemanagement.api.dto.EmployeeResponse;
import biz.craftline.server.feature.employeemanagement.domain.model.Employee;
import biz.craftline.server.feature.employeemanagement.infra.entity.EmployeeEntity;

public class EmployeeMapper {
    public static EmployeeDto toDto(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setName(employee.getName());
        dto.setUserId(employee.getUserId());
        dto.setRoleId(employee.getRoleId());
        dto.setStoreId(employee.getStoreId());
        dto.setBusinessId(employee.getBusinessId());

        dto.setJoinDate(employee.getJoinDate());
        dto.setLeaveDate(employee.getLeaveDate());

        return dto;
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
        return resp;
    }
    public static Employee toDomain(EmployeeEntity entity) {
        Employee employee = new Employee();
        employee.setId(entity.getId());
        employee.setEmployeeCode(entity.getEmployeeCode());

        employee.setName(entity.getName());
        employee.setFirstName(entity.getFirstName());
        employee.setLastName(entity.getLastName());
        employee.setSurName(entity.getSurName());
        employee.setName(entity.getName());
        employee.setUserId(entity.getUserId());
        employee.setRoleId(entity.getRoleId());
        employee.setStoreId(entity.getStoreId());
        employee.setBusinessId(entity.getBusinessId());

        employee.setEmail(entity.getEmail());
        employee.setPhone(entity.getPhone());

        employee.setJoinDate(entity.getJoinDate());
        employee.setLeaveDate(entity.getLeaveDate());
        return employee;
    }
    public static EmployeeEntity toEntity(Employee employee) {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(employee.getId());
        entity.setEmployeeCode(employee.getEmployeeCode());
        entity.setName(employee.getName());

        entity.setName(employee.getName());
        entity.setFirstName(employee.getFirstName());
        entity.setLastName(employee.getLastName());
        entity.setSurName(employee.getSurName());
        // User and Role should be set in service after fetching from DB
        entity.setStoreId(employee.getStoreId());
        entity.setBusinessId(employee.getBusinessId());

        entity.setEmail(employee.getEmail());
        entity.setPhone(employee.getPhone());

        entity.setJoinDate(employee.getJoinDate());
        entity.setLeaveDate(employee.getLeaveDate());
        return entity;
    }
    public static Employee toDomain(EmployeeRequest req) {
        Employee employee = new Employee();
        employee.setEmployeeCode(req.getEmployeeCode());
        employee.setName(req.getName());

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
        return employee;
    }
}

