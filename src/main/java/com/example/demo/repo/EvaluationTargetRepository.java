package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.EvaluationTarget;

@Repository
public interface EvaluationTargetRepository extends JpaRepository<EvaluationTarget, Integer> {
    List<EvaluationTarget> findByRoundId(Integer roundId);

    List<EvaluationTarget> findByUserId(Integer userId);

    boolean existsByRoundIdAndUserId(Integer roundId, Integer userId);

    int countByRoundId(Integer roundId);
}
