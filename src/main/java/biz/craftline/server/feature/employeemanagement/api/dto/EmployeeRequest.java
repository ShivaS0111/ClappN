package biz.craftline.server.feature.employeemanagement.api.dto;

import lombok.Data;

@Data
public class EmployeeRequest {
    private String employeeCode;
    private String name;

    private String firstName;
    private String lastName;
    private String surName;

    private String email;
    private String phone;

    private String joinDate;
    private String leaveDate;

    private Long userId;
    private Long roleId;
    private Long storeId;
    private Long businessId;

    // Getters and setters
}

