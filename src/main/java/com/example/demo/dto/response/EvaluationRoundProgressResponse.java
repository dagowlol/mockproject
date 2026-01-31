package com.example.demo.dto.response;

import java.util.List;

public class EvaluationRoundProgressResponse {
    private Integer roundId;
    private String roundName;
    private int totalAssignments;
    private int pendingCount;
    private int submittedCount;
    private int lockedCount;
    private List<EvaluatorProgressResponse> evaluatorProgress;
    private List<EvaluationAssignmentProgressRowResponse> assignmentRows;

    public Integer getRoundId() {
        return roundId;
    }

    public void setRoundId(Integer roundId) {
        this.roundId = roundId;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }

    public int getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(int totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public int getSubmittedCount() {
        return submittedCount;
    }

    public void setSubmittedCount(int submittedCount) {
        this.submittedCount = submittedCount;
    }

    public int getLockedCount() {
        return lockedCount;
    }

    public void setLockedCount(int lockedCount) {
        this.lockedCount = lockedCount;
    }

    public List<EvaluatorProgressResponse> getEvaluatorProgress() {
        return evaluatorProgress;
    }

    public void setEvaluatorProgress(List<EvaluatorProgressResponse> evaluatorProgress) {
        this.evaluatorProgress = evaluatorProgress;
    }

    public List<EvaluationAssignmentProgressRowResponse> getAssignmentRows() {
        return assignmentRows;
    }

    public void setAssignmentRows(List<EvaluationAssignmentProgressRowResponse> assignmentRows) {
        this.assignmentRows = assignmentRows;
    }
}
