package com.example.demo.dto.response.dailyReport;

public record DailyReportTaskItemResponse(
        Integer taskId,
        String taskTitle,
        String progressNote,
        Integer timeSpentMinutes) {
}
