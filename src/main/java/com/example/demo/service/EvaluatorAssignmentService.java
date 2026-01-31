package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.response.EvaluatorAssignmentRowResponse;
import com.example.demo.dto.response.EvaluatorAssignmentSummaryResponse;
import com.example.demo.dto.response.EvaluationFormCriterionResponse;
import com.example.demo.dto.response.EvaluationFormResponse;
import com.example.demo.model.EvaluationAssignment;
import com.example.demo.model.EvaluationAssignmentStatus;
import com.example.demo.model.EvaluationRound;
import com.example.demo.model.EvaluationRoundCriterion;
import com.example.demo.model.EvaluationScore;
import com.example.demo.model.EvaluationForm;
import com.example.demo.model.EvaluationTarget;
import com.example.demo.model.User;
import com.example.demo.repo.EvaluationAssignmentRepository;
import com.example.demo.repo.EvaluationFormRepository;
import com.example.demo.repo.EvaluationRoundRepository;
import com.example.demo.repo.EvaluationRoundCriterionRepository;
import com.example.demo.repo.EvaluationScoreRepository;
import com.example.demo.repo.EvaluationTargetRepository;
import com.example.demo.repo.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EvaluatorAssignmentService {
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;
    private final EvaluationTargetRepository evaluationTargetRepository;
    private final EvaluationRoundRepository evaluationRoundRepository;
    private final EvaluationRoundCriterionRepository evaluationRoundCriterionRepository;
    private final EvaluationFormRepository evaluationFormRepository;
    private final EvaluationScoreRepository evaluationScoreRepository;
    private final UserRepository userRepository;

    public EvaluatorAssignmentService(
        EvaluationAssignmentRepository evaluationAssignmentRepository,
        EvaluationTargetRepository evaluationTargetRepository,
        EvaluationRoundRepository evaluationRoundRepository,
        EvaluationRoundCriterionRepository evaluationRoundCriterionRepository,
        EvaluationFormRepository evaluationFormRepository,
        EvaluationScoreRepository evaluationScoreRepository,
        UserRepository userRepository
    ) {
        this.evaluationAssignmentRepository = evaluationAssignmentRepository;
        this.evaluationTargetRepository = evaluationTargetRepository;
        this.evaluationRoundRepository = evaluationRoundRepository;
        this.evaluationRoundCriterionRepository = evaluationRoundCriterionRepository;
        this.evaluationFormRepository = evaluationFormRepository;
        this.evaluationScoreRepository = evaluationScoreRepository;
        this.userRepository = userRepository;
    }

    public EvaluatorAssignmentSummaryResponse getSummary(Integer evaluatorUserId) {
        EvaluatorAssignmentSummaryResponse response = new EvaluatorAssignmentSummaryResponse();
        int pending = (int) evaluationAssignmentRepository.countByEvaluatorUserIdAndStatus(
            evaluatorUserId, EvaluationAssignmentStatus.PENDING);
        int submitted = (int) evaluationAssignmentRepository.countByEvaluatorUserIdAndStatus(
            evaluatorUserId, EvaluationAssignmentStatus.SUBMITTED);
        int dueSoon = (int) evaluationAssignmentRepository.countDueSoon(
            evaluatorUserId, LocalDateTime.now().plusDays(7));
        response.setPendingCount(pending);
        response.setSubmittedCount(submitted);
        response.setDueSoonCount(dueSoon);
        return response;
    }

    public List<EvaluatorAssignmentRowResponse> listAssignments(Integer evaluatorUserId, String status) {
        log.info("Listing assignments for evaluatorUserId: {} with status filter: {}", evaluatorUserId, status);
        List<EvaluationAssignment> assignments = evaluationAssignmentRepository
            .findByEvaluatorUserIdOrderByDueAtAsc(evaluatorUserId);
            log.info("Total assignments found: {}", assignments.size());
        List<EvaluatorAssignmentRowResponse> rows = new ArrayList<>();
        for (EvaluationAssignment assignment : assignments) {
            log.info("Checking assignment with status: {}", assignment.getStatus().name());
            if (status != null && !status.isBlank()
                && !assignment.getStatus().name().equalsIgnoreCase(status)) {
                continue;
            }
            EvaluationTarget target = evaluationTargetRepository.findById(assignment.getTargetId()).orElse(null);
            if (target == null) {
                continue;
            }
            EvaluationRound round = evaluationRoundRepository.findById(target.getRoundId()).orElse(null);
            User user = userRepository.findById(target.getUserId()).orElse(null);
            if (round == null || user == null) {
                continue;
            }
            EvaluatorAssignmentRowResponse row = new EvaluatorAssignmentRowResponse();
            row.setAssignmentId(assignment.getId());
            row.setRoundId(round.getId());
            row.setRoundName(round.getName());
            row.setTargetUserId(user.getId());
            row.setEmployeeName(user.getFullName());
            row.setPositionTitle(user.getPositionTitle());
            row.setDueAt(assignment.getDueAt());
            row.setStatus(assignment.getStatus().name());
            rows.add(row);
        }
        return rows;
    }

    public EvaluationFormResponse getEvaluationForm(Integer assignmentId, Integer evaluatorUserId) {
        EvaluationAssignment assignment = evaluationAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new com.example.demo.exception.AppException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Assignment not found"));
        if (!evaluatorUserId.equals(assignment.getEvaluatorUserId())) {
            throw new com.example.demo.exception.AppException(
                org.springframework.http.HttpStatus.FORBIDDEN, "Access denied");
        }

        EvaluationTarget target = evaluationTargetRepository.findById(assignment.getTargetId())
            .orElseThrow(() -> new com.example.demo.exception.AppException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Target not found"));
        Integer roundId = target.getRoundId();
        List<EvaluationRoundCriterion> criteria = evaluationRoundCriterionRepository
            .findByRoundIdOrderByDisplayOrderAsc(roundId);

        EvaluationForm form = evaluationFormRepository.findByAssignmentId(assignmentId).orElse(null);
        List<EvaluationScore> scores = form != null
            ? evaluationScoreRepository.findByFormId(form.getId())
            : java.util.List.of();
        java.util.Map<Integer, EvaluationScore> scoreMap = new java.util.HashMap<>();
        for (EvaluationScore score : scores) {
            scoreMap.put(score.getCriterionId(), score);
        }

        List<EvaluationFormCriterionResponse> rows = new java.util.ArrayList<>();
        for (EvaluationRoundCriterion c : criteria) {
            EvaluationFormCriterionResponse row = new EvaluationFormCriterionResponse();
            row.setCriterionId(c.getId());
            row.setName(c.getNameSnapshot());
            row.setDescription(c.getDescription());
            row.setWeight(c.getWeight());
            row.setMaxScore(c.getMaxScore());
            EvaluationScore existing = scoreMap.get(c.getId());
            if (existing != null) {
                row.setScoreValue(existing.getScoreValue());
                row.setNote(existing.getNote());
            }
            rows.add(row);
        }

        EvaluationFormResponse response = new EvaluationFormResponse();
        response.setAssignmentId(assignmentId);
        response.setFormId(form != null ? form.getId() : null);
        response.setStatus(assignment.getStatus().name());
        response.setComment(form != null ? form.getComment() : null);
        response.setTotalScore(form != null ? form.getTotalScore() : null);
        response.setCriteria(rows);
        return response;
    }

    public EvaluationFormResponse saveEvaluationForm(
        Integer assignmentId,
        Integer evaluatorUserId,
        java.util.List<com.example.demo.dto.request.EvaluationScoreRequestItem> scores,
        String comment,
        boolean submit
    ) {
        EvaluationAssignment assignment = evaluationAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new com.example.demo.exception.AppException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Assignment not found"));
        if (!evaluatorUserId.equals(assignment.getEvaluatorUserId())) {
            throw new com.example.demo.exception.AppException(
                org.springframework.http.HttpStatus.FORBIDDEN, "Access denied");
        }
        if (assignment.getStatus() == EvaluationAssignmentStatus.LOCKED
            || assignment.getStatus() == EvaluationAssignmentStatus.CANCELLED) {
            throw new com.example.demo.exception.AppException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Assignment is not editable");
        }

        EvaluationTarget target = evaluationTargetRepository.findById(assignment.getTargetId())
            .orElseThrow(() -> new com.example.demo.exception.AppException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Target not found"));
        Integer roundId = target.getRoundId();
        List<EvaluationRoundCriterion> criteria = evaluationRoundCriterionRepository
            .findByRoundIdOrderByDisplayOrderAsc(roundId);
        java.util.Map<Integer, EvaluationRoundCriterion> criteriaMap = new java.util.HashMap<>();
        for (EvaluationRoundCriterion c : criteria) {
            criteriaMap.put(c.getId(), c);
        }

        EvaluationForm form = evaluationFormRepository.findByAssignmentId(assignmentId)
            .orElseGet(() -> {
                EvaluationForm created = new EvaluationForm();
                created.setAssignmentId(assignmentId);
                return evaluationFormRepository.save(created);
            });

        int total = 0;
        for (com.example.demo.dto.request.EvaluationScoreRequestItem item : scores) {
            EvaluationRoundCriterion criterion = criteriaMap.get(item.getCriterionId());
            if (criterion == null) {
                throw new com.example.demo.exception.AppException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid criterion");
            }
            Integer max = criterion.getMaxScore();
            if (item.getScoreValue() < 0 || (max != null && item.getScoreValue() > max)) {
                throw new com.example.demo.exception.AppException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Score exceeds max score");
            }
            total += item.getScoreValue();

            EvaluationScore score = evaluationScoreRepository
                .findByFormIdAndCriterionId(form.getId(), item.getCriterionId())
                .orElseGet(EvaluationScore::new);
            score.setFormId(form.getId());
            score.setCriterionId(item.getCriterionId());
            score.setScoreValue(item.getScoreValue());
            score.setNote(item.getNote());
            evaluationScoreRepository.save(score);
        }

        form.setComment(comment);
        form.setTotalScore(total);
        if (submit) {
            form.setSubmittedAt(LocalDateTime.now());
            assignment.setStatus(EvaluationAssignmentStatus.SUBMITTED);
            evaluationAssignmentRepository.save(assignment);
        }
        evaluationFormRepository.save(form);

        return getEvaluationForm(assignmentId, evaluatorUserId);
    }
}
