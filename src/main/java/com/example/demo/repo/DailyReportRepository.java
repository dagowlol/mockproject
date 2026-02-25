package com.example.demo.repo;

import com.example.demo.model.DailyReport;
import com.example.demo.model.DailyReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyReportRepository
                extends JpaRepository<DailyReport, Integer>, JpaSpecificationExecutor<DailyReport> {

        Optional<DailyReport> findFirstByUser_IdAndProject_IdAndStatusOrderByUpdatedAtDesc(Integer userId,
                        Integer projectId, DailyReportStatus status);

        boolean existsByUser_IdAndProject_IdAndReportDateAndStatus(Integer userId, Integer projectId,
                        LocalDate reportDate,
                        DailyReportStatus status);

        @Query("SELECT r.reportDate, COUNT(r) FROM DailyReport r WHERE r.project.id = :projectId AND r.status = 'SUBMITTED' AND r.reportDate BETWEEN :from AND :to GROUP BY r.reportDate")
        List<Object[]> countSubmittedByDay(@Param("projectId") Integer projectId, @Param("from") LocalDate from,
                        @Param("to") LocalDate to);
}
