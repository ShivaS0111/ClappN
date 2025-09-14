package biz.craftline.server.feature.employeemanagement.api.dto;

import lombok.Data;

@Data
public class EmployeeDto {
    private Long id;
    private String employeeCode;
    private String name;
    private Long userId;
    private Long roleId;
    private String storeId;
    private String businessId;
    // Getters and setters
}

