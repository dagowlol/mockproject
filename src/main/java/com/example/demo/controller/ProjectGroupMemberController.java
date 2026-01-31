package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.example.demo.dto.response.APIResponse;
import com.example.demo.dto.response.ProjectGroupMemberResponse;
import com.example.demo.dto.request.UpdateGroupMemberRoleRequest;
import com.example.demo.service.ProjectGroupMemberService;

@RestController
@RequestMapping("/api/projects/{projectId}/groups/{groupId}/members")
public class ProjectGroupMemberController {
    @Autowired
    private ProjectGroupMemberService service;

    @GetMapping
    public ResponseEntity<APIResponse<List<ProjectGroupMemberResponse>>> list(
        @PathVariable Integer projectId,
        @PathVariable Integer groupId
    ) {
        List<ProjectGroupMemberResponse> members = service.list(projectId, groupId);
        return ResponseEntity.ok(
            APIResponse.<List<ProjectGroupMemberResponse>>builder()
                .code(200)
                .message("Get group members success")
                .result(members)
                .build()
        );
    }

    @PostMapping("/addToGroup/{userId}")
    public ResponseEntity<APIResponse<ProjectGroupMemberResponse>> add(
        @PathVariable Integer projectId,
        @PathVariable Integer groupId,
        @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(APIResponse.<ProjectGroupMemberResponse>builder()
            .code(200)
            .message("get detail project success")
            .result(service.add(projectId, groupId, userId))
            .build());
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<APIResponse<ProjectGroupMemberResponse>> updateRole(
        @PathVariable Integer projectId,
        @PathVariable Integer groupId,
        @PathVariable Integer userId,
        @Valid @RequestBody UpdateGroupMemberRoleRequest req
    ) {
        return ResponseEntity.ok(APIResponse.<ProjectGroupMemberResponse>builder()
            .code(200)
            .message("update group member role success")
            .result(service.updateRole(projectId, groupId, userId, req))
            .build());
    }

    @DeleteMapping("/{userId}")
    public void remove(
        @PathVariable Integer projectId,
        @PathVariable Integer groupId,
        @PathVariable Integer userId
    ) {
        service.remove(projectId, groupId, userId);
    }
}
