package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "daily_report_task_items", uniqueConstraints = @UniqueConstraint(name = "uq_drt", columnNames = {
        "report_id", "task_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyReportTaskItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_task_item_id")
    private Integer reportTaskItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private DailyReport report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "progress_note", columnDefinition = "text")
    private String progressNote;

    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes;
}
