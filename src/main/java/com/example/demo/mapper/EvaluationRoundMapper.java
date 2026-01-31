package com.example.demo.mapper;

import org.mapstruct.Mapper;

import com.example.demo.dto.response.EvaluationRoundResponse;
import com.example.demo.model.EvaluationRound;

@Mapper(componentModel = "spring")
public interface EvaluationRoundMapper {
    EvaluationRoundResponse toResponse(EvaluationRound round);
}
