package com.example.demo.repo;

import com.example.demo.model.DailyReportTaskItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyReportTaskItemRepository extends JpaRepository<DailyReportTaskItem, Integer> {
}
