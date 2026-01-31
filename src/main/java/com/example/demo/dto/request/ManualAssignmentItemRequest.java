package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;

public class ManualAssignmentItemRequest {
    @NotNull
    private Integer targetId;

    private java.util.List<Integer> evaluatorUserIds;

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public java.util.List<Integer> getEvaluatorUserIds() {
        return evaluatorUserIds;
    }

    public void setEvaluatorUserIds(java.util.List<Integer> evaluatorUserIds) {
        this.evaluatorUserIds = evaluatorUserIds;
    }
}
