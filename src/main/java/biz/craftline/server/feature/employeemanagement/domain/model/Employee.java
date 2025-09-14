package biz.craftline.server.feature.employeemanagement.domain.model;

import lombok.Data;

@Data
public class Employee {
    private Long id;
    private String employeeCode;
    private String name;
    private Long userId;
    private Long roleId;
    private String storeId;
    private String businessId;
    // Getters and setters
}

