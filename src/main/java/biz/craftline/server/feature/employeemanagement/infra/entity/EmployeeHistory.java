package biz.craftline.server.feature.employeemanagement.infra.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "employee")
public class EmployeeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "role_id")
    private Long roleId;

    @Column
    private Long storeId;

    @Column
    private Long businessId;

    private String email;

    private String phone;

    private String joinDate;
    private String leaveDate;
}
