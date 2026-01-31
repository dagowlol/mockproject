package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.CreateTemplateRoundRequest;
import com.example.demo.dto.request.UpdateTemplateRoundRequest;
import com.example.demo.dto.response.APIResponse;
import com.example.demo.dto.response.EvaluationRoundResponse;
import com.example.demo.exception.AppException;
import com.example.demo.model.User;
import com.example.demo.repo.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.TemplateRoundService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/evaluation-rounds/templates")
@CrossOrigin(origins = "http://localhost:4200")
public class TemplateRoundController {

    private final TemplateRoundService templateRoundService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public TemplateRoundController(
        TemplateRoundService templateRoundService,
        JwtUtil jwtUtil,
        UserRepository userRepository
    ) {
        this.templateRoundService = templateRoundService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<APIResponse<List<EvaluationRoundResponse>>> listTemplates(
        HttpServletRequest request
    ) {
        requireAdminRole(request);
        return ResponseEntity.ok(APIResponse.<List<EvaluationRoundResponse>>builder()
            .code(200)
            .message("get template rounds success")
            .result(templateRoundService.listTemplateRounds())
            .build());
    }

    @PostMapping
    public ResponseEntity<APIResponse<EvaluationRoundResponse>> createTemplate(
        HttpServletRequest request,
        @Valid @RequestBody CreateTemplateRoundRequest req
    ) {
        requireAdminRole(request);
        Integer actorId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<EvaluationRoundResponse>builder()
            .code(200)
            .message("create template round success")
            .result(templateRoundService.createTemplateRound(req, actorId))
            .build());
    }

    @PutMapping("/{templateId}")
    public ResponseEntity<APIResponse<EvaluationRoundResponse>> updateTemplate(
        HttpServletRequest request,
        @PathVariable Integer templateId,
        @Valid @RequestBody UpdateTemplateRoundRequest req
    ) {
        requireAdminRole(request);
        Integer actorId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<EvaluationRoundResponse>builder()
            .code(200)
            .message("update template round success")
            .result(templateRoundService.updateTemplateRound(templateId, req, actorId))
            .build());
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<APIResponse<Void>> deleteTemplate(
        HttpServletRequest request,
        @PathVariable Integer templateId
    ) {
        requireAdminRole(request);
        templateRoundService.deleteTemplateRound(templateId);
        return ResponseEntity.ok(APIResponse.<Void>builder()
            .code(200)
            .message("delete template round success")
            .result(null)
            .build());
    }

    private Integer getActorId(HttpServletRequest request) {
        String username = extractUsername(request);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new AppException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));
        return user.getId();
    }

    private void requireAdminRole(HttpServletRequest request) {
        String role = extractRole(request);
        if (!"SUPER_ADMIN".equals(role) && !"ADMIN".equals(role)) {
            throw new AppException(org.springframework.http.HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private String extractUsername(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractUsername(token);
    }

    private String extractRole(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractRole(token);
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AppException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing token");
        }
        return authorization.substring(7);
    }
}
