package com.example.demo.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DelegationRequest {
    @NotNull(message = "Assignment ID cannot be null")
    private Integer assignmentId;

    @NotNull(message = "To Evaluator ID cannot be null")
    private Integer toEvaluatorId;

    @NotNull(message = "Effective From date cannot be null")
    private LocalDate effectiveFrom;

    @NotNull(message = "Effective To date cannot be null")
    private LocalDate effectiveTo;

    @NotNull(message = "Reason cannot be null")
    private String reason;
}
