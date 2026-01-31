package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.example.demo.dto.request.CreateGroupRequest;
import com.example.demo.dto.request.UpdateGroupRequest;
import com.example.demo.dto.response.APIResponse;
import com.example.demo.dto.response.ProjectGroupResponse;
import com.example.demo.service.ProjectGroupService;

@RestController
@RequestMapping("/api/projects/{projectId}/groups")
public class ProjectGroupController {
    @Autowired
    private ProjectGroupService groupService;

    @GetMapping
    public ResponseEntity<APIResponse<List<ProjectGroupResponse>>> list(@PathVariable Integer projectId) {
        return ResponseEntity.ok(APIResponse.<List<ProjectGroupResponse>>builder()
            .code(200)
            .message("get detail project success")
            .result(groupService.listGroups(projectId))
            .build());
    }

    @PostMapping
    public ResponseEntity<APIResponse<ProjectGroupResponse>> create(
        @PathVariable Integer projectId,
        @Valid @RequestBody CreateGroupRequest req
    ) {
        return ResponseEntity.ok(APIResponse.<ProjectGroupResponse>builder()
            .code(200)
            .message("get detail project success")
            .result(groupService.createGroup(projectId, req))
            .build());
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<APIResponse<ProjectGroupResponse>> update(
        @PathVariable Integer projectId,
        @PathVariable Integer groupId,
        @Valid @RequestBody UpdateGroupRequest req
    ) {
        return ResponseEntity.ok(APIResponse.<ProjectGroupResponse>builder()
            .code(200)
            .message("get detail project success")
            .result(groupService.updateGroup(projectId, groupId, req))
            .build());
    }

    @DeleteMapping("/{groupId}")
    public void delete(@PathVariable Integer projectId, @PathVariable Integer groupId) {
        groupService.deleteGroup(projectId, groupId);
    }
}
