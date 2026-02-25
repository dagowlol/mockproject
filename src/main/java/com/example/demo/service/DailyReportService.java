package com.example.demo.service;

import com.example.demo.dto.request.DailyReportTaskItemRequest;
import com.example.demo.dto.request.UpsertDailyReportDraftRequest;
import com.example.demo.dto.response.dailyReport.DailyReportResponse;
import com.example.demo.exception.AppException;
import com.example.demo.mapper.DailyReportMapper;
import com.example.demo.model.*;
import com.example.demo.repo.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.demo.repo.specification.DailyReportSpecification.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DailyReportService {

    private final DailyReportRepository reportRepo;
    private final DailyReportTaskItemRepository itemRepo;
    private final ProjectRepository projectRepo;
    private final ProjectMemberRepository projectMemberRepo;
    private final UserRepository userRepo;
    private final UserLeaderRepository userLeaderRepo;
    private final TaskRepository taskRepo;

    private final NotificationPublisher notificationPublisher;
    private final DailyReportMapper mapper;

    private final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private boolean isAdmin(User u) {
        String r = u.getRoleName();
        return "ADMIN".equalsIgnoreCase(r) || "SUPER_ADMIN".equalsIgnoreCase(r);
    }

    private void requireProjectMemberOrAdmin(Integer projectId, User actor) {
        if (isAdmin(actor))
            return;

        if (!projectMemberRepo.existsByProject_IdAndUser_Id(projectId, actor.getId())) {
            log.info("projectid: " + projectId);
            log.info("userid: " + actor.getId());
            throw new AppException(HttpStatus.FORBIDDEN, "User not in project");
        }
    }

    private void requireOwnerAndDraft(DailyReport r, User actor) {
        if (!java.util.Objects.equals(r.getUser().getId(), actor.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Unauthorized");
        }
        if (r.getStatus() != DailyReportStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only DRAFT can be edited/submitted.");
        }
    }

    private void validateContentOrTask(UpsertDailyReportDraftRequest req, boolean isSubmit) {
        boolean hasContent = req != null && req.content() != null && !req.content().trim().isEmpty();
        boolean hasTasks = req != null && req.taskItems() != null && !req.taskItems().isEmpty();

        if (isSubmit && !hasContent && !hasTasks) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Require summary(content) or at least 1 task item.");
        }
    }

    private void validateTaskSelection(Integer projectId, Integer userId, List<DailyReportTaskItemRequest> items) {
        if (items == null || items.isEmpty())
            return;

        Set<Integer> reqIds = items.stream()
                .map(DailyReportTaskItemRequest::taskId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (reqIds.isEmpty())
            return;

        Set<Integer> allowed = taskRepo.findSelectableTaskIdsForUserInProject(projectId, userId);
        if (!allowed.containsAll(reqIds)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Some tasks are not selectable.");
        }
    }

    @Transactional
    public DailyReportResponse getOrCreateDraft(Integer projectId) {
        User actor = currentUser();
        requireProjectMemberOrAdmin(projectId, actor);

        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Project not found"));

        Optional<DailyReport> existing = reportRepo
                .findFirstByUser_IdAndProject_IdAndStatusOrderByUpdatedAtDesc(actor.getId(), projectId,
                        DailyReportStatus.DRAFT);

        if (existing.isPresent()) {
            return mapper.toResponse(existing.get());
        }

        DailyReport draft = DailyReport.builder()
                .user(actor)
                .project(project)
                .status(DailyReportStatus.DRAFT)
                .reportDate(null)
                .content("")
                .build();

        return mapper.toResponse(reportRepo.save(draft));
    }

    @Transactional
    public DailyReportResponse updateDraft(Integer reportId, UpsertDailyReportDraftRequest req) {
        User actor = currentUser();

        DailyReport r = reportRepo.findById(reportId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Daily report not found"));

        requireOwnerAndDraft(r, actor);

        if (req.projectId() != null && !r.getProject().getId().equals(req.projectId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Project mismatch.");
        }

        validateTaskSelection(r.getProject().getId(), actor.getId(), req.taskItems());

        r.setContent(req.content());

        // Synchronize task items to avoid unique constraint violations
        List<DailyReportTaskItem> existingItems = new ArrayList<>(r.getTaskItems());
        Set<Integer> requestedTaskIds = req.taskItems() == null ? new HashSet<>()
                : req.taskItems().stream().map(DailyReportTaskItemRequest::taskId).collect(Collectors.toSet());

        // Remove items not in the request
        existingItems.removeIf(item -> {
            if (!requestedTaskIds.contains(item.getTask().getId())) {
                r.getTaskItems().remove(item);
                return true;
            }
            return false;
        });

        // Update or add items
        if (req.taskItems() != null) {
            for (DailyReportTaskItemRequest it : req.taskItems()) {
                Task t = taskRepo.findById(it.taskId())
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Task not found"));

                // Find existing item for this task
                DailyReportTaskItem existingItem = r.getTaskItems().stream()
                        .filter(item -> item.getTask().getId().equals(it.taskId()))
                        .findFirst()
                        .orElse(null);

                if (existingItem != null) {
                    // Update existing item
                    existingItem.setProgressNote(it.progressNote());
                    existingItem.setTimeSpentMinutes(it.timeSpentMinutes());
                } else {
                    // Add new item
                    DailyReportTaskItem newItem = DailyReportTaskItem.builder()
                            .report(r)
                            .task(t)
                            .progressNote(it.progressNote())
                            .timeSpentMinutes(it.timeSpentMinutes())
                            .build();
                    r.getTaskItems().add(newItem);
                }
            }
        }

        return mapper.toResponse(reportRepo.save(r));
    }

    @Transactional
    public DailyReportResponse submit(Integer reportId) {
        User actor = currentUser();

        DailyReport r = reportRepo.findById(reportId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Daily report not found"));

        requireOwnerAndDraft(r, actor);

        UpsertDailyReportDraftRequest snapshotReq = new UpsertDailyReportDraftRequest(
                r.getProject().getId(),
                r.getContent(),
                r.getTaskItems() == null ? List.of()
                        : r.getTaskItems().stream()
                                .map(i -> new DailyReportTaskItemRequest(i.getTask().getId(), i.getProgressNote(),
                                        i.getTimeSpentMinutes()))
                                .collect(Collectors.toList()));
        validateContentOrTask(snapshotReq, true);

        LocalDate today = LocalDate.now(ZONE);
        if (reportRepo.existsByUser_IdAndProject_IdAndReportDateAndStatus(
                actor.getId(), r.getProject().getId(), today, DailyReportStatus.SUBMITTED)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Already submitted today for this project.");
        }

        r.setStatus(DailyReportStatus.SUBMITTED);
        r.setSubmittedAt(LocalDateTime.now(ZONE));
        r.setReportDate(today);

        DailyReport saved = reportRepo.save(r);

        // Notify leaders (simplified logic)
        List<UserLeader> leaders = userLeaderRepo.findByUserId(actor.getId());
        if (leaders != null && !leaders.isEmpty()) {
            Set<Integer> leaderIds = leaders.stream().map(ul -> ul.getLeaderId()).collect(Collectors.toSet());

            notificationPublisher.notifyUsers(
                    actor.getId(),
                    leaderIds,
                    "DAILY_REPORT_SUBMITTED",
                    "DAILY_REPORT",
                    saved.getReportId().toString(),
                    "New Daily Report",
                    String.format("%s submitted daily report for project '%s' (%s).",
                            actor.getFullName(),
                            saved.getProject().getName(),
                            today));
        }

        return mapper.toResponse(saved);
    }

    public Page<DailyReportResponse> listMine(Integer projectId,
            DailyReportStatus status,
            LocalDate reportFrom,
            LocalDate reportTo,
            LocalDateTime submittedFrom,
            LocalDateTime submittedTo,
            LocalDateTime updatedFrom,
            LocalDateTime updatedTo,
            Pageable pageable) {

        User actor = currentUser();
        if (projectId == null) {
            Specification<DailyReport> spec = Specification.where(hasUserId(actor.getId()))
                    .and(hasStatus(status))
                    .and(reportDateBetween(reportFrom, reportTo))
                    .and(submittedAtBetween(submittedFrom, submittedTo))
                    .and(updatedAtBetween(updatedFrom, updatedTo));

            Page<DailyReport> page = reportRepo.findAll(spec, pageable);
            return page.map(r -> mapper.toResponse(r));
        }
        Specification<DailyReport> spec = Specification.where(hasUserId(actor.getId()))
                .and(hasProjectId(projectId))
                .and(hasStatus(status))
                .and(reportDateBetween(reportFrom, reportTo))
                .and(submittedAtBetween(submittedFrom, submittedTo))
                .and(updatedAtBetween(updatedFrom, updatedTo));

        Page<DailyReport> page = reportRepo.findAll(spec, pageable);
        return page.map(r -> mapper.toResponse(r));
    }

    public DailyReportResponse getById(Integer reportId) {
        User actor = currentUser();
        DailyReport r = reportRepo.findById(reportId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Daily report not found"));

        if (!isAdmin(actor) && !r.getUser().getId().equals(actor.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return mapper.toResponse(r);
    }

    public List<com.example.demo.dto.response.dailyReport.SimpleTaskResponse> listTasksByProject(Integer projectId) {
        User actor = currentUser();
        requireProjectMemberOrAdmin(projectId, actor);

        return taskRepo.findByProjectId(projectId).stream()
                .map(t -> new com.example.demo.dto.response.dailyReport.SimpleTaskResponse(t.getId(), t.getTitle(),
                        t.getStatus() != null ? t.getStatus().name() : null))
                .collect(Collectors.toList());
    }
}
