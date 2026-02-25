package com.example.demo.dto.response.dailyReport;

import java.time.LocalDate;

public record DailySubmitStatResponse(
        LocalDate reportDate,
        long totalMembers,
        long submittedCount,
        long missingCount) {
}
