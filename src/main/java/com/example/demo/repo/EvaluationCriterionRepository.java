package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.EvaluationCriterion;

@Repository
public interface EvaluationCriterionRepository extends JpaRepository<EvaluationCriterion, Integer> {
    List<EvaluationCriterion> findByIsTemplateTrueOrderByDisplayOrderAsc();

    @Query("select coalesce(sum(c.weight), 0) from EvaluationCriterion c where c.isTemplate = true")
    Integer sumTemplateWeight();
}
