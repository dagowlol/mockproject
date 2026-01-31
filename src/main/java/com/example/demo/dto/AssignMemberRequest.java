package com.example.demo.dto;

import java.util.List;

public class AssignMemberRequest {
    private List<Integer> memberIds;
    private String mode;

    public List<Integer> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Integer> memberIds) {
        this.memberIds = memberIds;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
