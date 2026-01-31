package com.example.demo.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.CreateUserLeaderRequest;
import com.example.demo.dto.response.APIResponse;
import com.example.demo.exception.AppException;
import com.example.demo.model.User;
import com.example.demo.model.UserLeader;
import com.example.demo.repo.UserLeaderRepository;
import com.example.demo.repo.UserRepository;
import com.example.demo.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user-leaders")
@CrossOrigin(origins = "http://localhost:4200")
public class UserLeaderController {

    private final UserLeaderRepository userLeaderRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserLeaderController(
        UserLeaderRepository userLeaderRepository,
        UserRepository userRepository,
        JwtUtil jwtUtil
    ) {
        this.userLeaderRepository = userLeaderRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<APIResponse<UserLeader>> createUserLeader(
        HttpServletRequest request,
        @Valid @RequestBody CreateUserLeaderRequest req
    ) {
        requireAdminRole(request);
        Integer actorId = getActorId(request);

        if (req.getUserId().equals(req.getLeaderId())) {
            throw new AppException(org.springframework.http.HttpStatus.BAD_REQUEST, "User cannot be their own leader");
        }
        User user = userRepository.findById(req.getUserId())
            .orElseThrow(() -> new AppException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        User leader = userRepository.findById(req.getLeaderId())
            .orElseThrow(() -> new AppException(org.springframework.http.HttpStatus.NOT_FOUND, "Leader not found"));

        LocalDate today = LocalDate.now();
        userLeaderRepository.findActiveLeader(user.getId(), today).ifPresent(existing -> {
            existing.setIsActive(false);
            userLeaderRepository.save(existing);
        });

        UserLeader mapping = new UserLeader();
        mapping.setUserId(user.getId());
        mapping.setLeaderId(leader.getId());
        mapping.setIsActive(req.getIsActive() != null ? req.getIsActive() : Boolean.TRUE);
        mapping.setEffectiveFrom(req.getEffectiveFrom());
        mapping.setEffectiveTo(req.getEffectiveTo());
        mapping.setCreatedBy(actorId);

        return ResponseEntity.ok(APIResponse.<UserLeader>builder()
            .code(200)
            .message("create user leader success")
            .result(userLeaderRepository.save(mapping))
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
