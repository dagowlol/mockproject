package com.example.demo.dto.response;

public class EvaluatorProgressResponse {
    private Integer evaluatorUserId;
    private String evaluatorName;
    private String evaluatorPositionTitle;
    private int totalCount;
    private int pendingCount;
    private int submittedCount;
    private int lockedCount;

    public Integer getEvaluatorUserId() {
        return evaluatorUserId;
    }

    public void setEvaluatorUserId(Integer evaluatorUserId) {
        this.evaluatorUserId = evaluatorUserId;
    }

    public String getEvaluatorName() {
        return evaluatorName;
    }

    public void setEvaluatorName(String evaluatorName) {
        this.evaluatorName = evaluatorName;
    }

    public String getEvaluatorPositionTitle() {
        return evaluatorPositionTitle;
    }

    public void setEvaluatorPositionTitle(String evaluatorPositionTitle) {
        this.evaluatorPositionTitle = evaluatorPositionTitle;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
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
}
