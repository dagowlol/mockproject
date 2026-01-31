package com.example.demo.dto.response;

public class EvaluationSummaryHeaderResponse {
    private Integer ongoingRounds;
    private Integer totalAssignments;
    private Integer completedAssignments;
    private Integer awaitingApprovalRounds;

    public Integer getOngoingRounds() {
        return ongoingRounds;
    }

    public void setOngoingRounds(Integer ongoingRounds) {
        this.ongoingRounds = ongoingRounds;
    }

    public Integer getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(Integer totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public Integer getCompletedAssignments() {
        return completedAssignments;
    }

    public void setCompletedAssignments(Integer completedAssignments) {
        this.completedAssignments = completedAssignments;
    }

    public Integer getAwaitingApprovalRounds() {
        return awaitingApprovalRounds;
    }

    public void setAwaitingApprovalRounds(Integer awaitingApprovalRounds) {
        this.awaitingApprovalRounds = awaitingApprovalRounds;
    }
}
