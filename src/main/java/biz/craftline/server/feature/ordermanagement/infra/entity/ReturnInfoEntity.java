package biz.craftline.server.feature.ordermanagement.infra.entity;

import biz.craftline.server.feature.ordermanagement.application.enums.ReturnStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "return_info")
public class ReturnInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime returnRequestDate;

    private LocalDateTime returnProcessedDate;

    @Enumerated(EnumType.STRING)
    private ReturnStatus status; // REQUESTED, APPROVED, REJECTED, REFUNDED

    private String reason;
}

