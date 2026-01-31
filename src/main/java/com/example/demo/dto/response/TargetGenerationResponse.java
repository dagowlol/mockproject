package com.example.demo.dto.response;

public class TargetGenerationResponse {
    private int candidateCount;
    private int createdCount;
    private int skippedExisting;

    public int getCandidateCount() {
        return candidateCount;
    }

    public void setCandidateCount(int candidateCount) {
        this.candidateCount = candidateCount;
    }

    public int getCreatedCount() {
        return createdCount;
    }

    public void setCreatedCount(int createdCount) {
        this.createdCount = createdCount;
    }

    public int getSkippedExisting() {
        return skippedExisting;
    }

    public void setSkippedExisting(int skippedExisting) {
        this.skippedExisting = skippedExisting;
    }
}
