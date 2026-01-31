package com.example.demo.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DelegationResponse {
    private Long id;
    private Integer assignmentId;
    private String assignmentTargetName; // Helper for UI
    private Integer fromEvaluatorId;
    private String fromEvaluatorName;
    private Integer toEvaluatorId;
    private String toEvaluatorName;
    private String status;
    private String reason;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private LocalDateTime createdAt;
}
