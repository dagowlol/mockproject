package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "evaluation_delegations")
@Getter
@Setter
public class EvaluationDelegation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delegation_id")
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    private Integer assignmentId;

    @Column(name = "from_evaluator_id", nullable = false)
    private Integer fromEvaluatorId;

    @Column(name = "to_evaluator_id", nullable = false)
    private Integer toEvaluatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationDelegationStatus status = EvaluationDelegationStatus.REQUESTED;

    @Column(columnDefinition = "text")
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
