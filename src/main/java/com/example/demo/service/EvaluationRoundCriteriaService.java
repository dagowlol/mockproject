    package com.example.demo.service;

    import java.util.List;
    import java.util.stream.Collectors;

    import org.springframework.http.HttpStatus;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import com.example.demo.dto.request.CreateEvaluationCriterionRequest;
    import com.example.demo.dto.response.EvaluationRoundCriterionResponse;
    import com.example.demo.exception.AppException;
    import com.example.demo.mapper.EvaluationRoundCriterionMapper;
    import com.example.demo.model.EvaluationCriterion;
    import com.example.demo.model.EvaluationRoundCriterion;
    import com.example.demo.repo.EvaluationCriterionRepository;
    import com.example.demo.repo.EvaluationRoundCriterionRepository;
    import com.example.demo.repo.EvaluationRoundRepository;

    @Service
    @Transactional
    public class EvaluationRoundCriteriaService {
        private final EvaluationRoundRepository evaluationRoundRepository;
        private final EvaluationCriterionRepository evaluationCriterionRepository;
        private final EvaluationRoundCriterionRepository evaluationRoundCriterionRepository;
        private final EvaluationRoundCriterionMapper evaluationRoundCriterionMapper;

        public EvaluationRoundCriteriaService(
            EvaluationRoundRepository evaluationRoundRepository,
            EvaluationCriterionRepository evaluationCriterionRepository,
            EvaluationRoundCriterionRepository evaluationRoundCriterionRepository,
            EvaluationRoundCriterionMapper evaluationRoundCriterionMapper
        ) {
            this.evaluationRoundRepository = evaluationRoundRepository;
            this.evaluationCriterionRepository = evaluationCriterionRepository;
            this.evaluationRoundCriterionRepository = evaluationRoundCriterionRepository;
            this.evaluationRoundCriterionMapper = evaluationRoundCriterionMapper;
        }

        public List<EvaluationRoundCriterionResponse> listCriteria(Integer roundId) {
            return evaluationRoundCriterionRepository.findByRoundIdOrderByDisplayOrderAsc(roundId)
                .stream()
                .map(evaluationRoundCriterionMapper::toResponse)
                .collect(Collectors.toList());
        }

        public EvaluationRoundCriterionResponse addCriterion(
            Integer roundId,
            CreateEvaluationCriterionRequest req,
            Integer actorId
        ) {
            validateCriterionRequest(req);
            int currentTotal = evaluationRoundCriterionRepository.sumWeightByRoundId(roundId);
            if (currentTotal + req.getWeight() > 100) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Total weight cannot exceed 100%.");
            }

            EvaluationCriterion criterion = new EvaluationCriterion();
            criterion.setName(req.getName().trim());
            criterion.setDescription(req.getDescription().trim());
            criterion.setWeight(req.getWeight());
            criterion.setMaxScore(req.getMaxScore());
            criterion.setDisplayOrder(req.getDisplayOrder());
            criterion.setIsTemplate(false);
            criterion.setCreatedBy(actorId);
            criterion.setUpdatedBy(actorId);
            EvaluationCriterion savedCriterion = evaluationCriterionRepository.save(criterion);

            EvaluationRoundCriterion roundCriterion = new EvaluationRoundCriterion();
            roundCriterion.setRoundId(roundId);
            roundCriterion.setCriterionId(savedCriterion.getId());
            roundCriterion.setNameSnapshot(savedCriterion.getName());
            roundCriterion.setDescription(savedCriterion.getDescription());
            roundCriterion.setWeight(savedCriterion.getWeight());
            roundCriterion.setMaxScore(savedCriterion.getMaxScore());
            roundCriterion.setDisplayOrder(savedCriterion.getDisplayOrder());
            roundCriterion.setCreatedBy(actorId);

            return evaluationRoundCriterionMapper.toResponse(evaluationRoundCriterionRepository.save(roundCriterion));
        }
    public void deleteCriterion(Integer roundId, Integer roundCriterionId) {
        EvaluationRoundCriterion roundCriterion = evaluationRoundCriterionRepository.findById(roundCriterionId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Criterion not found"));
        if (!roundId.equals(roundCriterion.getRoundId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Criterion does not belong to the round");
            }
            Integer criterionId = roundCriterion.getCriterionId();
            evaluationRoundCriterionRepository.deleteByRoundIdAndId(roundId, roundCriterionId);
            if (criterionId != null) {
                evaluationCriterionRepository.deleteById(criterionId);
            }
        }

        public List<EvaluationCriterion> listTemplateCriteria() {
            return evaluationCriterionRepository.findByIsTemplateTrueOrderByDisplayOrderAsc();
        }

        public EvaluationCriterion addTemplateCriterion(CreateEvaluationCriterionRequest req, Integer actorId) {
            validateCriterionRequest(req);

            EvaluationCriterion template = new EvaluationCriterion();
            template.setName(req.getName().trim());
            template.setDescription(req.getDescription().trim());
            template.setWeight(req.getWeight());
            template.setMaxScore(req.getMaxScore());
            template.setDisplayOrder(req.getDisplayOrder());
            template.setIsTemplate(true);
            template.setCreatedBy(actorId);
            template.setUpdatedBy(actorId);

            return evaluationCriterionRepository.save(template);
        }

        public EvaluationCriterion updateTemplateCriterion(
            Integer templateId,
            CreateEvaluationCriterionRequest req,
            Integer actorId
        ) {
            validateCriterionRequest(req);

            EvaluationCriterion template = evaluationCriterionRepository.findById(templateId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Template criterion not found"));
            if (template.getIsTemplate() == null || !template.getIsTemplate()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Criterion is not a template");
            }

            template.setName(req.getName().trim());
            template.setDescription(req.getDescription().trim());
            template.setWeight(req.getWeight());
            template.setMaxScore(req.getMaxScore());
            template.setDisplayOrder(req.getDisplayOrder());
            template.setUpdatedBy(actorId);

            return evaluationCriterionRepository.save(template);
        }

        public void deleteTemplateCriterion(Integer templateId) {
            if (!evaluationCriterionRepository.existsById(templateId)) {
                throw new AppException(HttpStatus.NOT_FOUND, "Template criterion not found");
            }
            evaluationCriterionRepository.deleteById(templateId);
        }

        public void applyCriteriaFromRound(Integer sourceRoundId, Integer targetRoundId, Integer actorId) {
            if (!evaluationRoundRepository.existsById(sourceRoundId)) {
                throw new AppException(HttpStatus.NOT_FOUND, "Source evaluation round not found");
            }
            List<EvaluationRoundCriterion> sourceCriteria = evaluationRoundCriterionRepository
                .findByRoundIdOrderByDisplayOrderAsc(sourceRoundId);
            if (sourceCriteria.isEmpty()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Selected round has no criteria to copy");
            }
            for (EvaluationRoundCriterion source : sourceCriteria) {
                EvaluationCriterion criterion = new EvaluationCriterion();
                criterion.setName(source.getNameSnapshot());
                criterion.setDescription(source.getDescription());
                criterion.setWeight(source.getWeight());
                criterion.setMaxScore(source.getMaxScore());
                criterion.setDisplayOrder(source.getDisplayOrder());
                criterion.setIsTemplate(false);
                criterion.setCreatedBy(actorId);
                criterion.setUpdatedBy(actorId);
                EvaluationCriterion savedCriterion = evaluationCriterionRepository.save(criterion);

                EvaluationRoundCriterion roundCriterion = new EvaluationRoundCriterion();
                roundCriterion.setRoundId(targetRoundId);
                roundCriterion.setCriterionId(savedCriterion.getId());
                roundCriterion.setNameSnapshot(savedCriterion.getName());
                roundCriterion.setDescription(savedCriterion.getDescription());
                roundCriterion.setWeight(savedCriterion.getWeight());
                roundCriterion.setMaxScore(savedCriterion.getMaxScore());
                roundCriterion.setDisplayOrder(savedCriterion.getDisplayOrder());
                roundCriterion.setCreatedBy(actorId);

                evaluationRoundCriterionRepository.save(roundCriterion);
            }
        }

        private void validateCriterionRequest(CreateEvaluationCriterionRequest req) {
            if (req.getWeight() != null && req.getWeight() > 100) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Weight cannot exceed 100%.");
            }
            if (req.getMaxScore() != null && req.getMaxScore() <= 0) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Max score must be greater than 0.");
            }
        }
    

    public EvaluationRoundCriterionResponse updateCriterion(
        Integer roundId,
        Integer roundCriterionId,
        CreateEvaluationCriterionRequest req,
        Integer actorId
    ) {
        validateCriterionRequest(req);

        EvaluationRoundCriterion roundCriterion = evaluationRoundCriterionRepository.findById(roundCriterionId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Criterion not found"));
        if (!roundId.equals(roundCriterion.getRoundId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Criterion does not belong to the round");
        }

        int totalOther = evaluationRoundCriterionRepository.sumWeightByRoundIdExcluding(roundId, roundCriterionId);
        if (totalOther + req.getWeight() > 100) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Total weight cannot exceed 100%.");
        }

        Integer criterionId = roundCriterion.getCriterionId();
        if (criterionId != null) {
            EvaluationCriterion criterion = evaluationCriterionRepository.findById(criterionId)
                .orElse(null);
            if (criterion != null) {
                criterion.setName(req.getName().trim());
                criterion.setDescription(req.getDescription().trim());
                criterion.setWeight(req.getWeight());
                criterion.setMaxScore(req.getMaxScore());
                criterion.setDisplayOrder(req.getDisplayOrder());
                criterion.setUpdatedBy(actorId);
                evaluationCriterionRepository.save(criterion);
            }
        }

        roundCriterion.setNameSnapshot(req.getName().trim());
        roundCriterion.setDescription(req.getDescription().trim());
        roundCriterion.setWeight(req.getWeight());
        roundCriterion.setMaxScore(req.getMaxScore());
        roundCriterion.setDisplayOrder(req.getDisplayOrder());
        roundCriterion.setCreatedBy(actorId);

        return evaluationRoundCriterionMapper.toResponse(evaluationRoundCriterionRepository.save(roundCriterion));
    }

}
