package biz.craftline.server.feature.employeemanagement.api.dto;

import lombok.Data;

@Data
public class EmployeeResponse {
    private Long id;
    private String name;

    private String firstName;
    private String lastName;
    private String surName;

    private String employeeCode;
    private Long userId;
    private Long roleId;
    private Long storeId;
    private Long businessId;

    private String email;
    private String phone;

    private String joinDate;
    private String leaveDate;
    // Getters and setters
}

