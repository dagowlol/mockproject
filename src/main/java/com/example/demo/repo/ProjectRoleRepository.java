package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.ProjectRole;

@Repository
public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Integer> {
    List<ProjectRole> findByProjectId(Integer projectId);

    Optional<ProjectRole> findByProjectIdAndRoleName(Integer projectId, String roleName);

    void deleteByProjectIdAndRoleName(Integer projectId, String roleName);
}
