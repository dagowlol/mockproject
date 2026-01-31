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

import com.example.demo.dto.request.CreateEvaluationCriterionRequest;
import com.example.demo.dto.response.APIResponse;
import com.example.demo.dto.response.EvaluationRoundCriterionResponse;
import com.example.demo.exception.AppException;
import com.example.demo.model.EvaluationCriterion;
import com.example.demo.model.User;
import com.example.demo.repo.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.EvaluationRoundCriteriaService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/evaluation-rounds")
@CrossOrigin(origins = "http://localhost:4200")
public class EvaluationRoundCriteriaController {

    private final EvaluationRoundCriteriaService evaluationRoundCriteriaService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public EvaluationRoundCriteriaController(
        EvaluationRoundCriteriaService evaluationRoundCriteriaService,
        JwtUtil jwtUtil,
        UserRepository userRepository
    ) {
        this.evaluationRoundCriteriaService = evaluationRoundCriteriaService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @GetMapping("/{roundId}/criteria")
    public ResponseEntity<APIResponse<List<EvaluationRoundCriterionResponse>>> listCriteria(
        @PathVariable Integer roundId
    ) {
        return ResponseEntity.ok(APIResponse.<List<EvaluationRoundCriterionResponse>>builder()
            .code(200)
            .message("get evaluation criteria success")
            .result(evaluationRoundCriteriaService.listCriteria(roundId))
            .build());
    }

    @PostMapping("/{roundId}/criteria")
    public ResponseEntity<APIResponse<EvaluationRoundCriterionResponse>> addCriterion(
        HttpServletRequest request,
        @PathVariable Integer roundId,
        @Valid @RequestBody CreateEvaluationCriterionRequest req
    ) {
        requireAdminRole(request);
        Integer actorId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<EvaluationRoundCriterionResponse>builder()
            .code(200)
            .message("create evaluation criterion success")
            .result(evaluationRoundCriteriaService.addCriterion(roundId, req, actorId))
            .build());
    }

    @DeleteMapping("/{roundId}/criteria/{roundCriterionId}")
    public ResponseEntity<APIResponse<Void>> deleteCriterion(
        HttpServletRequest request,
        @PathVariable Integer roundId,
        @PathVariable Integer roundCriterionId
    ) {
        requireAdminRole(request);
        evaluationRoundCriteriaService.deleteCriterion(roundId, roundCriterionId);
        return ResponseEntity.ok(APIResponse.<Void>builder()
            .code(200)
            .message("delete evaluation criterion success")
            .result(null)
            .build());
    }

    @PutMapping("/{roundId}/criteria/{roundCriterionId}")
    public ResponseEntity<APIResponse<EvaluationRoundCriterionResponse>> updateCriterion(
        HttpServletRequest request,
        @PathVariable Integer roundId,
        @PathVariable Integer roundCriterionId,
        @Valid @RequestBody CreateEvaluationCriterionRequest req
    ) {
        requireAdminRole(request);
        Integer actorId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<EvaluationRoundCriterionResponse>builder()
            .code(200)
            .message("update evaluation criterion success")
            .result(evaluationRoundCriteriaService.updateCriterion(roundId, roundCriterionId, req, actorId))
            .build());
    }

    @GetMapping("/criteria-templates")
    public ResponseEntity<APIResponse<List<EvaluationCriterion>>> listTemplateCriteria() {
        return ResponseEntity.ok(APIResponse.<List<EvaluationCriterion>>builder()
            .code(200)
            .message("get evaluation template criteria success")
            .result(evaluationRoundCriteriaService.listTemplateCriteria())
            .build());
    }

    @PostMapping("/criteria-templates")
    public ResponseEntity<APIResponse<EvaluationCriterion>> addTemplateCriterion(
        HttpServletRequest request,
        @Valid @RequestBody CreateEvaluationCriterionRequest req
    ) {
        requireAdminRole(request);
        Integer actorId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<EvaluationCriterion>builder()
            .code(200)
            .message("create evaluation template criterion success")
            .result(evaluationRoundCriteriaService.addTemplateCriterion(req, actorId))
            .build());
    }

    @PutMapping("/criteria-templates/{templateId}")
    public ResponseEntity<APIResponse<EvaluationCriterion>> updateTemplateCriterion(
        HttpServletRequest request,
        @PathVariable Integer templateId,
        @Valid @RequestBody CreateEvaluationCriterionRequest req
    ) {
        requireAdminRole(request);
        Integer actorId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<EvaluationCriterion>builder()
            .code(200)
            .message("update evaluation template criterion success")
            .result(evaluationRoundCriteriaService.updateTemplateCriterion(templateId, req, actorId))
            .build());
    }

    @DeleteMapping("/criteria-templates/{templateId}")
    public ResponseEntity<APIResponse<Void>> deleteTemplateCriterion(
        HttpServletRequest request,
        @PathVariable Integer templateId
    ) {
        requireAdminRole(request);
        evaluationRoundCriteriaService.deleteTemplateCriterion(templateId);
        return ResponseEntity.ok(APIResponse.<Void>builder()
            .code(200)
            .message("delete evaluation template criterion success")
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
