package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.response.ProjectGroupMemberResponse;
import com.example.demo.dto.request.UpdateGroupMemberRoleRequest;
import com.example.demo.model.ProjectGroup;
import com.example.demo.model.ProjectGroupMember;
import com.example.demo.model.User;
import com.example.demo.repo.ProjectGroupMemberRepository;
import com.example.demo.repo.ProjectGroupRepository;
import com.example.demo.repo.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectGroupMemberService {
    private final ProjectGroupRepository groupRepository;
    private final ProjectGroupMemberRepository memberRepository;
    private final UserRepository userRepository;

    public ProjectGroupMemberService(
        ProjectGroupRepository groupRepository,
        ProjectGroupMemberRepository memberRepository,
        UserRepository userRepository
    ) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    public List<ProjectGroupMemberResponse> list(Integer projectId, Integer groupId) {
        ProjectGroup group = getRequiredGroup(projectId, groupId);
        return memberRepository.findByGroupId(group.getId())
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ProjectGroupMemberResponse add(Integer projectId, Integer groupId, Integer userId) {
        ProjectGroup group = getRequiredGroup(projectId, groupId);
        User user = getRequiredUser(userId);
        ProjectGroupMember member = memberRepository.findByGroupIdAndUserId(groupId, userId)
            .orElseGet(ProjectGroupMember::new);
        member.setGroup(group);
        member.setUser(user);
        if (member.getRoleName() == null || member.getRoleName().isBlank()) {
            member.setRoleName("MEMBER");
        }
        return toResponse(memberRepository.save(member));
    }

    public ProjectGroupMemberResponse updateRole(Integer projectId, Integer groupId, Integer userId, UpdateGroupMemberRoleRequest req) {
        ProjectGroup group = getRequiredGroup(projectId, groupId);
        ProjectGroupMember member = memberRepository.findByGroupIdAndUserId(group.getId(), userId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        member.setRoleName(req.getRoleName());
        return toResponse(memberRepository.save(member));
    }

    @Transactional
    public void remove(Integer projectId, Integer groupId, Integer userId) {
        getRequiredGroup(projectId, groupId);
        memberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    private ProjectGroup getRequiredGroup(Integer projectId, Integer groupId) {
        ProjectGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        if (!group.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Group does not belong to project");
        }
        return group;
    }

    private User getRequiredUser(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private ProjectGroupMemberResponse toResponse(ProjectGroupMember member) {
        ProjectGroupMemberResponse response = new ProjectGroupMemberResponse();
        response.setGroupId(member.getGroup().getId());
        response.setUserId(member.getUser().getId());
        response.setUsername(member.getUser().getUsername());
        response.setFullName(member.getUser().getFullName());
        response.setEmail(member.getUser().getEmail());
        response.setRoleName(member.getRoleName());
        return response;
    }
}
