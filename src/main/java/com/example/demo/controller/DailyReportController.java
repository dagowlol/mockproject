package com.example.demo.controller;

import com.example.demo.dto.request.UpsertDailyReportDraftRequest;
import com.example.demo.dto.response.APIResponse;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.dailyReport.DailyReportResponse;
import com.example.demo.model.DailyReportStatus;
import com.example.demo.service.DailyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/daily-reports")
public class DailyReportController {

        private final DailyReportService dailyReportService;

        @PostMapping("/draft")
        public ResponseEntity<APIResponse<DailyReportResponse>> getOrCreateDraft(@RequestParam Integer projectId) {
                DailyReportResponse result = dailyReportService.getOrCreateDraft(projectId);

                return ResponseEntity.ok(APIResponse.<DailyReportResponse>builder()
                                .code(200)
                                .message("Draft loaded/created.")
                                .result(result)
                                .build());
        }

        @PatchMapping("/draft/{reportId}")
        public ResponseEntity<APIResponse<DailyReportResponse>> updateDraft(@PathVariable Integer reportId,
                        @RequestBody UpsertDailyReportDraftRequest req) {
                log.info("Updating draft: {}", req.content());
                DailyReportResponse result = dailyReportService.updateDraft(reportId, req);

                return ResponseEntity.ok(APIResponse.<DailyReportResponse>builder()
                                .code(200)
                                .message("Draft updated.")
                                .result(result)
                                .build());
        }

        @PostMapping("/draft/{reportId}/submit")
        public ResponseEntity<APIResponse<DailyReportResponse>> submit(@PathVariable Integer reportId) {
                DailyReportResponse result = dailyReportService.submit(reportId);

                return ResponseEntity.ok(APIResponse.<DailyReportResponse>builder()
                                .code(200)
                                .message("Submitted.")
                                .result(result)
                                .build());
        }

        @GetMapping("/me")
        public ResponseEntity<APIResponse<PageResponse<DailyReportResponse>>> listMine(
                        Pageable pageable,
                        @RequestParam(required = false) Integer projectId,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) LocalDate reportFrom,
                        @RequestParam(required = false) LocalDate reportTo,
                        @RequestParam(required = false) LocalDateTime submittedFrom,
                        @RequestParam(required = false) LocalDateTime submittedTo,
                        @RequestParam(required = false) LocalDateTime updatedFrom,
                        @RequestParam(required = false) LocalDateTime updatedTo) {
                DailyReportStatus st = (status == null) ? null : DailyReportStatus.valueOf(status);

                Page<DailyReportResponse> page = dailyReportService.listMine(
                                projectId, st, reportFrom, reportTo, submittedFrom, submittedTo, updatedFrom, updatedTo,
                                pageable);

                return ResponseEntity.ok(APIResponse.<PageResponse<DailyReportResponse>>builder()
                                .code(200)
                                .message("Daily reports retrieved successfully.")
                                .result(PageResponse.from(page))
                                .build());
        }

        @GetMapping("/{reportId}")
        public ResponseEntity<APIResponse<DailyReportResponse>> getById(@PathVariable Integer reportId) {
                DailyReportResponse result = dailyReportService.getById(reportId);

                return ResponseEntity.ok(APIResponse.<DailyReportResponse>builder()
                                .code(200)
                                .message("Daily report retrieved successfully.")
                                .result(result)
                                .build());
        }

        @GetMapping("/projects/{projectId}/tasks")
        public ResponseEntity<APIResponse<java.util.List<com.example.demo.dto.response.dailyReport.SimpleTaskResponse>>> listTasks(
                        @PathVariable Integer projectId) {
                java.util.List<com.example.demo.dto.response.dailyReport.SimpleTaskResponse> list = dailyReportService
                                .listTasksByProject(projectId);

                return ResponseEntity.ok(
                                APIResponse.<java.util.List<com.example.demo.dto.response.dailyReport.SimpleTaskResponse>>builder()
                                                .code(200)
                                                .message("Tasks retrieved.")
                                                .result(list)
                                                .build());
        }
}
