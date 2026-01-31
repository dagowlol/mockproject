package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.ProjectMember;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {
    List<ProjectMember> findByProjectId(Integer projectId);

    Optional<ProjectMember> findByProjectIdAndUserId(Integer projectId, Integer userId);

    void deleteByProjectIdAndUserId(Integer projectId, Integer userId);

    long countByProjectId(Integer projectId);
}
