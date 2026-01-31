package com.example.demo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.demo.dto.request.AddTargetsRequest;
import com.example.demo.dto.request.CreateEvaluationRoundRequest;
import com.example.demo.dto.request.ManualAssignmentItemRequest;
import com.example.demo.dto.request.ManualAssignmentRequest;
import com.example.demo.dto.response.AutoAssignResultResponse;
import com.example.demo.dto.response.EvaluationAssignmentProgressRowResponse;
import com.example.demo.dto.response.EvaluationCriterionScoreResponse;
import com.example.demo.dto.response.EvaluationRankingRowResponse;
import com.example.demo.dto.response.EvaluationRoundDetailResponse;
import com.example.demo.dto.response.EvaluationRoundProgressResponse;
import com.example.demo.dto.response.EvaluationRoundResponse;
import com.example.demo.dto.response.EvaluationRoundSummaryResponse;
import com.example.demo.dto.response.EvaluationScoreDistributionResponse;
import com.example.demo.dto.response.EvaluationScoreSummaryResponse;
import com.example.demo.dto.response.EvaluationSummaryHeaderResponse;
import com.example.demo.dto.response.EvaluatorProgressResponse;
import com.example.demo.dto.response.ManualAssignmentLeaderOptionResponse;
import com.example.demo.dto.response.ManualAssignmentRowResponse;
import com.example.demo.dto.response.MemberEvaluationResultResponse;
import com.example.demo.dto.response.MemberEvaluationSummaryResponse;
import com.example.demo.dto.response.TargetCandidateResponse;
import com.example.demo.dto.response.TargetGenerationResponse;
import com.example.demo.exception.AppException;
import com.example.demo.mapper.EvaluationRoundMapper;
import com.example.demo.model.EvaluationAssignment;
import com.example.demo.model.EvaluationAssignmentStatus;
import com.example.demo.model.EvaluationForm;
import com.example.demo.model.EvaluationRound;
import com.example.demo.model.EvaluationRoundCriterion;
import com.example.demo.model.EvaluationRoundStatus;
import com.example.demo.model.EvaluationRoundType;
import com.example.demo.model.EvaluationScore;
import com.example.demo.model.EvaluationTarget;
import com.example.demo.model.EvaluationTargetStatus;
import com.example.demo.model.Notification;
import com.example.demo.model.User;
import com.example.demo.model.UserLeader;
import com.example.demo.repo.EvaluationAssignmentRepository;
import com.example.demo.repo.EvaluationFormRepository;
import com.example.demo.repo.EvaluationRoundCriterionRepository;
import com.example.demo.repo.EvaluationRoundRepository;
import com.example.demo.repo.EvaluationTargetRepository;
import com.example.demo.repo.NotificationRepository;
import com.example.demo.repo.ProjectMemberRepository;
import com.example.demo.repo.UserLeaderRepository;
import com.example.demo.repo.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@org.springframework.transaction.annotation.Transactional
public class EvaluationRoundService {
    private final EvaluationRoundRepository evaluationRoundRepository;
    private final EvaluationTargetRepository evaluationTargetRepository;
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;
    private final EvaluationFormRepository evaluationFormRepository;
    private final UserLeaderRepository userLeaderRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final EvaluationRoundMapper evaluationRoundMapper;
    private final EvaluationRoundCriteriaService evaluationRoundCriteriaService;
    private final EvaluationRoundCriterionRepository evaluationRoundCriterionRepository;
    private final com.example.demo.repo.EvaluationScoreRepository evaluationScoreRepository;

    public EvaluationRoundService(
            EvaluationRoundRepository evaluationRoundRepository,
            EvaluationTargetRepository evaluationTargetRepository,
            EvaluationAssignmentRepository evaluationAssignmentRepository,
            EvaluationFormRepository evaluationFormRepository,
            UserLeaderRepository userLeaderRepository,
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository,
            EvaluationRoundMapper evaluationRoundMapper,
            EvaluationRoundCriteriaService evaluationRoundCriteriaService,
            EvaluationRoundCriterionRepository evaluationRoundCriterionRepository,
            com.example.demo.repo.EvaluationScoreRepository evaluationScoreRepository) {
        this.evaluationRoundRepository = evaluationRoundRepository;
        this.evaluationTargetRepository = evaluationTargetRepository;
        this.evaluationAssignmentRepository = evaluationAssignmentRepository;
        this.evaluationFormRepository = evaluationFormRepository;
        this.userLeaderRepository = userLeaderRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.evaluationRoundMapper = evaluationRoundMapper;
        this.evaluationRoundCriteriaService = evaluationRoundCriteriaService;
        this.evaluationRoundCriterionRepository = evaluationRoundCriterionRepository;
        this.evaluationScoreRepository = evaluationScoreRepository;
    }

    public EvaluationRoundResponse create(CreateEvaluationRoundRequest req, Integer actorId) {
        validateRequest(req);

        EvaluationRound round = new EvaluationRound();
        round.setName(req.getName().trim());
        round.setType(req.getType());
        round.setPeriodFrom(req.getPeriodFrom());
        round.setPeriodTo(req.getPeriodTo());
        round.setProjectId(req.getProjectId());
        round.setScoringScale(req.getScoringScale());
        round.setDescription(req.getDescription().trim());
        round.setStatus(EvaluationRoundStatus.DRAFT);
        round.setIsTemplateRound(false);
        round.setCreatedBy(actorId);
        round.setUpdatedBy(actorId);

        EvaluationRound saved = evaluationRoundRepository.save(round);
        if (req.getCriteriaRoundId() != null) {
            evaluationRoundCriteriaService.applyCriteriaFromRound(req.getCriteriaRoundId(), saved.getId(), actorId);
        }
        return evaluationRoundMapper.toResponse(saved);
    }

    public List<EvaluationRoundResponse> list() {
        return evaluationRoundRepository.findByIsTemplateRoundFalseOrderByCreatedAtDesc()
                .stream()
                .map(round -> {
                    EvaluationRoundResponse response = evaluationRoundMapper.toResponse(round);
                    int total = evaluationTargetRepository.countByRoundId(round.getId());
                    int completed = (int) evaluationAssignmentRepository.countCompletedByRoundId(round.getId());
                    log.info("Round ID {}: total targets = {}, completed evaluations = {}", round.getId(), total,
                            completed);
                    response.setTotalEvaluations(total);
                    response.setCompletedEvaluations(completed);
                    return response;
                })
                .collect(Collectors.toList());
    }

    public EvaluationRoundDetailResponse getDetail(Integer roundId) {
        EvaluationRound round = evaluationRoundRepository.findById(roundId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Evaluation round not found"));

        EvaluationRoundDetailResponse response = new EvaluationRoundDetailResponse();
        response.setRound(evaluationRoundMapper.toResponse(round));
        response.setCriteria(evaluationRoundCriteriaService.listCriteria(roundId));
        response.setTargetCount(evaluationTargetRepository.countByRoundId(roundId));
        response.setCriteriaWeightTotal(evaluationRoundCriterionRepository.sumWeightByRoundId(roundId));
        return response;
    }

    public EvaluationRoundResponse publish(Integer roundId, Integer actorId) {
        EvaluationRound round = evaluationRoundRepository.findById(roundId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Evaluation round not found"));

        if (round.getStatus() != EvaluationRoundStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft rounds can be published");
        }

        Integer totalWeight = evaluationRoundCriterionRepository.sumWeightByRoundId(roundId);
        if (totalWeight == null || totalWeight != 100) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Total criteria weight must equal 100% to publish");
        }

        round.setStatus(EvaluationRoundStatus.IN_PROGRESS);
        round.setUpdatedBy(actorId);
        EvaluationRound saved = evaluationRoundRepository.save(round);
        return evaluationRoundMapper.toResponse(saved);
    }

    public AutoAssignResultResponse autoAssignLeader(Integer roundId, Integer actorId, boolean notify) {
        EvaluationRound round = evaluationRoundRepository.findById(roundId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Evaluation round not found"));

        List<EvaluationTarget> targets = evaluationTargetRepository.findByRoundId(roundId);
        AutoAssignResultResponse result = new AutoAssignResultResponse();
        result.setTargetCount(targets.size());

        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.Set<Integer> evaluatorSet = new java.util.HashSet<>();
        int assignedCount = 0;
        int skippedNoLeader = 0;
        int skippedExisting = 0;

        for (EvaluationTarget target : targets) {
            Integer userId = target.getUserId();
            UserLeader leader = userLeaderRepository.findActiveLeader(userId, today).orElse(null);
            if (leader == null || leader.getLeaderId() == null) {
                skippedNoLeader++;
                continue;
            }
            Integer evaluatorId = leader.getLeaderId();
            evaluatorSet.add(evaluatorId);

            if (evaluationAssignmentRepository.existsByTargetIdAndEvaluatorUserId(target.getId(), evaluatorId)) {
                skippedExisting++;
                continue;
            }

            EvaluationAssignment assignment = new EvaluationAssignment();
            assignment.setTargetId(target.getId());
            assignment.setEvaluatorUserId(evaluatorId);
            assignment.setAssignedBy(actorId);
            assignment.setAssignedAt(java.time.LocalDateTime.now());
            if (round.getPeriodTo() != null) {
                assignment.setDueAt(round.getPeriodTo().atTime(23, 59, 59));
            }
            assignment.setStatus(EvaluationAssignmentStatus.PENDING);
            EvaluationAssignment saved = evaluationAssignmentRepository.save(assignment);
            assignedCount++;

            if (notify) {
                Notification notification = new Notification();
                notification.setRecipientUserId(evaluatorId);
                notification.setType("EVALUATION_ASSIGNMENT");
                notification.setTitle("New evaluation assignment");
                notification.setMessage(
                        "You have been assigned to evaluate user " + userId + " for round " + round.getName() + ".");
                notification.setRefType("evaluation_assignment");
                notification.setRefId(String.valueOf(saved.getId()));
                notificationRepository.save(notification);
            }
        }

        result.setAssignedCount(assignedCount);
        result.setSkippedNoLeader(skippedNoLeader);
        result.setSkippedExisting(skippedExisting);
        result.setEvaluatorCount(evaluatorSet.size());
        return result;
    }

    public List<ManualAssignmentRowResponse> listManualAssignments(Integer roundId, String query) {
        List<EvaluationTarget> targets = evaluationTargetRepository.findByRoundId(roundId);
        String normalizedQuery = normalizeQuery(query);
        List<ManualAssignmentRowResponse> rows = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();

        for (EvaluationTarget target : targets) {
            User user = userRepository.findById(target.getUserId()).orElse(null);
            if (user == null) {
                continue;
            }
            if (normalizedQuery != null) {
                String haystack = (safe(user.getFullName()) + " " + safe(user.getPositionTitle())).toLowerCase();
                if (!haystack.contains(normalizedQuery)) {
                    continue;
                }
            }
            ManualAssignmentRowResponse row = new ManualAssignmentRowResponse();
            row.setTargetId(target.getId());
            row.setUserId(user.getId());
            row.setFullName(user.getFullName());
            row.setPositionTitle(user.getPositionTitle());

            java.util.List<UserLeader> leaders = userLeaderRepository.findActiveLeaders(user.getId(), today);
            log.info("Found {} active leaders for user ID {}", leaders.size(), user.getId());
            java.util.List<ManualAssignmentLeaderOptionResponse> leaderOptions = new java.util.ArrayList<>();
            for (UserLeader leader : leaders) {
                if (leader.getLeaderId() == null) {
                    continue;
                }
                User leaderUser = userRepository.findById(leader.getLeaderId()).orElse(null);
                ManualAssignmentLeaderOptionResponse option = new ManualAssignmentLeaderOptionResponse();
                option.setUserId(leader.getLeaderId());
                option.setFullName(leaderUser != null ? leaderUser.getFullName() : null);
                leaderOptions.add(option);
            }
            row.setLeaderOptions(leaderOptions);

            java.util.List<EvaluationAssignment> assignments = evaluationAssignmentRepository
                    .findByTargetId(target.getId());
            java.util.List<Integer> evaluatorIds = assignments.stream()
                    .map(EvaluationAssignment::getEvaluatorUserId)
                    .filter(id -> id != null)
                    .collect(java.util.stream.Collectors.toList());
            row.setEvaluatorUserIds(evaluatorIds);
            rows.add(row);
        }
        return rows;
    }

    public EvaluationRoundProgressResponse getProgress(Integer roundId) {
        EvaluationRound round = evaluationRoundRepository.findById(roundId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Evaluation round not found"));
        List<EvaluationAssignment> assignments = evaluationAssignmentRepository.findByRoundId(roundId);

        EvaluationRoundProgressResponse response = new EvaluationRoundProgressResponse();
        response.setRoundId(round.getId());
        response.setRoundName(round.getName());
        response.setTotalAssignments(assignments.size());

        if (assignments.isEmpty()) {
            response.setPendingCount(0);
            response.setSubmittedCount(0);
            response.setLockedCount(0);
            response.setEvaluatorProgress(List.of());
            response.setAssignmentRows(List.of());
            return response;
        }

        Set<Integer> targetIds = assignments.stream()
                .map(EvaluationAssignment::getTargetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, EvaluationTarget> targetMap = evaluationTargetRepository.findAllById(targetIds).stream()
                .collect(Collectors.toMap(EvaluationTarget::getId, t -> t));

        Set<Integer> userIds = new HashSet<>();
        for (EvaluationTarget target : targetMap.values()) {
            if (target.getUserId() != null) {
                userIds.add(target.getUserId());
            }
        }
        for (EvaluationAssignment assignment : assignments) {
            if (assignment.getEvaluatorUserId() != null) {
                userIds.add(assignment.getEvaluatorUserId());
            }
        }
        Map<Integer, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<Integer> assignmentIds = assignments.stream()
                .map(EvaluationAssignment::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Integer, EvaluationForm> formMap = evaluationFormRepository.findByAssignmentIdIn(assignmentIds).stream()
                .collect(Collectors.toMap(EvaluationForm::getAssignmentId, f -> f));

        int pendingCount = 0;
        int submittedCount = 0;
        int lockedCount = 0;

        Map<Integer, EvaluatorProgressResponse> evaluatorMap = new HashMap<>();
        List<EvaluationAssignmentProgressRowResponse> rows = new ArrayList<>();

        for (EvaluationAssignment assignment : assignments) {
            EvaluationTarget target = targetMap.get(assignment.getTargetId());
            if (target == null) {
                continue;
            }
            User targetUser = userMap.get(target.getUserId());
            User evaluatorUser = userMap.get(assignment.getEvaluatorUserId());
            EvaluationForm form = formMap.get(assignment.getId());

            String status = assignment.getStatus() != null ? assignment.getStatus().name() : "PENDING";
            if (EvaluationAssignmentStatus.SUBMITTED.name().equals(status)) {
                submittedCount++;
            } else if (EvaluationAssignmentStatus.LOCKED.name().equals(status)) {
                lockedCount++;
            } else {
                pendingCount++;
            }

            Integer evaluatorId = assignment.getEvaluatorUserId();
            if (evaluatorId != null) {
                EvaluatorProgressResponse evaluatorProgress = evaluatorMap.get(evaluatorId);
                if (evaluatorProgress == null) {
                    evaluatorProgress = new EvaluatorProgressResponse();
                    evaluatorProgress.setEvaluatorUserId(evaluatorId);
                    evaluatorProgress.setEvaluatorName(evaluatorUser != null ? evaluatorUser.getFullName() : null);
                    evaluatorProgress.setEvaluatorPositionTitle(
                            evaluatorUser != null ? evaluatorUser.getPositionTitle() : null);
                    evaluatorMap.put(evaluatorId, evaluatorProgress);
                }
                evaluatorProgress.setTotalCount(evaluatorProgress.getTotalCount() + 1);
                if (EvaluationAssignmentStatus.SUBMITTED.name().equals(status)) {
                    evaluatorProgress.setSubmittedCount(evaluatorProgress.getSubmittedCount() + 1);
                } else if (EvaluationAssignmentStatus.LOCKED.name().equals(status)) {
                    evaluatorProgress.setLockedCount(evaluatorProgress.getLockedCount() + 1);
                } else {
                    evaluatorProgress.setPendingCount(evaluatorProgress.getPendingCount() + 1);
                }
            }

            EvaluationAssignmentProgressRowResponse row = new EvaluationAssignmentProgressRowResponse();
            row.setAssignmentId(assignment.getId());
            row.setTargetUserId(target.getUserId());
            row.setEmployeeName(targetUser != null ? targetUser.getFullName() : null);
            row.setPositionTitle(targetUser != null ? targetUser.getPositionTitle() : null);
            row.setEvaluatorUserId(assignment.getEvaluatorUserId());
            row.setEvaluatorName(evaluatorUser != null ? evaluatorUser.getFullName() : null);
            row.setStatus(status);
            row.setDueAt(assignment.getDueAt());
            row.setSubmittedAt(form != null ? form.getSubmittedAt() : null);
            row.setLockedAt(form != null ? form.getLockedAt() : null);
            rows.add(row);
        }

        List<EvaluatorProgressResponse> evaluatorProgressRows = new ArrayList<>(evaluatorMap.values());
        evaluatorProgressRows.sort(Comparator.comparing(
                r -> r.getEvaluatorName() == null ? "" : r.getEvaluatorName().toLowerCase()));
        rows.sort(Comparator.comparing(
                r -> r.getEmployeeName() == null ? "" : r.getEmployeeName().toLowerCase()));

        response.setPendingCount(pendingCount);
        response.setSubmittedCount(submittedCount);
        response.setLockedCount(lockedCount);
        response.setEvaluatorProgress(evaluatorProgressRows);
        response.setAssignmentRows(rows);
        return response;
    }

    public EvaluationRoundSummaryResponse getSummary(Integer roundId) {
        EvaluationRound round = evaluationRoundRepository.findById(roundId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Evaluation round not found"));

        List<EvaluationRound> rounds = evaluationRoundRepository.findByIsTemplateRoundFalseOrderByCreatedAtDesc();
        EvaluationSummaryHeaderResponse header = new EvaluationSummaryHeaderResponse();
        header.setOngoingRounds(
                (int) rounds.stream().filter(r -> r.getStatus() == EvaluationRoundStatus.IN_PROGRESS).count());
        header.setAwaitingApprovalRounds(
                (int) rounds.stream().filter(r -> r.getStatus() == EvaluationRoundStatus.DRAFT).count());
        header.setTotalAssignments((int) evaluationAssignmentRepository.count());
        header.setCompletedAssignments((int) evaluationAssignmentRepository.countCompletedAll());

        EvaluationRoundSummaryResponse response = new EvaluationRoundSummaryResponse();
        response.setRoundId(round.getId());
        response.setRoundName(round.getName());
        response.setHeader(header);

        List<EvaluationAssignment> assignments = evaluationAssignmentRepository.findByRoundId(roundId);
        if (assignments.isEmpty()) {
            response.setScoreSummary(emptyScoreSummary());
            response.setDistribution(defaultDistribution());
            response.setRanking(List.of());
            return response;
        }

        Map<Integer, EvaluationTarget> targetMap = evaluationTargetRepository.findByRoundId(roundId).stream()
                .collect(Collectors.toMap(EvaluationTarget::getId, t -> t));
        if (targetMap.isEmpty()) {
            response.setScoreSummary(emptyScoreSummary());
            response.setDistribution(defaultDistribution());
            response.setRanking(List.of());
            return response;
        }

        List<Integer> assignmentIds = assignments.stream()
                .map(EvaluationAssignment::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Integer, EvaluationForm> formMap = evaluationFormRepository.findByAssignmentIdIn(assignmentIds).stream()
                .collect(Collectors.toMap(EvaluationForm::getAssignmentId, f -> f));

        Map<Integer, TargetAggregate> aggregateMap = new HashMap<>();
        Map<Integer, Integer> formToUser = new HashMap<>();
        List<Integer> formIds = new ArrayList<>();

        for (EvaluationAssignment assignment : assignments) {
            if (assignment.getStatus() != EvaluationAssignmentStatus.SUBMITTED
                    && assignment.getStatus() != EvaluationAssignmentStatus.LOCKED) {
                continue;
            }
            EvaluationForm form = formMap.get(assignment.getId());
            if (form == null || form.getTotalScore() == null) {
                continue;
            }
            EvaluationTarget target = targetMap.get(assignment.getTargetId());
            if (target == null || target.getUserId() == null) {
                continue;
            }
            Integer userId = target.getUserId();
            TargetAggregate aggregate = aggregateMap.computeIfAbsent(userId, id -> new TargetAggregate(id));
            aggregate.getFormIds().add(form.getId());
            aggregate.getScores().add(form.getTotalScore());
            formToUser.put(form.getId(), userId);
            formIds.add(form.getId());
        }

        if (aggregateMap.isEmpty()) {
            response.setScoreSummary(emptyScoreSummary());
            response.setDistribution(defaultDistribution());
            response.setRanking(List.of());
            return response;
        }

        Map<Integer, User> userMap = userRepository.findAllById(aggregateMap.keySet()).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        for (TargetAggregate aggregate : aggregateMap.values()) {
            User user = userMap.get(aggregate.getUserId());
            aggregate.setFullName(user != null ? user.getFullName() : null);
            aggregate.setPositionTitle(user != null ? user.getPositionTitle() : null);
        }

        List<EvaluationRoundCriterion> criteria = evaluationRoundCriterionRepository
                .findByRoundIdOrderByDisplayOrderAsc(roundId);
        int maxTotalScore = criteria.stream()
                .map(EvaluationRoundCriterion::getMaxScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        int scoringScale = round.getScoringScale() != null ? round.getScoringScale() : 100;
        Map<Integer, EvaluationRoundCriterion> criteriaMap = criteria.stream()
                .collect(Collectors.toMap(EvaluationRoundCriterion::getId, c -> c));

        Map<Integer, Map<Integer, List<Integer>>> targetCriterionScores = new HashMap<>();
        if (!formIds.isEmpty()) {
            List<EvaluationScore> scores = evaluationScoreRepository.findByFormIdIn(formIds);
            for (EvaluationScore score : scores) {
                Integer userId = formToUser.get(score.getFormId());
                if (userId == null || score.getCriterionId() == null) {
                    continue;
                }
                targetCriterionScores
                        .computeIfAbsent(userId, id -> new HashMap<>())
                        .computeIfAbsent(score.getCriterionId(), id -> new ArrayList<>())
                        .add(score.getScoreValue());
            }
        }

        List<EvaluationRankingRowResponse> ranking = new ArrayList<>();
        List<Integer> targetScores = new ArrayList<>();
        List<Double> normalizedScores = new ArrayList<>();
        List<EvaluationScoreDistributionResponse> distribution = defaultDistribution();

        for (TargetAggregate aggregate : aggregateMap.values()) {
            double avg = averageScore(aggregate.getScores());
            double normalized = maxTotalScore > 0 ? (avg / maxTotalScore) * scoringScale : avg;
            int roundedTotal = (int) Math.round(normalized);
            targetScores.add(roundedTotal);
            normalizedScores.add(normalized);

            EvaluationRankingRowResponse row = new EvaluationRankingRowResponse();
            row.setUserId(aggregate.getUserId());
            row.setFullName(aggregate.getFullName());
            row.setPositionTitle(aggregate.getPositionTitle());
            row.setTotalScore(roundedTotal);
            row.setCriteriaScores(buildCriteriaScores(criteria, targetCriterionScores.get(aggregate.getUserId())));
            ranking.add(row);

            incrementDistribution(distribution, roundedTotal);
        }

        ranking.sort(Comparator
                .comparing(EvaluationRankingRowResponse::getTotalScore, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(r -> r.getFullName() == null ? "" : r.getFullName().toLowerCase()));
        int rank = 1;
        for (EvaluationRankingRowResponse row : ranking) {
            row.setRank(rank++);
        }

        EvaluationScoreSummaryResponse scoreSummary = new EvaluationScoreSummaryResponse();
        scoreSummary.setEvaluatedCount(targetScores.size());
        scoreSummary.setAverageScore(roundToOneDecimal(averageScoreDouble(normalizedScores)));
        scoreSummary.setHighestScore(targetScores.stream().max(Integer::compareTo).orElse(0));
        scoreSummary.setLowestScore(targetScores.stream().min(Integer::compareTo).orElse(0));

        response.setScoreSummary(scoreSummary);
        response.setDistribution(distribution);
        response.setRanking(ranking);
        return response;
    }

    public List<User> listEvaluators(String query) {
        List<User> users = userRepository.findByStatusIgnoreCase("ACTIVE");
        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery == null) {
            return users;
        }
        return users.stream()
                .filter(user -> {
                    String haystack = (safe(user.getFullName()) + " " + safe(user.getPositionTitle()) + " "
                            + safe(user.getEmail()))
                            .toLowerCase();
                    return haystack.contains(normalizedQuery);
                })
                .collect(Collectors.toList());
    }

    public AutoAssignResultResponse manualAssign(Integer roundId, ManualAssignmentRequest req, Integer actorId) {
        EvaluationRound round = evaluationRoundRepository.findById(roundId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Evaluation round not found"));
        boolean notify = Boolean.TRUE.equals(req.getNotify());

        AutoAssignResultResponse result = new AutoAssignResultResponse();
        int assigned = 0;
        int skippedExisting = 0;
        java.util.Set<Integer> evaluatorSet = new java.util.HashSet<>();
        java.time.LocalDate today = java.time.LocalDate.now();

        for (ManualAssignmentItemRequest item : req.getAssignments()) {
            Integer targetId = item.getTargetId();
            java.util.List<Integer> evaluatorIds = item.getEvaluatorUserIds();

            EvaluationTarget target = evaluationTargetRepository.findById(targetId)
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Target not found"));
            if (!roundId.equals(target.getRoundId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Target does not belong to the round");
            }

            if (evaluatorIds == null || evaluatorIds.isEmpty()) {
                evaluationAssignmentRepository.deleteByTargetId(targetId);
                continue;
            }

            java.util.List<UserLeader> leaders = userLeaderRepository.findActiveLeaders(target.getUserId(), today);
            java.util.Set<Integer> allowed = new java.util.HashSet<>();
            for (UserLeader leader : leaders) {
                if (leader.getLeaderId() != null) {
                    allowed.add(leader.getLeaderId());
                }
            }
            for (Integer evaluatorId : evaluatorIds) {
                if (!allowed.contains(evaluatorId)) {
                    throw new AppException(HttpStatus.BAD_REQUEST, "Evaluator must be the target's active leader");
                }
            }

            java.util.List<EvaluationAssignment> existingAssignments = evaluationAssignmentRepository
                    .findByTargetId(targetId);
            java.util.Set<Integer> existingIds = existingAssignments.stream()
                    .map(EvaluationAssignment::getEvaluatorUserId)
                    .filter(id -> id != null)
                    .collect(java.util.stream.Collectors.toSet());

            for (EvaluationAssignment existing : existingAssignments) {
                if (existing.getEvaluatorUserId() == null || !evaluatorIds.contains(existing.getEvaluatorUserId())) {
                    evaluationAssignmentRepository.deleteByTargetIdAndEvaluatorUserId(
                            targetId,
                            existing.getEvaluatorUserId());
                }
            }

            for (Integer evaluatorId : evaluatorIds) {
                evaluatorSet.add(evaluatorId);
                if (existingIds.contains(evaluatorId)) {
                    skippedExisting++;
                    continue;
                }
                EvaluationAssignment assignment = new EvaluationAssignment();
                assignment.setTargetId(targetId);
                assignment.setEvaluatorUserId(evaluatorId);
                assignment.setAssignedBy(actorId);
                assignment.setAssignedAt(java.time.LocalDateTime.now());
                if (round.getPeriodTo() != null) {
                    assignment.setDueAt(round.getPeriodTo().atTime(23, 59, 59));
                }
                assignment.setStatus(EvaluationAssignmentStatus.PENDING);
                EvaluationAssignment saved = evaluationAssignmentRepository.save(assignment);
                assigned++;

                if (notify) {
                    Integer targetUserId = target.getUserId();
                    Notification notification = new Notification();
                    notification.setRecipientUserId(evaluatorId);
                    notification.setType("EVALUATION_ASSIGNMENT");
                    notification.setTitle("New evaluation assignment");
                    notification.setMessage("You have been assigned to evaluate user " + safeId(targetUserId)
                            + " for round " + round.getName() + ".");
                    notification.setRefType("evaluation_assignment");
                    notification.setRefId(String.valueOf(saved.getId()));
                    notificationRepository.save(notification);
                }
            }
        }

        result.setAssignedCount(assigned);
        result.setSkippedExisting(skippedExisting);
        result.setEvaluatorCount(evaluatorSet.size());
        return result;
    }

    public List<TargetCandidateResponse> listTargetCandidates(Integer roundId, String query, String sort) {
        EvaluationRound round = evaluationRoundRepository.findById(roundId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Evaluation round not found"));

        List<User> candidates = new java.util.ArrayList<>();
        if (round.getType() == EvaluationRoundType.PROJECT) {
            if (round.getProjectId() == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Project is required for PROJECT evaluation rounds.");
            }
            projectMemberRepository.findByProjectId(round.getProjectId()).forEach(member -> {
                if (member.getUser() != null) {
                    candidates.add(member.getUser());
                }
            });
        } else {
            candidates.addAll(userRepository.findByStatusIgnoreCase("ACTIVE"));
        }

        java.util.Set<Integer> existing = evaluationTargetRepository.findByRoundId(roundId)
                .stream()
                .map(EvaluationTarget::getUserId)
                .collect(java.util.stream.Collectors.toSet());

        String normalizedQuery = normalizeQuery(query);
        List<User> filtered = candidates.stream()
                .filter(user -> !existing.contains(user.getId()))
                .filter(user -> {
                    if (normalizedQuery == null) {
                        return true;
                    }
                    String haystack = (safe(user.getFullName()) + " " + safe(user.getPositionTitle()) + " "
                            + safe(user.getEmail()))
                            .toLowerCase();
                    return haystack.contains(normalizedQuery);
                })
                .collect(Collectors.toList());

        sortCandidates(filtered, sort);

        return filtered.stream().map(user -> {
            TargetCandidateResponse response = new TargetCandidateResponse();
            response.setUserId(user.getId());
            response.setFullName(user.getFullName());
            response.setPositionTitle(user.getPositionTitle());
            response.setEmail(user.getEmail());
            return response;
        }).collect(Collectors.toList());
    }

    public MemberEvaluationSummaryResponse getMemberEvaluationSummary(Integer userId) {
        List<EvaluationTarget> targets = evaluationTargetRepository.findByUserId(userId);
        List<MemberEvaluationResultResponse> history = new ArrayList<>();

        for (EvaluationTarget target : targets) {
            EvaluationRound round = evaluationRoundRepository.findById(target.getRoundId()).orElse(null);
            if (round == null)
                continue;

            List<EvaluationAssignment> assignments = evaluationAssignmentRepository.findByTargetId(target.getId());
            List<Integer> formIds = new ArrayList<>();
            List<Integer> evaluatorIds = new ArrayList<>();
            for (EvaluationAssignment assignment : assignments) {
                if (assignment.getStatus() == EvaluationAssignmentStatus.SUBMITTED
                        || assignment.getStatus() == EvaluationAssignmentStatus.LOCKED) {
                    evaluationFormRepository.findByAssignmentId(assignment.getId())
                            .ifPresent(form -> formIds.add(form.getId()));
                    if (assignment.getEvaluatorUserId() != null) {
                        evaluatorIds.add(assignment.getEvaluatorUserId());
                    }
                }
            }

            if (formIds.isEmpty())
                continue;

            List<EvaluationForm> forms = evaluationFormRepository.findAllById(formIds);
            double avgScore = forms.stream().mapToDouble(EvaluationForm::getTotalScore).average().orElse(0.0);
            String comments = forms.stream()
                    .map(EvaluationForm::getComment)
                    .filter(Objects::nonNull)
                    .filter(c -> !c.isBlank())
                    .collect(Collectors.joining("; "));

            List<EvaluationRoundCriterion> criteria = evaluationRoundCriterionRepository
                    .findByRoundIdOrderByDisplayOrderAsc(round.getId());
            int maxTotalScore = criteria.stream().mapToInt(c -> c.getMaxScore() != null ? c.getMaxScore() : 0).sum();
            int scoringScale = round.getScoringScale() != null ? round.getScoringScale() : 100;
            double normalized = maxTotalScore > 0 ? (avgScore / maxTotalScore) * scoringScale : avgScore;

            MemberEvaluationResultResponse result = new MemberEvaluationResultResponse();
            result.setRoundId(round.getId());
            result.setRoundName(round.getName());
            result.setPeriodFrom(round.getPeriodFrom());
            result.setPeriodTo(round.getPeriodTo());
            int finalScore = (int) Math.round(normalized);
            result.setTotalScore(finalScore);
            result.setNote(comments);
            result.setRating(getRatingLabel(finalScore));

            if (!evaluatorIds.isEmpty()) {
                String names = userRepository.findAllById(evaluatorIds).stream()
                        .map(User::getFullName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(", "));
                result.setEvaluatorName(names);
            }

            LocalDateTime latestSubmission = forms.stream()
                    .map(f -> f.getSubmittedAt() != null ? f.getSubmittedAt() : f.getCreatedAt())
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            if (latestSubmission != null) {
                result.setEvaluationDate(latestSubmission.toLocalDate());
            }

            List<EvaluationScore> scores = evaluationScoreRepository.findByFormIdIn(formIds);
            Map<Integer, List<Integer>> criterionScoreMap = new HashMap<>();
            for (EvaluationScore score : scores) {
                if (score.getCriterionId() != null) {
                    criterionScoreMap.computeIfAbsent(score.getCriterionId(), id -> new ArrayList<>())
                            .add(score.getScoreValue());
                }
            }
            result.setCriteriaScores(buildCriteriaScores(criteria, criterionScoreMap));

            history.add(result);
        }

        history.sort(Comparator.comparing(MemberEvaluationResultResponse::getPeriodTo,
                Comparator.nullsLast(Comparator.reverseOrder())));

        MemberEvaluationSummaryResponse response = new MemberEvaluationSummaryResponse();
        response.setHistory(history);
        if (!history.isEmpty()) {
            response.setLatest(history.get(0));
            double totalAvg = history.stream().mapToInt(MemberEvaluationResultResponse::getTotalScore).average()
                    .orElse(0.0);
            response.setAverageScore(roundToOneDecimal(totalAvg));
            response.setEvaluationCount(history.size());
        } else {
            response.setAverageScore(0.0);
            response.setEvaluationCount(0);
        }
        return response;
    }

    private String getRatingLabel(int score) {
        if (score >= 90)
            return "Xuất sắc";
        if (score >= 80)
            return "Tốt";
        if (score >= 70)
            return "Khá";
        if (score >= 50)
            return "Trung bình";
        return "Cần cố gắng";
    }

    public TargetGenerationResponse addTargets(Integer roundId, AddTargetsRequest req) {
        EvaluationRound round = evaluationRoundRepository.findById(roundId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Evaluation round not found"));

        java.util.Set<Integer> allowed = new java.util.HashSet<>();
        if (round.getType() == EvaluationRoundType.PROJECT) {
            if (round.getProjectId() == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Project is required for PROJECT evaluation rounds.");
            }
            projectMemberRepository.findByProjectId(round.getProjectId()).forEach(member -> {
                if (member.getUser() != null) {
                    allowed.add(member.getUser().getId());
                }
            });
        } else {
            userRepository.findByStatusIgnoreCase("ACTIVE").forEach(user -> allowed.add(user.getId()));
        }

        int created = 0;
        int skipped = 0;
        for (Integer userId : req.getUserIds()) {
            if (!allowed.contains(userId)) {
                skipped++;
                continue;
            }
            if (evaluationTargetRepository.existsByRoundIdAndUserId(roundId, userId)) {
                skipped++;
                continue;
            }
            EvaluationTarget target = new EvaluationTarget();
            target.setRoundId(roundId);
            target.setUserId(userId);
            target.setTargetStatus(EvaluationTargetStatus.NOT_STARTED);
            evaluationTargetRepository.save(target);
            created++;
        }
        TargetGenerationResponse response = new TargetGenerationResponse();
        response.setCandidateCount(req.getUserIds().size());
        response.setCreatedCount(created);
        response.setSkippedExisting(skipped);
        return response;
    }

    private void validateRequest(CreateEvaluationRoundRequest req) {
        if (req.getPeriodFrom() != null && req.getPeriodTo() != null
                && req.getPeriodFrom().isAfter(req.getPeriodTo())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Period from must be before or equal to period to.");
        }
        if (req.getType() == EvaluationRoundType.PROJECT && req.getProjectId() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Project is required for PROJECT evaluation rounds.");
        }
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim().toLowerCase();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeId(Integer value) {
        return value == null ? "unknown user" : String.valueOf(value);
    }

    private void sortCandidates(List<User> users, String sort) {
        if (sort == null || sort.isBlank()) {
            users.sort(java.util.Comparator.comparing(u -> safe(u.getFullName()).toLowerCase()));
            return;
        }
        switch (sort) {
            case "NAME_DESC" ->
                users.sort(java.util.Comparator.comparing((User u) -> safe(u.getFullName()).toLowerCase()).reversed());
            case "POSITION_ASC" ->
                users.sort(java.util.Comparator.comparing(u -> safe(u.getPositionTitle()).toLowerCase()));
            case "POSITION_DESC" -> users.sort(
                    java.util.Comparator.comparing((User u) -> safe(u.getPositionTitle()).toLowerCase()).reversed());
            default -> users.sort(java.util.Comparator.comparing(u -> safe(u.getFullName()).toLowerCase()));
        }
    }

    private EvaluationScoreSummaryResponse emptyScoreSummary() {
        EvaluationScoreSummaryResponse summary = new EvaluationScoreSummaryResponse();
        summary.setAverageScore(0.0);
        summary.setHighestScore(0);
        summary.setLowestScore(0);
        summary.setEvaluatedCount(0);
        return summary;
    }

    private List<EvaluationScoreDistributionResponse> defaultDistribution() {
        List<EvaluationScoreDistributionResponse> rows = new ArrayList<>();
        rows.add(distributionRow("90-100", 0));
        rows.add(distributionRow("80-89", 0));
        rows.add(distributionRow("70-79", 0));
        rows.add(distributionRow("60-69", 0));
        rows.add(distributionRow("0-59", 0));
        return rows;
    }

    private EvaluationScoreDistributionResponse distributionRow(String label, int count) {
        EvaluationScoreDistributionResponse row = new EvaluationScoreDistributionResponse();
        row.setLabel(label);
        row.setCount(count);
        return row;
    }

    private void incrementDistribution(List<EvaluationScoreDistributionResponse> distribution, int score) {
        if (score >= 90) {
            distribution.get(0).setCount(distribution.get(0).getCount() + 1);
        } else if (score >= 80) {
            distribution.get(1).setCount(distribution.get(1).getCount() + 1);
        } else if (score >= 70) {
            distribution.get(2).setCount(distribution.get(2).getCount() + 1);
        } else if (score >= 60) {
            distribution.get(3).setCount(distribution.get(3).getCount() + 1);
        } else {
            distribution.get(4).setCount(distribution.get(4).getCount() + 1);
        }
    }

    private List<EvaluationCriterionScoreResponse> buildCriteriaScores(
            List<EvaluationRoundCriterion> criteria,
            Map<Integer, List<Integer>> scores) {
        List<EvaluationCriterionScoreResponse> rows = new ArrayList<>();
        for (EvaluationRoundCriterion criterion : criteria) {
            EvaluationCriterionScoreResponse row = new EvaluationCriterionScoreResponse();
            row.setName(criterion.getNameSnapshot());
            row.setWeight(criterion.getWeight());
            List<Integer> values = scores != null ? scores.get(criterion.getId()) : null;
            row.setScore(values == null || values.isEmpty() ? null : (int) Math.round(averageScore(values)));
            rows.add(row);
        }
        return rows;
    }

    private double averageScore(List<Integer> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Integer score : scores) {
            if (score != null) {
                total += score;
            }
        }
        return total / (double) scores.size();
    }

    private double averageScoreDouble(List<Double> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0;
        }
        double total = 0;
        int count = 0;
        for (Double score : scores) {
            if (score != null) {
                total += score;
                count++;
            }
        }
        return count == 0 ? 0 : total / count;
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static class TargetAggregate {
        private final Integer userId;
        private String fullName;
        private String positionTitle;
        private final List<Integer> formIds = new ArrayList<>();
        private final List<Integer> scores = new ArrayList<>();

        TargetAggregate(Integer userId) {
            this.userId = userId;
        }

        Integer getUserId() {
            return userId;
        }

        String getFullName() {
            return fullName;
        }

        void setFullName(String fullName) {
            this.fullName = fullName;
        }

        String getPositionTitle() {
            return positionTitle;
        }

        void setPositionTitle(String positionTitle) {
            this.positionTitle = positionTitle;
        }

        List<Integer> getFormIds() {
            return formIds;
        }

        List<Integer> getScores() {
            return scores;
        }
    }

}
