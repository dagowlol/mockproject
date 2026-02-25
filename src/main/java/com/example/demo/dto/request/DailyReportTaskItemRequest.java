package com.example.demo.dto.request;

public record DailyReportTaskItemRequest(
                Integer taskId,
                String progressNote,
                Integer timeSpentMinutes) {
}
