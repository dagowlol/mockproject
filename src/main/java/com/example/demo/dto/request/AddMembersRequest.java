package com.example.demo.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public class AddMembersRequest {
    @NotEmpty
    private List<Integer> userIds;
    private String roleName;

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
