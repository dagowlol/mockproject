package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.request.CreateTemplateRoundRequest;
import com.example.demo.dto.request.UpdateTemplateRoundRequest;
import com.example.demo.dto.response.EvaluationRoundResponse;
import com.example.demo.exception.AppException;
import com.example.demo.mapper.EvaluationRoundMapper;
import com.example.demo.model.EvaluationRound;
import com.example.demo.model.EvaluationRoundCriterion;
import com.example.demo.model.EvaluationRoundStatus;
import com.example.demo.model.EvaluationRoundType;
import com.example.demo.repo.EvaluationCriterionRepository;
import com.example.demo.repo.EvaluationRoundCriterionRepository;
import com.example.demo.repo.EvaluationRoundRepository;

@Service
@Transactional
public class TemplateRoundService {
    private final EvaluationRoundRepository evaluationRoundRepository;
    private final EvaluationRoundCriterionRepository evaluationRoundCriterionRepository;
    private final EvaluationCriterionRepository evaluationCriterionRepository;
    private final EvaluationRoundMapper evaluationRoundMapper;

    public TemplateRoundService(
        EvaluationRoundRepository evaluationRoundRepository,
        EvaluationRoundCriterionRepository evaluationRoundCriterionRepository,
        EvaluationCriterionRepository evaluationCriterionRepository,
        EvaluationRoundMapper evaluationRoundMapper
    ) {
        this.evaluationRoundRepository = evaluationRoundRepository;
        this.evaluationRoundCriterionRepository = evaluationRoundCriterionRepository;
        this.evaluationCriterionRepository = evaluationCriterionRepository;
        this.evaluationRoundMapper = evaluationRoundMapper;
    }

    public List<EvaluationRoundResponse> listTemplateRounds() {
        return evaluationRoundRepository.findByIsTemplateRoundTrueOrderByCreatedAtDesc()
            .stream()
            .map(evaluationRoundMapper::toResponse)
            .collect(Collectors.toList());
    }

    public EvaluationRoundResponse createTemplateRound(CreateTemplateRoundRequest req, Integer actorId) {
        EvaluationRound round = new EvaluationRound();
        round.setName(req.getName().trim());
        round.setDescription(req.getDescription().trim());
        round.setType(EvaluationRoundType.MONTHLY);
        java.time.LocalDate today = java.time.LocalDate.now();
        round.setPeriodFrom(today);
        round.setPeriodTo(today);
        round.setStatus(EvaluationRoundStatus.DRAFT);
        round.setScoringScale(req.getScoringScale() != null ? req.getScoringScale() : 100);
        round.setIsTemplateRound(true);
        round.setCreatedBy(actorId);
        round.setUpdatedBy(actorId);
        EvaluationRound saved = evaluationRoundRepository.save(round);
        return evaluationRoundMapper.toResponse(saved);
    }

    public EvaluationRoundResponse updateTemplateRound(
        Integer templateRoundId,
        UpdateTemplateRoundRequest req,
        Integer actorId
    ) {
        EvaluationRound round = evaluationRoundRepository.findById(templateRoundId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Template round not found"));
        if (!Boolean.TRUE.equals(round.getIsTemplateRound())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Round is not a template");
        }
        round.setName(req.getName().trim());
        round.setDescription(req.getDescription().trim());
        if (req.getScoringScale() != null) {
            round.setScoringScale(req.getScoringScale());
        }
        round.setUpdatedBy(actorId);
        EvaluationRound saved = evaluationRoundRepository.save(round);
        return evaluationRoundMapper.toResponse(saved);
    }

    public void deleteTemplateRound(Integer templateRoundId) {
        EvaluationRound round = evaluationRoundRepository.findById(templateRoundId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Template round not found"));
        if (!Boolean.TRUE.equals(round.getIsTemplateRound())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Round is not a template");
        }
        List<EvaluationRoundCriterion> criteria = evaluationRoundCriterionRepository
            .findByRoundIdOrderByDisplayOrderAsc(templateRoundId);
        evaluationRoundCriterionRepository.deleteByRoundId(templateRoundId);
        for (EvaluationRoundCriterion item : criteria) {
            if (item.getCriterionId() != null) {
                evaluationCriterionRepository.deleteById(item.getCriterionId());
            }
        }
        evaluationRoundRepository.deleteById(templateRoundId);
    }
}
