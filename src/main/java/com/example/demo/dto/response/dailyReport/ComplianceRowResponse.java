package com.example.demo.dto.response.dailyReport;

import java.time.LocalDateTime;

public record ComplianceRowResponse(
        Integer userId,
        String username,
        String fullName,
        boolean submitted,
        Integer reportId,
        LocalDateTime submittedAt) {
}
