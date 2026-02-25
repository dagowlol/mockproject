package com.example.demo.controller;

import com.example.demo.dto.response.APIResponse;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.dailyReport.ComplianceRowResponse;
import com.example.demo.dto.response.dailyReport.DailyReportResponse;
import com.example.demo.dto.response.dailyReport.DailySubmitStatResponse;
import com.example.demo.model.DailyReportStatus;
import com.example.demo.service.DailyReportDashboardService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/daily-reports/dashboard")
public class DailyReportDashboardController {

        private final DailyReportDashboardService dashboardService;

        @GetMapping("/project/{projectId}")
        public ResponseEntity<APIResponse<PageResponse<DailyReportResponse>>> listByProject(
                        @PathVariable Integer projectId,
                        Pageable pageable,
                        @RequestParam(required = false) Integer userId,
                        @RequestParam(required = false) Integer groupId,
                        @RequestParam(required = false) Integer leaderUserId,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) LocalDate reportFrom,
                        @RequestParam(required = false) LocalDate reportTo,
                        @RequestParam(required = false) LocalDateTime submittedFrom,
                        @RequestParam(required = false) LocalDateTime submittedTo,
                        @RequestParam(required = false) LocalDateTime updatedFrom,
                        @RequestParam(required = false) LocalDateTime updatedTo) {
                DailyReportStatus st = (status == null) ? null : DailyReportStatus.valueOf(status);

                Page<DailyReportResponse> page = dashboardService.listByProject(
                                projectId, userId, groupId, leaderUserId, st,
                                reportFrom, reportTo, submittedFrom, submittedTo, updatedFrom, updatedTo, pageable);

                return ResponseEntity.ok(APIResponse.<PageResponse<DailyReportResponse>>builder()
                                .code(200)
                                .message("Dashboard project reports.")
                                .result(PageResponse.from(page))
                                .build());
        }

        @GetMapping("/leader")
        public ResponseEntity<APIResponse<PageResponse<DailyReportResponse>>> listByLeader(
                        Pageable pageable,
                        @RequestParam(required = false) Integer leaderUserId,
                        @RequestParam(required = false) Integer projectId,
                        @RequestParam(required = false) Integer groupId,
                        @RequestParam(required = false) Integer userId,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) LocalDate reportFrom,
                        @RequestParam(required = false) LocalDate reportTo) {
                DailyReportStatus st = (status == null) ? null : DailyReportStatus.valueOf(status);

                Page<DailyReportResponse> page = dashboardService.listByLeader(
                                leaderUserId, projectId, groupId, userId, st, reportFrom, reportTo, pageable);

                return ResponseEntity.ok(APIResponse.<PageResponse<DailyReportResponse>>builder()
                                .code(200)
                                .message("Leader dashboard reports.")
                                .result(PageResponse.from(page))
                                .build());
        }

        @GetMapping("/admin")
        public ResponseEntity<APIResponse<PageResponse<DailyReportResponse>>> listAdmin(
                        Pageable pageable,
                        @RequestParam(required = false) Integer projectId,
                        @RequestParam(required = false) Integer userId,
                        @RequestParam(required = false) Integer groupId,
                        @RequestParam(required = false) Integer leaderUserId,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) LocalDate reportFrom,
                        @RequestParam(required = false) LocalDate reportTo,
                        @RequestParam(required = false) LocalDateTime submittedFrom,
                        @RequestParam(required = false) LocalDateTime submittedTo,
                        @RequestParam(required = false) LocalDateTime updatedFrom,
                        @RequestParam(required = false) LocalDateTime updatedTo) {
                DailyReportStatus st = (status == null) ? null : DailyReportStatus.valueOf(status);

                Page<DailyReportResponse> page = dashboardService.listAdmin(
                                projectId, userId, groupId, leaderUserId, st,
                                reportFrom, reportTo, submittedFrom, submittedTo, updatedFrom, updatedTo, pageable);

                return ResponseEntity.ok(APIResponse.<PageResponse<DailyReportResponse>>builder()
                                .code(200)
                                .message("Admin dashboard reports.")
                                .result(PageResponse.from(page))
                                .build());
        }

        @GetMapping("/project/{projectId}/compliance")
        public ResponseEntity<APIResponse<PageResponse<ComplianceRowResponse>>> compliance(
                        @PathVariable Integer projectId,
                        Pageable pageable,
                        @RequestParam LocalDate reportDate,
                        @RequestParam(required = false) Boolean submitted,
                        @RequestParam(required = false) Integer groupId,
                        @RequestParam(required = false) Integer leaderUserId) {
                Page<ComplianceRowResponse> page = dashboardService.complianceByProject(
                                projectId, reportDate, submitted, groupId, leaderUserId, pageable);

                return ResponseEntity.ok(APIResponse.<PageResponse<ComplianceRowResponse>>builder()
                                .code(200)
                                .message("Compliance result.")
                                .result(PageResponse.from(page))
                                .build());
        }

        @GetMapping("/project/{projectId}/stats")
        public ResponseEntity<APIResponse<List<DailySubmitStatResponse>>> stats(
                        @PathVariable Integer projectId,
                        @RequestParam LocalDate from,
                        @RequestParam LocalDate to) {
                List<DailySubmitStatResponse> list = dashboardService.submitStatsByDay(projectId, from, to);
                return ResponseEntity.ok(APIResponse.<List<DailySubmitStatResponse>>builder()
                                .code(200)
                                .message("Submit stats by day.")
                                .result(list)
                                .build());
        }

        @GetMapping(value = "/project/{projectId}/export.csv")
        public void exportProjectCsv(
                        @PathVariable Integer projectId,
                        @RequestParam(required = false) Integer userId,
                        @RequestParam(required = false) Integer groupId,
                        @RequestParam(required = false) Integer leaderUserId,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) LocalDate reportFrom,
                        @RequestParam(required = false) LocalDate reportTo,
                        @RequestParam(required = false) List<Integer> reportIds,
                        HttpServletResponse response) throws IOException {
                DailyReportStatus st = (status == null) ? null : DailyReportStatus.valueOf(status);

                response.setContentType("text/csv");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"daily_reports_project_" + projectId + ".csv\"");

                dashboardService.exportProjectCsv(projectId, userId, groupId, leaderUserId, st, reportFrom, reportTo,
                                reportIds,
                                response.getOutputStream());
        }
}
