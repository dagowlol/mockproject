package com.example.demo.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public class ManualAssignmentRequest {
    @NotEmpty
    private List<ManualAssignmentItemRequest> assignments;

    private Boolean notify;

    public List<ManualAssignmentItemRequest> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<ManualAssignmentItemRequest> assignments) {
        this.assignments = assignments;
    }

    public Boolean getNotify() {
        return notify;
    }

    public void setNotify(Boolean notify) {
        this.notify = notify;
    }
}
