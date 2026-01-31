package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.example.demo.dto.request.AddRoleRequest;
import com.example.demo.dto.response.APIResponse;
import com.example.demo.dto.response.RoleResponse;
import com.example.demo.service.ProjectRoleService;

@RestController
@RequestMapping("/api/projects/{projectId}/roles")
public class ProjectRoleController {
    @Autowired
    private ProjectRoleService roleService;

    @GetMapping
    public ResponseEntity<APIResponse<List<RoleResponse>>> list(@PathVariable Integer projectId) {
        return ResponseEntity.ok(APIResponse.<List<RoleResponse>>builder()
            .code(200)
            .message("get detail project success")
            .result(roleService.listRoles(projectId))
            .build());
    }

    @PostMapping
    public ResponseEntity<APIResponse<RoleResponse>> add(
        @PathVariable Integer projectId,
        @Valid @RequestBody AddRoleRequest req
    ) {
        return ResponseEntity.ok(APIResponse.<RoleResponse>builder()
            .code(200)
            .message("get detail project success")
            .result(roleService.addRole(projectId, req))
            .build());
    }

    @DeleteMapping("/{roleName}")
    public void remove(@PathVariable Integer projectId, @PathVariable String roleName) {
        roleService.removeRole(projectId, roleName);
    }
}
