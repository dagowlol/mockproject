package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.AddTargetsRequest;
import com.example.demo.dto.request.CreateEvaluationRoundRequest;
import com.example.demo.dto.request.ManualAssignmentRequest;
import com.example.demo.dto.response.APIResponse;
import com.example.demo.dto.response.AutoAssignResultResponse;
import com.example.demo.dto.response.EvaluationRoundDetailResponse;
import com.example.demo.dto.response.EvaluationRoundProgressResponse;
import com.example.demo.dto.response.EvaluationRoundResponse;
import com.example.demo.dto.response.EvaluationRoundSummaryResponse;
import com.example.demo.dto.response.ManualAssignmentRowResponse;
import com.example.demo.dto.response.MemberEvaluationSummaryResponse;
import com.example.demo.dto.response.TargetCandidateResponse;
import com.example.demo.dto.response.TargetGenerationResponse;
import com.example.demo.exception.AppException;
import com.example.demo.model.User;
import com.example.demo.repo.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.EvaluationRoundService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/evaluation-rounds")
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class EvaluationRoundController {

    private final EvaluationRoundService evaluationRoundService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public EvaluationRoundController(
            EvaluationRoundService evaluationRoundService,
            JwtUtil jwtUtil,
            UserRepository userRepository) {
        this.evaluationRoundService = evaluationRoundService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<APIResponse<List<EvaluationRoundResponse>>> list() {
        return ResponseEntity.ok(APIResponse.<List<EvaluationRoundResponse>>builder()
                .code(200)
                .message("get evaluation rounds success")
                .result(evaluationRoundService.list())
                .build());
    }

    @GetMapping("/my-summary")
    public ResponseEntity<APIResponse<MemberEvaluationSummaryResponse>> getMySummary(HttpServletRequest request) {
        Integer actorId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<MemberEvaluationSummaryResponse>builder()
                .code(200)
                .message("get my evaluation summary success")
                .result(evaluationRoundService.getMemberEvaluationSummary(actorId))
                .build());
    }

    @GetMapping("/{roundId}")
    public ResponseEntity<APIResponse<EvaluationRoundDetailResponse>> detail(
            HttpServletRequest request,
            @PathVariable Integer roundId) {
        requireAdminRole(request);
        return ResponseEntity.ok(APIResponse.<EvaluationRoundDetailResponse>builder()
                .code(200)
                .message("get evaluation round detail success")
                .result(evaluationRoundService.getDetail(roundId))
                .build());
    }

    @PostMapping
    public ResponseEntity<APIResponse<EvaluationRoundResponse>> create(
            HttpServletRequest request,
            @Valid @RequestBody CreateEvaluationRoundRequest req){
        requireAdminRole(request);
        Integer actorId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<EvaluationRoundResponse>builder()
                .code(200)
                .message("create evaluation round success")
                .result(evaluationRoundService.create(req, actorId))
                .build());
    }

    @PostMapping("/{roundId}/auto-assign-leader")
    public ResponseEntity<APIResponse<AutoAssignResultResponse>> autoAssignLeader(
            HttpServletRequest request,
            @PathVariable Integer roundId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "true") boolean notify) {
        requireAdminRole(request);
        Integer actorId = getActorId(request);
        AutoAssignResultResponse result = evaluationRoundService.autoAssignLeader(roundId, actorId, notify);
        return ResponseEntity.ok(APIResponse.<AutoAssignResultResponse>builder()
                .code(200)
                .message("auto assign evaluators success")
                .result(result)
                .build());
    }

    @GetMapping("/{roundId}/manual-assignments")
    public ResponseEntity<APIResponse<List<ManualAssignmentRowResponse>>> listManualAssignments(
            HttpServletRequest request,
            @PathVariable Integer roundId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q) {
        requireAdminRole(request);
        return ResponseEntity.ok(APIResponse.<List<ManualAssignmentRowResponse>>builder()
                .code(200)
                .message("get manual assignments success")
                .result(evaluationRoundService.listManualAssignments(roundId, q))
                .build());
    }

    @GetMapping("/{roundId}/progress")
    public ResponseEntity<APIResponse<EvaluationRoundProgressResponse>> progress(
            HttpServletRequest request,
            @PathVariable Integer roundId) {
        requireAdminRole(request);
        return ResponseEntity.ok(APIResponse.<EvaluationRoundProgressResponse>builder()
                .code(200)
                .message("get evaluation round progress success")
                .result(evaluationRoundService.getProgress(roundId))
                .build());
    }

    @GetMapping("/{roundId}/summary")
    public ResponseEntity<APIResponse<EvaluationRoundSummaryResponse>> summary(
            HttpServletRequest request,
            @PathVariable Integer roundId) {
        requireAdminRole(request);
        return ResponseEntity.ok(APIResponse.<EvaluationRoundSummaryResponse>builder()
                .code(200)
                .message("get evaluation round summary success")
                .result(evaluationRoundService.getSummary(roundId))
                .build());
    }

    @GetMapping("/evaluators")
    public ResponseEntity<APIResponse<List<User>>> listEvaluators(
            HttpServletRequest request,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q) {
        requireAdminRole(request);
        return ResponseEntity.ok(APIResponse.<List<User>>builder()
                .code(200)
                .message("get evaluators success")
                .result(evaluationRoundService.listEvaluators(q))
                .build());
    }

    @PostMapping("/{roundId}/manual-assignments")
    public ResponseEntity<APIResponse<AutoAssignResultResponse>> manualAssign(
            HttpServletRequest request,
            @PathVariable Integer roundId,
            @Valid @RequestBody ManualAssignmentRequest req) {
        requireAdminRole(request);
        Integer actorId = getActorId(request);
        AutoAssignResultResponse result = evaluationRoundService.manualAssign(roundId, req, actorId);
        return ResponseEntity.ok(APIResponse.<AutoAssignResultResponse>builder()
                .code(200)
                .message("save manual assignments success")
                .result(result)
                .build());
    }

    @PostMapping("/{roundId}/publish")
    public ResponseEntity<APIResponse<EvaluationRoundResponse>> publish(
            HttpServletRequest request,
            @PathVariable Integer roundId) {
        requireAdminRole(request);
        Integer actorId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<EvaluationRoundResponse>builder()
                .code(200)
                .message("publish evaluation round success")
                .result(evaluationRoundService.publish(roundId, actorId))
                .build());
    }

    @GetMapping("/{roundId}/target-candidates")
    public ResponseEntity<APIResponse<List<TargetCandidateResponse>>> listTargetCandidates(
            HttpServletRequest request,
            @PathVariable Integer roundId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String sort) {
        requireAdminRole(request);
        return ResponseEntity.ok(APIResponse.<List<TargetCandidateResponse>>builder()
                .code(200)
                .message("get target candidates success")
                .result(evaluationRoundService.listTargetCandidates(roundId, q, sort))
                .build());
    }

    @PostMapping("/{roundId}/targets")
    public ResponseEntity<APIResponse<TargetGenerationResponse>> addTargets(
            HttpServletRequest request,
            @PathVariable Integer roundId,
            @Valid @RequestBody AddTargetsRequest req) {
        requireAdminRole(request);
        TargetGenerationResponse result = evaluationRoundService.addTargets(roundId, req);
        return ResponseEntity.ok(APIResponse.<TargetGenerationResponse>builder()
                .code(200)
                .message("add targets success")
                .result(result)
                .build());
    }

    private Integer getActorId(HttpServletRequest request) {
        String username = extractUsername(request);
        User user = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new AppException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));
        return user.getId();
    }

    private void requireAdminRole(HttpServletRequest request) {
        String role = extractRole(request);
        log.info("Role: {}", role);
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
