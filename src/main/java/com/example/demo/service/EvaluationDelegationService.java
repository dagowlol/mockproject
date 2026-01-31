package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.request.DelegationRequest;
import com.example.demo.dto.response.DelegationResponse;
import com.example.demo.exception.AppException;
import com.example.demo.model.EvaluationAssignment;
import com.example.demo.model.EvaluationDelegation;
import com.example.demo.model.EvaluationDelegationStatus;
import com.example.demo.model.EvaluationTarget;
import com.example.demo.model.User;
import com.example.demo.repo.EvaluationAssignmentRepository;
import com.example.demo.repo.EvaluationDelegationRepository;
import com.example.demo.repo.EvaluationTargetRepository;
import com.example.demo.repo.UserRepository;

@Service
@Transactional
public class EvaluationDelegationService {

    private final EvaluationDelegationRepository delegationRepository;
    private final EvaluationAssignmentRepository assignmentRepository;
    private final EvaluationTargetRepository targetRepository;
    private final UserRepository userRepository;

    public EvaluationDelegationService(
            EvaluationDelegationRepository delegationRepository,
            EvaluationAssignmentRepository assignmentRepository,
            EvaluationTargetRepository targetRepository,
            UserRepository userRepository) {
        this.delegationRepository = delegationRepository;
        this.assignmentRepository = assignmentRepository;
        this.targetRepository = targetRepository;
        this.userRepository = userRepository;
    }

    public DelegationResponse create(DelegationRequest req, Integer actorId) {
        EvaluationAssignment assignment = assignmentRepository.findById(req.getAssignmentId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Assignment not found"));

        if (!actorId.equals(assignment.getEvaluatorUserId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "You can only delegate your own assignments");
        }

        User toEvaluator = userRepository.findById(req.getToEvaluatorId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Target evaluator not found"));

        if (toEvaluator.getId() != null && toEvaluator.getId().equals(actorId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delegate to yourself");
        }

        EvaluationDelegation delegation = new EvaluationDelegation();
        delegation.setAssignmentId(req.getAssignmentId());
        delegation.setFromEvaluatorId(actorId);
        delegation.setToEvaluatorId(req.getToEvaluatorId());
        delegation.setReason(req.getReason());
        delegation.setCreatedBy(actorId);
        delegation.setEffectiveFrom(req.getEffectiveFrom().atStartOfDay());
        delegation.setEffectiveTo(req.getEffectiveTo().atTime(23, 59, 59));
        delegation.setStatus(EvaluationDelegationStatus.REQUESTED);

        EvaluationDelegation saved = delegationRepository.save(delegation);
        return toResponse(saved);
    }

    public List<DelegationResponse> listMyDelegationRequests(Integer actorId) {
        return delegationRepository.findByFromEvaluatorIdOrderByCreatedAtDesc(actorId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private DelegationResponse toResponse(EvaluationDelegation entity) {
        DelegationResponse res = new DelegationResponse();
        res.setId(entity.getId());
        res.setAssignmentId(entity.getAssignmentId());
        res.setFromEvaluatorId(entity.getFromEvaluatorId());
        res.setToEvaluatorId(entity.getToEvaluatorId());
        res.setStatus(entity.getStatus().name());
        res.setReason(entity.getReason());
        res.setEffectiveFrom(entity.getEffectiveFrom());
        res.setEffectiveTo(entity.getEffectiveTo());
        res.setCreatedAt(entity.getCreatedAt());

        // Enrich with names
        userRepository.findById(entity.getFromEvaluatorId())
                .ifPresent(u -> res.setFromEvaluatorName(u.getFullName()));
        userRepository.findById(entity.getToEvaluatorId())
                .ifPresent(u -> res.setToEvaluatorName(u.getFullName()));

        assignmentRepository.findById(entity.getAssignmentId()).ifPresent(assignment -> {
            Integer targetId = assignment.getTargetId();
            if (targetId != null) {
                targetRepository.findById(targetId).ifPresent(target -> {
                    Integer userId = target.getUserId();
                    if (userId != null) {
                        userRepository.findById(userId).ifPresent(user -> {
                            res.setAssignmentTargetName(user.getFullName());
                        });
                    }
                });
            }
        });

        return res;
    }
}
