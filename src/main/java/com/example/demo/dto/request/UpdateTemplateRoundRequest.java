package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateTemplateRoundRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private Integer scoringScale;

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

    public Integer getScoringScale() {
        return scoringScale;
    }

    public void setScoringScale(Integer scoringScale) {
        this.scoringScale = scoringScale;
    }
}
