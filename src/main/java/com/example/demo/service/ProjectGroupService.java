package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.request.CreateGroupRequest;
import com.example.demo.dto.request.UpdateGroupRequest;
import com.example.demo.dto.response.ProjectGroupResponse;
import com.example.demo.model.Project;
import com.example.demo.model.ProjectGroup;
import com.example.demo.repo.ProjectGroupRepository;
import com.example.demo.repo.ProjectRepository;

@Service
public class ProjectGroupService {
    private final ProjectRepository projectRepository;
    private final ProjectGroupRepository groupRepository;

    public ProjectGroupService(ProjectRepository projectRepository, ProjectGroupRepository groupRepository) {
        this.projectRepository = projectRepository;
        this.groupRepository = groupRepository;
    }

    public List<ProjectGroupResponse> listGroups(Integer projectId) {
        return groupRepository.findByProjectId(projectId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ProjectGroupResponse createGroup(Integer projectId, CreateGroupRequest req) {
        Project project = getRequiredProject(projectId);
        ProjectGroup group = new ProjectGroup();
        group.setProject(project);
        group.setName(req.getName());
        group.setDescription(req.getDescription());
        return toResponse(groupRepository.save(group));
    }

    public ProjectGroupResponse updateGroup(Integer projectId, Integer groupId, UpdateGroupRequest req) {
        ProjectGroup group = getRequiredGroup(projectId, groupId);
        group.setName(req.getName());
        group.setDescription(req.getDescription());
        return toResponse(groupRepository.save(group));
    }

    public void deleteGroup(Integer projectId, Integer groupId) {
        ProjectGroup group = getRequiredGroup(projectId, groupId);
        groupRepository.delete(group);
    }

    private Project getRequiredProject(Integer projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    private ProjectGroup getRequiredGroup(Integer projectId, Integer groupId) {
        ProjectGroup group = groupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        if (!group.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Group does not belong to project");
        }
        return group;
    }

    private ProjectGroupResponse toResponse(ProjectGroup group) {
        ProjectGroupResponse response = new ProjectGroupResponse();
        response.setId(group.getId());
        response.setProjectId(group.getProject().getId());
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        return response;
    }
}
