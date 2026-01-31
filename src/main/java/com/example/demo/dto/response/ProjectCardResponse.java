package com.example.demo.dto.response;

import com.example.demo.model.ProjectStatus;

public class ProjectCardResponse {
    private Integer id;
    private String name;
    private String description;
    private ProjectStatus status;
    private boolean isArchive;
    private String startDate;
    private String endDate;
    private long memberCount;
    private long groupCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public boolean getIsArchive() {
        return isArchive;
    }

    public void setIsArchive(boolean isArchive) {
        this.isArchive = isArchive;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(long memberCount) {
        this.memberCount = memberCount;
    }

    public long getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(long groupCount) {
        this.groupCount = groupCount;
    }
}
