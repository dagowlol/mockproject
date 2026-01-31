package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.DelegationRequest;
import com.example.demo.dto.response.APIResponse;
import com.example.demo.dto.response.DelegationResponse;
import com.example.demo.exception.AppException;
import com.example.demo.model.User;
import com.example.demo.repo.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.EvaluationDelegationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/delegations")
@CrossOrigin(origins = "http://localhost:4200")
public class EvaluationDelegationController {

    private final EvaluationDelegationService delegationService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public EvaluationDelegationController(
            EvaluationDelegationService delegationService,
            JwtUtil jwtUtil,
            UserRepository userRepository) {
        this.delegationService = delegationService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<APIResponse<DelegationResponse>> create(
            HttpServletRequest request,
            @Valid @RequestBody DelegationRequest req) {
        Integer actorId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<DelegationResponse>builder()
                .code(200)
                .message("create delegation success")
                .result(delegationService.create(req, actorId))
                .build());
    }

    @GetMapping("/my-requests")
    public ResponseEntity<APIResponse<List<DelegationResponse>>> listMyRequests(
            HttpServletRequest request) {
        Integer actorId = getActorId(request);
        return ResponseEntity.ok(APIResponse.<List<DelegationResponse>>builder()
                .code(200)
                .message("list delegation requests success")
                .result(delegationService.listMyDelegationRequests(actorId))
                .build());
    }

    private Integer getActorId(HttpServletRequest request) {
        String username = extractUsername(request);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "User not found"));
        return user.getId();
    }

    private String extractUsername(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtUtil.extractUsername(token);
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Missing token");
        }
        return authorization.substring(7);
    }
}
