package com.example.demo.repo;

import com.example.demo.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query("SELECT t.id FROM Task t WHERE t.project.id = :projectId")
    Set<Integer> findSelectableTaskIdsForUserInProject(@Param("projectId") Integer projectId,
            @Param("userId") Integer userId);

    java.util.List<Task> findByProjectId(Integer projectId);
}
