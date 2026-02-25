package com.example.demo.dto.request;

import java.util.List;

public record UpsertDailyReportDraftRequest(
        Integer projectId,
        String content,
        List<DailyReportTaskItemRequest> taskItems) {
}
