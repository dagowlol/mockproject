package com.example.demo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.demo.dto.response.EvaluationRoundCriterionResponse;
import com.example.demo.model.EvaluationRoundCriterion;

@Mapper(componentModel = "spring")
public interface EvaluationRoundCriterionMapper {
    @Mapping(target = "name", source = "nameSnapshot")
    EvaluationRoundCriterionResponse toResponse(EvaluationRoundCriterion criterion);
}
