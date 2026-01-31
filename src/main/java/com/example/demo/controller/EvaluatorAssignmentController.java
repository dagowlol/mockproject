package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.dto.response.APIResponse;
import com.example.demo.dto.response.EvaluatorAssignmentRowResponse;
import com.example.demo.dto.response.EvaluatorAssignmentSummaryResponse;
import com.example.demo.dto.response.EvaluationFormResponse;
import com.example.demo.dto.request.EvaluationFormRequest;
import com.example.demo.exception.AppException;
import com.example.demo.model.User;
import com.example.demo.repo.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.EvaluatorAssignmentService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
@RequestMapping("/api/evaluation-assignments")
@CrossOrigin(origins = "http://localhost:4200")
public class EvaluatorAssignmentController {

    private final EvaluatorAssignmentService evaluatorAssignmentService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public EvaluatorAssignmentController(
        EvaluatorAssignmentService evaluatorAssignmentService,
        JwtUtil jwtUtil,
        UserRepository userRepository
    ) {
        this.evaluatorAssignmentService = evaluatorAssignmentService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<APIResponse<EvaluatorAssignmentSummaryResponse>> summary(HttpServletRequest request) {
        Integer userId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<EvaluatorAssignmentSummaryResponse>builder()
            .code(200)
            .message("get evaluation assignment summary success")
            .result(evaluatorAssignmentService.getSummary(userId))
            .build());
    }

    @GetMapping
    public ResponseEntity<APIResponse<List<EvaluatorAssignmentRowResponse>>> list(
        HttpServletRequest request,
        @RequestParam(required = false) String status
    ) {
        log.info("status: {}", status);
        Integer userId = getActorId(request);
        log.info("userId: {}", userId);
        return ResponseEntity.ok(APIResponse.<List<EvaluatorAssignmentRowResponse>>builder()
            .code(200)
            .message("get evaluation assignments success")
            .result(evaluatorAssignmentService.listAssignments(userId, status))
            .build());
    }

    @GetMapping("/{assignmentId}/form")
    public ResponseEntity<APIResponse<EvaluationFormResponse>> getForm(
        HttpServletRequest request,
        @PathVariable Integer assignmentId
    ) {
        Integer userId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<EvaluationFormResponse>builder()
            .code(200)
            .message("get evaluation form success")
            .result(evaluatorAssignmentService.getEvaluationForm(assignmentId, userId))
            .build());
    }

    @PostMapping("/{assignmentId}/form")
    public ResponseEntity<APIResponse<EvaluationFormResponse>> saveForm(
        HttpServletRequest request,
        @PathVariable Integer assignmentId,
        @RequestBody EvaluationFormRequest req
    ) {
        Integer userId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<EvaluationFormResponse>builder()
            .code(200)
            .message("save evaluation form success")
            .result(evaluatorAssignmentService.saveEvaluationForm(
                assignmentId,
                userId,
                req.getScores(),
                req.getComment(),
                false
            ))
            .build());
    }

    @PostMapping("/{assignmentId}/submit")
    public ResponseEntity<APIResponse<EvaluationFormResponse>> submitForm(
        HttpServletRequest request,
        @PathVariable Integer assignmentId,
        @RequestBody EvaluationFormRequest req
    ) {
        Integer userId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<EvaluationFormResponse>builder()
            .code(200)
            .message("submit evaluation form success")
            .result(evaluatorAssignmentService.saveEvaluationForm(
                assignmentId,
                userId,
                req.getScores(),
                req.getComment(),
                true
            ))
            .build());
    }

    private Integer getActorId(HttpServletRequest request) {
        String username = extractUsername(request);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new AppException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));
        return user.getId();
    }

    private String extractUsername(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractUsername(token);
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AppException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing token");
        }
        return authorization.substring(7);
    }
}
