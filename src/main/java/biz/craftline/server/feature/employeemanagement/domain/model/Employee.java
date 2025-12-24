package biz.craftline.server.feature.employeemanagement.domain.model;

import lombok.Data;

@Data
public class Employee {
    private Long id;
    private String employeeCode;
    private String name;

    private String firstName;
    private String lastName;
    private String surName;

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

