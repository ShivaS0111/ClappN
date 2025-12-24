package biz.craftline.server.feature.employeemanagement.infra.entity;

import biz.craftline.server.feature.usermanagement.infra.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "employee")
public class EmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employeeCode;

    @Column(nullable = false)
    private String name;

    private String firstName;

    private String lastName;

    private String surName;

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

