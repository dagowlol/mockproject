package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.ProjectGroupMember;

@Repository
public interface ProjectGroupMemberRepository extends JpaRepository<ProjectGroupMember, Integer> {
    List<ProjectGroupMember> findByGroupId(Integer groupId);

    Optional<ProjectGroupMember> findByGroupIdAndUserId(Integer groupId, Integer userId);

    void deleteByGroupIdAndUserId(Integer groupId, Integer userId);
}
