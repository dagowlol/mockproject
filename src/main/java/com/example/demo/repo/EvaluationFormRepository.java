package com.example.demo.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.EvaluationForm;

@Repository
public interface EvaluationFormRepository extends JpaRepository<EvaluationForm, Integer> {
    Optional<EvaluationForm> findByAssignmentId(Integer assignmentId);
    java.util.List<EvaluationForm> findByAssignmentIdIn(java.util.List<Integer> assignmentIds);
}
