package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.example.demo.dto.request.AddMemberRequest;
import com.example.demo.dto.request.AddMembersRequest;
import com.example.demo.dto.request.UpdateMemberRoleRequest;
import com.example.demo.dto.response.APIResponse;
import com.example.demo.dto.response.ProjectMemberResponse;
import com.example.demo.service.ProjectMemberService;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {
    @Autowired
    private ProjectMemberService memberService;

    @GetMapping
    public ResponseEntity<APIResponse<List<ProjectMemberResponse>>> list(@PathVariable Integer projectId) {
        return ResponseEntity.ok(APIResponse.<List<ProjectMemberResponse>>builder()
            .code(200)
            .message("get detail project success")
            .result(memberService.listMembers(projectId))
            .build());
    }

    @PostMapping
    public ResponseEntity<APIResponse<ProjectMemberResponse>> add(
        @PathVariable Integer projectId,
        @Valid @RequestBody AddMemberRequest req
    ) {
        ProjectMemberResponse response = memberService.addMember(projectId, req);
        return ResponseEntity.ok(APIResponse.<ProjectMemberResponse>builder()
            .code(200)
            .message("get detail project success")
            .result(response)
            .build());
    }

    @PostMapping("/bulk")
    public ResponseEntity<APIResponse<List<ProjectMemberResponse>>> addBulk(
        @PathVariable Integer projectId,
        @Valid @RequestBody AddMembersRequest req
    ) {
        return ResponseEntity.ok(APIResponse.<List<ProjectMemberResponse>>builder()
            .code(200)
            .message("add members success")
            .result(memberService.addMembers(projectId, req))
            .build());
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<APIResponse<ProjectMemberResponse>> updateRole(
        @PathVariable Integer projectId,
        @PathVariable Integer userId,
        @RequestBody UpdateMemberRoleRequest req
    ) {
        return ResponseEntity.ok(APIResponse.<ProjectMemberResponse>builder()
            .code(200)
            .message("get detail project success")
            .result(memberService.updateRole(projectId, userId, req))
            .build());
    }

    @DeleteMapping("/{userId}")
    public void remove(@PathVariable Integer projectId, @PathVariable Integer userId) {
        memberService.removeMember(projectId, userId);
    }
}
