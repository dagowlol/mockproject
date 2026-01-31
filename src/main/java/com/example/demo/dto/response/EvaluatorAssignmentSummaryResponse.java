package com.example.demo.dto.response;

public class EvaluatorAssignmentSummaryResponse {
    private int pendingCount;
    private int submittedCount;
    private int dueSoonCount;

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

    public int getDueSoonCount() {
        return dueSoonCount;
    }

    public void setDueSoonCount(int dueSoonCount) {
        this.dueSoonCount = dueSoonCount;
    }
}
