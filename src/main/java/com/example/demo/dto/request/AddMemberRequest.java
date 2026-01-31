package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;

public class AddMemberRequest {
    @NotNull
    private Integer userId;
    private String roleName;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
