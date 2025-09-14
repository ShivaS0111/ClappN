package biz.craftline.server.feature.employeemanagement.api.dto;

import lombok.Data;

@Data
public class EmployeeRequest {
    private String employeeCode;
    private String name;
    private Long userId;
    private Long roleId;
    private String storeId;
    private String businessId;
    // Getters and setters
}

