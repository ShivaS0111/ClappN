package biz.craftline.server.feature.employeemanagement.api.dto;

import lombok.Data;

@Data
public class EmployeeDto {
    private Long id;
    private String employeeCode;
    private String name;
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

