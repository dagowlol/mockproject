package com.example.demo.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public class EvaluationFormRequest {
    @NotEmpty
    private List<EvaluationScoreRequestItem> scores;

    private String comment;

    public List<EvaluationScoreRequestItem> getScores() {
        return scores;
    }

    public void setScores(List<EvaluationScoreRequestItem> scores) {
        this.scores = scores;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
