package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.EvaluationDelegation;
import com.example.demo.model.EvaluationDelegationStatus;

@Repository
public interface EvaluationDelegationRepository extends JpaRepository<EvaluationDelegation, Long> {
    List<EvaluationDelegation> findByFromEvaluatorIdOrderByCreatedAtDesc(Integer fromEvaluatorId);

    List<EvaluationDelegation> findByToEvaluatorIdAndStatus(Integer toEvaluatorId, EvaluationDelegationStatus status);
}
