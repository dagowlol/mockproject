package com.example.demo.dto.response;

public class AutoAssignResultResponse {
    private int targetCount;
    private int evaluatorCount;
    private int assignedCount;
    private int skippedNoLeader;
    private int skippedExisting;

    public int getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(int targetCount) {
        this.targetCount = targetCount;
    }

    public int getEvaluatorCount() {
        return evaluatorCount;
    }

    public void setEvaluatorCount(int evaluatorCount) {
        this.evaluatorCount = evaluatorCount;
    }

    public int getAssignedCount() {
        return assignedCount;
    }

    public void setAssignedCount(int assignedCount) {
        this.assignedCount = assignedCount;
    }

    public int getSkippedNoLeader() {
        return skippedNoLeader;
    }

    public void setSkippedNoLeader(int skippedNoLeader) {
        this.skippedNoLeader = skippedNoLeader;
    }

    public int getSkippedExisting() {
        return skippedExisting;
    }

    public void setSkippedExisting(int skippedExisting) {
        this.skippedExisting = skippedExisting;
    }
}
