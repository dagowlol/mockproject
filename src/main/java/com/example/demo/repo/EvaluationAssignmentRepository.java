package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.EvaluationAssignment;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface EvaluationAssignmentRepository extends JpaRepository<EvaluationAssignment, Integer> {
    boolean existsByTargetIdAndEvaluatorUserId(Integer targetId, Integer evaluatorUserId);
    Optional<EvaluationAssignment> findFirstByTargetId(Integer targetId);
    java.util.List<EvaluationAssignment> findByTargetId(Integer targetId);
    void deleteByTargetIdAndEvaluatorUserId(Integer targetId, Integer evaluatorUserId);
    void deleteByTargetId(Integer targetId);

    @Query("select count(a) from EvaluationAssignment a join EvaluationTarget t on a.targetId = t.id where t.roundId = ?1")
    long countByRoundId(Integer roundId);

    @Query("select a from EvaluationAssignment a join EvaluationTarget t on a.targetId = t.id where t.roundId = ?1")
    java.util.List<EvaluationAssignment> findByRoundId(Integer roundId);

    @Query("""
        select count(a) from EvaluationAssignment a
        join EvaluationTarget t on a.targetId = t.id
        where t.roundId = ?1 and a.status in ('SUBMITTED', 'LOCKED')
        """)
    long countCompletedByRoundId(Integer roundId);

    @Query("select count(a) from EvaluationAssignment a where a.status in ('SUBMITTED', 'LOCKED')")
    long countCompletedAll();

    java.util.List<EvaluationAssignment> findByEvaluatorUserIdOrderByDueAtAsc(Integer evaluatorUserId);

    long countByEvaluatorUserIdAndStatus(Integer evaluatorUserId, com.example.demo.model.EvaluationAssignmentStatus status);

    @Query("""
        select count(a) from EvaluationAssignment a
        where a.evaluatorUserId = ?1 and a.status = 'PENDING' and a.dueAt is not null and a.dueAt <= ?2
        """)
    long countDueSoon(Integer evaluatorUserId, java.time.LocalDateTime dueLimit);
}
