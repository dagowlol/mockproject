package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.dto.request.CreateProjectRequest;
import com.example.demo.dto.request.UpdateProjectRequest;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.ProjectCardResponse;
import com.example.demo.dto.response.ProjectDetailResponse;
import com.example.demo.model.Project;
import com.example.demo.model.ProjectStatus;
import com.example.demo.repo.ProjectGroupRepository;
import com.example.demo.repo.ProjectMemberRepository;
import com.example.demo.repo.ProjectRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectGroupRepository projectGroupRepository;

    public ProjectService(
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository,
        ProjectGroupRepository projectGroupRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectGroupRepository = projectGroupRepository;
    }

    public ProjectDetailResponse getDetail(Integer projectId) {
        Project project = getRequiredProject(projectId);
        return toDetailResponse(project);
    }

    public ProjectDetailResponse create(CreateProjectRequest req) {
        Project project = new Project();
        project.setName(req.getName());
        project.setDescription(req.getDescription());
        project.setStatus(req.getStatus());
        project.setStartDate(req.getStartDate());
        project.setEndDate(req.getEndDate());
        return toDetailResponse(projectRepository.save(project));
    }

    public ProjectDetailResponse update(Integer projectId, UpdateProjectRequest req) {
        Project project = getRequiredProject(projectId);
        project.setName(req.getName());
        project.setDescription(req.getDescription());
        project.setStatus(req.getStatus());
        project.setStartDate(req.getStartDate());
        project.setEndDate(req.getEndDate());
        return toDetailResponse(projectRepository.save(project));
    }

    public ProjectDetailResponse close(Integer projectId) {
        Project project = getRequiredProject(projectId);
        project.setStatus(ProjectStatus.DONE);
        project.setIsArchive(false);
        return toDetailResponse(projectRepository.save(project));
    }

    public ProjectDetailResponse archive(Integer projectId) {
        Project project = getRequiredProject(projectId);
        project.setStatus( ProjectStatus.ARCHIVED);
        return toDetailResponse(projectRepository.save(project));
    }

    public ProjectDetailResponse restore(Integer projectId) {
        Project project = getRequiredProject(projectId);
        project.setStatus(ProjectStatus.ACTIVE);
        return toDetailResponse(projectRepository.save(project));
    }

    public void delete(Integer projectId) {
        projectRepository.deleteById(projectId);
    }

    public PageResponse<ProjectCardResponse> list(ProjectStatus status, String q, Pageable pageable) {
        Page<Project> page = projectRepository.search(status, normalizeQuery(q), pageable);
        List<ProjectCardResponse> cards = page.getContent()
            .stream()
            .map(this::toCardResponse)
            .collect(Collectors.toList());
            log.info("Project : " + cards);
        return new PageResponse<>(
            cards,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

    private Project getRequiredProject(Integer projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    private String normalizeQuery(String q) {
        if (q == null) {
            return null;
        }
        String trimmed = q.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProjectDetailResponse toDetailResponse(Project project) {
        ProjectDetailResponse response = new ProjectDetailResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setStatus(project.getStatus());
        response.setIsArchive(Boolean.TRUE.equals(project.getIsArchive()));
        response.setStartDate(project.getStartDate());
        response.setEndDate(project.getEndDate());
        return response;
    }

    private ProjectCardResponse toCardResponse(Project project) {
        ProjectCardResponse response = new ProjectCardResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setStatus(project.getStatus());
        response.setIsArchive(Boolean.TRUE.equals(project.getIsArchive()));
        response.setStartDate(project.getStartDate());
        response.setEndDate(project.getEndDate());
        response.setMemberCount(projectMemberRepository.countByProjectId(project.getId()));
        response.setGroupCount(projectGroupRepository.countByProjectId(project.getId()));
        return response;
    }
}
