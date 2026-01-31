package com.example.demo.dto.response;

import java.util.List;

public class EvaluationRoundDetailResponse {
    private EvaluationRoundResponse round;
    private List<EvaluationRoundCriterionResponse> criteria;
    private Integer targetCount;
    private Integer criteriaWeightTotal;

    public EvaluationRoundResponse getRound() {
        return round;
    }

    public void setRound(EvaluationRoundResponse round) {
        this.round = round;
    }

    public List<EvaluationRoundCriterionResponse> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<EvaluationRoundCriterionResponse> criteria) {
        this.criteria = criteria;
    }

    public Integer getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(Integer targetCount) {
        this.targetCount = targetCount;
    }

    public Integer getCriteriaWeightTotal() {
        return criteriaWeightTotal;
    }

    public void setCriteriaWeightTotal(Integer criteriaWeightTotal) {
        this.criteriaWeightTotal = criteriaWeightTotal;
    }
}
