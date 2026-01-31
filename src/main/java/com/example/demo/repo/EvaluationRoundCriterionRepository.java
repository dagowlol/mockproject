package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.EvaluationRoundCriterion;

@Repository
public interface EvaluationRoundCriterionRepository extends JpaRepository<EvaluationRoundCriterion, Integer> {
    List<EvaluationRoundCriterion> findByRoundIdOrderByDisplayOrderAsc(Integer roundId);

    @Query("select coalesce(sum(c.weight), 0) from EvaluationRoundCriterion c where c.roundId = ?1")
    Integer sumWeightByRoundId(Integer roundId);

    @Query("select coalesce(sum(c.weight), 0) from EvaluationRoundCriterion c where c.roundId = ?1 and c.id <> ?2")
    Integer sumWeightByRoundIdExcluding(Integer roundId, Integer roundCriterionId);

    void deleteByRoundIdAndId(Integer roundId, Integer id);

    void deleteByRoundId(Integer roundId);
}
