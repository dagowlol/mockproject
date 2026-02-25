package com.example.demo.mapper;

import com.example.demo.dto.response.dailyReport.DailyReportResponse;
import com.example.demo.dto.response.dailyReport.DailyReportTaskItemResponse;
import com.example.demo.model.DailyReport;
import com.example.demo.model.DailyReportTaskItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DailyReportMapper {

    public DailyReportResponse toResponse(DailyReport r) {
        List<DailyReportTaskItemResponse> items = r.getTaskItems() == null ? List.of()
                : r.getTaskItems().stream().map(this::toItem).collect(Collectors.toList());

        return new DailyReportResponse(
                r.getReportId(),
                r.getProject().getId(),
                r.getProject().getName(),
                r.getUser().getId(),
                r.getUser().getUsername(),
                r.getUser().getFullName(),
                r.getStatus(),
                r.getReportDate(),
                r.getSubmittedAt(),
                r.getCreatedAt(),
                r.getUpdatedAt(),
                r.getContent(),
                items);
    }

    private DailyReportTaskItemResponse toItem(DailyReportTaskItem i) {
        return new DailyReportTaskItemResponse(
                i.getTask().getId(),
                i.getTask().getTitle(),
                i.getProgressNote(),
                i.getTimeSpentMinutes());
    }
}
