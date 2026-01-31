package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.EvaluationRound;

@Repository
public interface EvaluationRoundRepository extends JpaRepository<EvaluationRound, Integer> {
    java.util.List<EvaluationRound> findByIsTemplateRoundFalseOrderByCreatedAtDesc();
    java.util.List<EvaluationRound> findByIsTemplateRoundTrueOrderByCreatedAtDesc();
}
