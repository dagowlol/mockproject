package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.example.demo.dto.request.CreateProjectRequest;
import com.example.demo.dto.request.UpdateProjectRequest;
import com.example.demo.dto.response.APIResponse;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.ProjectCardResponse;
import com.example.demo.dto.response.ProjectDetailResponse;
import com.example.demo.model.ProjectStatus;
import com.example.demo.service.ProjectService;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/{projectId}")
    public ResponseEntity<APIResponse<ProjectDetailResponse>> detail(@PathVariable Integer projectId) {
        ProjectDetailResponse response = projectService.getDetail(projectId);
        return ResponseEntity.ok(APIResponse.<ProjectDetailResponse>builder()
            .code(200)
            .message("get detail project success")
            .result(response)
            .build());
    }

    @PostMapping
    public ResponseEntity<APIResponse<ProjectDetailResponse>> create(@Valid @RequestBody CreateProjectRequest req) {
        return ResponseEntity.ok(APIResponse.<ProjectDetailResponse>builder()
            .code(200)
            .message("get detail project success")
            .result(projectService.create(req))
            .build());
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<APIResponse<ProjectDetailResponse>> update(
        @PathVariable Integer projectId,
        @Valid @RequestBody UpdateProjectRequest req
    ) {
        return ResponseEntity.ok(APIResponse.<ProjectDetailResponse>builder()
            .code(200)
            .message("get detail project success")
            .result(projectService.update(projectId, req))
            .build());
    }

    @PatchMapping("/{projectId}/close")
    public ResponseEntity<APIResponse<ProjectDetailResponse>> close(@PathVariable Integer projectId) {
        return ResponseEntity.ok(APIResponse.<ProjectDetailResponse>builder()
            .code(200)
            .message("get detail project success")
            .result(projectService.close(projectId))
            .build());
    }

    @DeleteMapping("/{projectId}")
    public void delete(@PathVariable Integer projectId) {
        projectService.delete(projectId);
    }

    @PatchMapping("/{projectId}/archive")
    public ResponseEntity<APIResponse<ProjectDetailResponse>> archive(@PathVariable Integer projectId) {
        return ResponseEntity.ok(APIResponse.<ProjectDetailResponse>builder()
            .code(200)
            .message("get detail project success")
            .result(projectService.archive(projectId))
            .build());
    }

    @PatchMapping("/{projectId}/restore")
    public ResponseEntity<APIResponse<ProjectDetailResponse>> restore(@PathVariable Integer projectId) {
        return ResponseEntity.ok(APIResponse.<ProjectDetailResponse>builder()
            .code(200)
            .message("get detail project success")
            .result(projectService.restore(projectId))
            .build());
    }

    @GetMapping
    public ResponseEntity<APIResponse<PageResponse<ProjectCardResponse>>> list(
        @RequestParam(required = false) ProjectStatus status,
        @RequestParam(required = false) String q,
        Pageable pageable
    ) {
        APIResponse<PageResponse<ProjectCardResponse>> response = APIResponse
            .<PageResponse<ProjectCardResponse>>builder()
            .result(projectService.list(status, q, pageable))
            .message("success")
            .code(200)
            .build();
        return ResponseEntity.ok(response);
    }
}
