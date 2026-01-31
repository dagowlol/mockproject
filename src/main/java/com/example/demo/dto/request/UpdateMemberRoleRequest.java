package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateMemberRoleRequest {
    @NotBlank
    private String roleName;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
