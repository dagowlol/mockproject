package com.example.demo.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public class AddTargetsRequest {
    @NotEmpty
    private List<Integer> userIds;

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }
}
