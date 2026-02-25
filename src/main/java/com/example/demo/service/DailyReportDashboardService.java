package com.example.demo.service;

import com.example.demo.dto.response.dailyReport.ComplianceRowResponse;
import com.example.demo.dto.response.dailyReport.DailyReportResponse;
import com.example.demo.dto.response.dailyReport.DailySubmitStatResponse;
import com.example.demo.exception.AppException;
import com.example.demo.mapper.DailyReportMapper;
import com.example.demo.model.*;
import com.example.demo.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.demo.repo.specification.DailyReportSpecification.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DailyReportDashboardService {

    private final DailyReportRepository reportRepo;
    private final ProjectMemberRepository projectMemberRepo;
    private final UserRepository userRepo;

    private final DailyReportMapper mapper;

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private boolean isAdmin(User u) {
        log.info("Checking roles for user: {}. roleName: {}, role: {}", u.getUsername(), u.getRoleName(), u.getRole());
        String rn = u.getRoleName();
        String r = u.getRole();
        return "ADMIN".equalsIgnoreCase(rn) || "SUPER_ADMIN".equalsIgnoreCase(rn) ||
                "ADMIN".equalsIgnoreCase(r) || "SUPER_ADMIN".equalsIgnoreCase(r);
    }

    private void requireProjectMemberOrAdmin(Integer projectId, User actor) {
        if (isAdmin(actor))
            return;
        if (!projectMemberRepo.existsByProject_IdAndUser_Id(projectId, actor.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "User not in project");
        }
    }

    private void requireLeaderOrAdmin(User actor) {
        if (isAdmin(actor))
            return;
        if (!Boolean.TRUE.equals(actor.getLeader())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Unauthorized");
        }
    }

    public Page<DailyReportResponse> listByProject(Integer projectId,
            Integer userId,
            Integer groupId,
            Integer leaderUserId,
            DailyReportStatus status,
            LocalDate reportFrom,
            LocalDate reportTo,
            LocalDateTime submittedFrom,
            LocalDateTime submittedTo,
            LocalDateTime updatedFrom,
            LocalDateTime updatedTo,
            Pageable pageable) {

        User actor = currentUser();
        requireProjectMemberOrAdmin(projectId, actor);

        Specification<DailyReport> spec = Specification.where(hasProjectId(projectId))
                .and(hasUserId(userId))
                .and(hasStatus(status))
                .and(reportDateBetween(reportFrom, reportTo))
                .and(submittedAtBetween(submittedFrom, submittedTo))
                .and(updatedAtBetween(updatedFrom, updatedTo));

        return reportRepo.findAll(spec, pageable).map(mapper::toResponse);
    }

    public Page<DailyReportResponse> listByLeader(Integer leaderUserId,
            Integer projectId,
            Integer groupId,
            Integer userId,
            DailyReportStatus status,
            LocalDate reportFrom,
            LocalDate reportTo,
            Pageable pageable) {

        User actor = currentUser();
        requireLeaderOrAdmin(actor);

        Integer effectiveLeaderId = (leaderUserId != null) ? leaderUserId : actor.getId();

        Specification<DailyReport> spec = Specification.where(hasProjectId(projectId))
                .and(hasUserId(userId))
                .and(hasStatus(status))
                .and(reportDateBetween(reportFrom, reportTo));

        return reportRepo.findAll(spec, pageable).map(r -> mapper.toResponse(r));
    }

    public Page<DailyReportResponse> listAdmin(Integer projectId,
            Integer userId,
            Integer groupId,
            Integer leaderUserId,
            DailyReportStatus status,
            LocalDate reportFrom,
            LocalDate reportTo,
            LocalDateTime submittedFrom,
            LocalDateTime submittedTo,
            LocalDateTime updatedFrom,
            LocalDateTime updatedTo,
            Pageable pageable) {

        User actor = currentUser();
        requireLeaderOrAdmin(actor);

        Specification<DailyReport> spec = Specification.where(hasProjectId(projectId))
                .and(hasUserId(userId))
                .and(hasStatus(status))
                .and(reportDateBetween(reportFrom, reportTo))
                .and(submittedAtBetween(submittedFrom, submittedTo))
                .and(updatedAtBetween(updatedFrom, updatedTo));

        return reportRepo.findAll(spec, pageable).map(mapper::toResponse);
    }

    public Page<ComplianceRowResponse> complianceByProject(Integer projectId,
            LocalDate reportDate,
            Boolean submitted,
            Integer groupId,
            Integer leaderUserId,
            Pageable pageable) {

        User actor = currentUser();
        requireProjectMemberOrAdmin(projectId, actor);

        List<ProjectMember> members = projectMemberRepo.findByProjectId(projectId);
        List<Integer> baseUserIds = members.stream().map(m -> m.getUser().getId()).collect(Collectors.toList());

        if (baseUserIds.isEmpty()) {
            return Page.empty(pageable);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), baseUserIds.size());
        if (start >= end) {
            return new PageImpl<>(List.of(), pageable, baseUserIds.size());
        }

        List<Integer> pageUserIds = baseUserIds.subList(start, end);
        List<User> usersList = userRepo.findAllById(pageUserIds);
        Map<Integer, User> usersMap = usersList.stream().collect(Collectors.toMap(User::getId, u -> u));

        Specification<DailyReport> spec = Specification.where(hasProjectId(projectId))
                .and(reportDateBetween(reportDate, reportDate))
                .and(hasStatus(DailyReportStatus.SUBMITTED));

        List<DailyReport> submittedReports = reportRepo.findAll(spec);
        Map<Integer, DailyReport> byUser = new HashMap<>();
        for (DailyReport r : submittedReports) {
            byUser.put(r.getUser().getId(), r);
        }

        List<ComplianceRowResponse> rows = new ArrayList<>();
        for (Integer uid : pageUserIds) {
            User u = usersMap.get(uid);
            DailyReport r = byUser.get(uid);

            boolean has = (r != null);
            if (submitted != null && submitted.booleanValue() != has)
                continue;

            rows.add(new ComplianceRowResponse(
                    u.getId(),
                    u.getUsername(),
                    u.getFullName(),
                    has,
                    has ? r.getReportId() : null,
                    has ? r.getSubmittedAt() : null));
        }

        return new PageImpl<>(rows, pageable, baseUserIds.size());
    }

    public List<DailySubmitStatResponse> submitStatsByDay(Integer projectId, LocalDate from, LocalDate to) {
        User actor = currentUser();
        requireProjectMemberOrAdmin(projectId, actor);

        long totalMembers = projectMemberRepo.countByProjectId(projectId);
        Map<LocalDate, Long> submittedMap = new HashMap<>();

        for (Object[] row : reportRepo.countSubmittedByDay(projectId, from, to)) {
            LocalDate d = (LocalDate) row[0];
            long c = (long) row[1];
            submittedMap.put(d, c);
        }

        List<DailySubmitStatResponse> result = new ArrayList<>();
        LocalDate cur = from;
        while (!cur.isAfter(to)) {
            long submittedCount = submittedMap.getOrDefault(cur, 0L);
            result.add(new DailySubmitStatResponse(cur, totalMembers, submittedCount,
                    Math.max(0, totalMembers - submittedCount)));
            cur = cur.plusDays(1);
        }
        return result;
    }

    public void exportProjectCsv(Integer projectId,
            Integer userId,
            Integer groupId,
            Integer leaderUserId,
            DailyReportStatus status,
            LocalDate reportFrom,
            LocalDate reportTo,
            List<Integer> reportIds,
            OutputStream os) throws IOException {

        User actor = currentUser();
        requireProjectMemberOrAdmin(projectId, actor);

        Specification<DailyReport> spec = Specification.where(hasProjectId(projectId))
                .and(hasUserId(userId))
                .and(hasStatus(status))
                .and(reportDateBetween(reportFrom, reportTo))
                .and(hasReportIdsIn(reportIds));

        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
        w.write("reportId,projectId,projectName,userId,username,fullName,status,reportDate,submittedAt,updatedAt,content,tasks\n");

        int page = 0;
        while (true) {
            Page<DailyReport> p = reportRepo.findAll(spec,
                    PageRequest.of(page, 500, Sort.by(Sort.Direction.DESC, "updatedAt")));
            if (p.isEmpty())
                break;

            for (DailyReport r : p.getContent()) {
                String tasks = (r.getTaskItems() == null || r.getTaskItems().isEmpty())
                        ? ""
                        : r.getTaskItems().stream()
                                .map(i -> i.getTask().getId() + ":" + safe(i.getTask().getTitle()))
                                .collect(Collectors.joining(";"));

                w.write(csv(r.getReportId()));
                w.write(",");
                w.write(csv(r.getProject().getId()));
                w.write(",");
                w.write(csv(r.getProject().getName()));
                w.write(",");
                w.write(csv(r.getUser().getId()));
                w.write(",");
                w.write(csv(r.getUser().getUsername()));
                w.write(",");
                w.write(csv(r.getUser().getFullName()));
                w.write(",");
                w.write(csv(r.getStatus().name()));
                w.write(",");
                w.write(csv(r.getReportDate()));
                w.write(",");
                w.write(csv(r.getSubmittedAt()));
                w.write(",");
                w.write(csv(r.getUpdatedAt()));
                w.write(",");
                w.write(csv(r.getContent()));
                w.write(",");
                w.write(csv(tasks));
                w.write("\n");
            }

            if (!p.hasNext())
                break;
            page++;
        }

        w.flush();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String csv(Object v) {
        if (v == null)
            return "";
        String s = String.valueOf(v);
        s = s.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
