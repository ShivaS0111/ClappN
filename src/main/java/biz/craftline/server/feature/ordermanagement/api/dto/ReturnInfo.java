package biz.craftline.server.feature.ordermanagement.api.dto;


import biz.craftline.server.feature.ordermanagement.application.enums.ReturnStatus;

import java.time.LocalDateTime;

public class ReturnInfo {

    private Long id;

    private LocalDateTime returnRequestDate;

    private LocalDateTime returnProcessedDate;

    private ReturnStatus status; // REQUESTED, APPROVED, REJECTED, REFUNDED

    private String reason;
}
