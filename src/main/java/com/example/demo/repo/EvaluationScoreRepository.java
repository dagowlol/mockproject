package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.EvaluationScore;

@Repository
public interface EvaluationScoreRepository extends JpaRepository<EvaluationScore, Integer> {
    List<EvaluationScore> findByFormId(Integer formId);
    List<EvaluationScore> findByFormIdIn(List<Integer> formIds);
    Optional<EvaluationScore> findByFormIdAndCriterionId(Integer formId, Integer criterionId);
}
