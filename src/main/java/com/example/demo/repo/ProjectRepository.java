package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Project;
import com.example.demo.model.ProjectStatus;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    @Query("""
        select p
        from Project p
        where (:status is null or p.status = :status)
          and (
            :q is null
            or lower(p.name) like lower(concat('%', :q, '%'))
            or lower(p.description) like lower(concat('%', :q, '%'))
          )
        """)
    Page<Project> search(@Param("status") ProjectStatus status, @Param("q") String q, Pageable pageable);
}
