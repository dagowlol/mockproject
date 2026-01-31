package com.example.demo.dto.response;

public class ManualAssignmentRowResponse {
    private Integer targetId;
    private Integer userId;
    private String fullName;
    private String positionTitle;
    private java.util.List<Integer> evaluatorUserIds;
    private java.util.List<ManualAssignmentLeaderOptionResponse> leaderOptions;

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }

    public java.util.List<Integer> getEvaluatorUserIds() {
        return evaluatorUserIds;
    }

    public void setEvaluatorUserIds(java.util.List<Integer> evaluatorUserIds) {
        this.evaluatorUserIds = evaluatorUserIds;
    }

    public java.util.List<ManualAssignmentLeaderOptionResponse> getLeaderOptions() {
        return leaderOptions;
    }

    public void setLeaderOptions(java.util.List<ManualAssignmentLeaderOptionResponse> leaderOptions) {
        this.leaderOptions = leaderOptions;
    }
}
