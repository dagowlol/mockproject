package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.request.AddRoleRequest;
import com.example.demo.dto.response.RoleResponse;
import com.example.demo.model.Project;
import com.example.demo.model.ProjectRole;
import com.example.demo.repo.ProjectRepository;
import com.example.demo.repo.ProjectRoleRepository;

@Service
public class ProjectRoleService {
    private final ProjectRepository projectRepository;
    private final ProjectRoleRepository roleRepository;

    public ProjectRoleService(ProjectRepository projectRepository, ProjectRoleRepository roleRepository) {
        this.projectRepository = projectRepository;
        this.roleRepository = roleRepository;
    }

    public List<RoleResponse> listRoles(Integer projectId) {
        return roleRepository.findByProjectId(projectId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public RoleResponse addRole(Integer projectId, AddRoleRequest req) {
        Project project = getRequiredProject(projectId);
        ProjectRole role = roleRepository.findByProjectIdAndRoleName(projectId, req.getRoleName())
            .orElseGet(ProjectRole::new);
        role.setProject(project);
        role.setRoleName(req.getRoleName());
        return toResponse(roleRepository.save(role));
    }

    public void removeRole(Integer projectId, String roleName) {
        roleRepository.deleteByProjectIdAndRoleName(projectId, roleName);
    }

    private Project getRequiredProject(Integer projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    private RoleResponse toResponse(ProjectRole role) {
        RoleResponse response = new RoleResponse();
        response.setRoleName(role.getRoleName());
        return response;
    }
}
