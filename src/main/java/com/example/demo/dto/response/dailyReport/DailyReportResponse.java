package com.example.demo.dto.response.dailyReport;

import com.example.demo.model.DailyReportStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DailyReportResponse(
        Integer reportId,
        Integer projectId,
        String projectName,
        Integer userId,
        String username,
        String fullName,
        DailyReportStatus status,
        LocalDate reportDate,
        LocalDateTime submittedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String content,
        List<DailyReportTaskItemResponse> taskItems) {
}
