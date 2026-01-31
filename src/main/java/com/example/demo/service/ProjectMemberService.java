package com.example.demo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.request.AddMemberRequest;
import com.example.demo.dto.request.AddMembersRequest;
import com.example.demo.dto.request.UpdateMemberRoleRequest;
import com.example.demo.dto.response.ProjectMemberResponse;
import com.example.demo.model.Project;
import com.example.demo.model.ProjectMember;
import com.example.demo.model.User;
import com.example.demo.repo.ProjectMemberRepository;
import com.example.demo.repo.ProjectRepository;
import com.example.demo.repo.UserRepository;

@Service
public class ProjectMemberService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;

    public ProjectMemberService(
        ProjectRepository projectRepository,
        ProjectMemberRepository memberRepository,
        UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    public List<ProjectMemberResponse> listMembers(Integer projectId) {
        return memberRepository.findByProjectId(projectId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ProjectMemberResponse addMember(Integer projectId, AddMemberRequest req) {
        Project project = getRequiredProject(projectId);
        User user = getRequiredUser(req.getUserId());
        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, req.getUserId())
            .orElseGet(ProjectMember::new);
        member.setProject(project);
        member.setUser(user);
        member.setRoleName(req.getRoleName());
        return toResponse(memberRepository.save(member));
    }

    public List<ProjectMemberResponse> addMembers(Integer projectId, AddMembersRequest req) {
        Project project = getRequiredProject(projectId);
        List<Integer> userIds = req.getUserIds();
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("User ids required");
        }
        List<Integer> uniqueIds = userIds.stream()
            .filter((id) -> id != null)
            .distinct()
            .collect(Collectors.toList());
        Map<Integer, User> usersById = new HashMap<>();
        userRepository.findAllById(uniqueIds).forEach((user) -> usersById.put(user.getId(), user));
        if (usersById.size() != uniqueIds.size()) {
            List<Integer> missing = uniqueIds.stream()
                .filter((id) -> !usersById.containsKey(id))
                .collect(Collectors.toList());
            throw new IllegalArgumentException("User not found: " + missing);
        }
        Map<Integer, ProjectMember> membersByUser = memberRepository.findByProjectId(projectId)
            .stream()
            .collect(Collectors.toMap((member) -> member.getUser().getId(), (member) -> member));
        List<ProjectMember> toSave = uniqueIds.stream()
            .map((id) -> {
                ProjectMember member = membersByUser.getOrDefault(id, new ProjectMember());
                member.setProject(project);
                member.setUser(usersById.get(id));
                if (req.getRoleName() != null) {
                    member.setRoleName(req.getRoleName());
                }
                return member;
            })
            .collect(Collectors.toList());
        return memberRepository.saveAll(toSave)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ProjectMemberResponse updateRole(Integer projectId, Integer userId, UpdateMemberRoleRequest req) {
        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        member.setRoleName(req.getRoleName());
        return toResponse(memberRepository.save(member));
    }

    @Transactional
    public void removeMember(Integer projectId, Integer userId) {
        memberRepository.deleteByProjectIdAndUserId(projectId, userId);
    }

    private Project getRequiredProject(Integer projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    private User getRequiredUser(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private ProjectMemberResponse toResponse(ProjectMember member) {
        ProjectMemberResponse response = new ProjectMemberResponse();
        response.setUserId(member.getUser().getId());
        response.setUsername(member.getUser().getUsername());
        response.setFullName(member.getUser().getFullName());
        response.setEmail(member.getUser().getEmail());
        response.setRoleName(member.getRoleName());
        return response;
    }
}
