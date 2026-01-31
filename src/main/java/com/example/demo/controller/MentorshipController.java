package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AssignMemberRequest;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import java.util.List;

@RestController
@RequestMapping("/api/leaders")
public class MentorshipController {
    @Autowired
    private UserService userService;

    @PutMapping("/{leaderId}/members")
    public ResponseEntity<Integer> assignMembers(@PathVariable Integer leaderId, @RequestBody AssignMemberRequest request) {
        int changed = userService.updateMembersForLeader(leaderId, request);
        return ResponseEntity.ok(changed);
    }

    @GetMapping("/{leaderId}/members")
    public ResponseEntity<List<User>> getMembers(@PathVariable Integer leaderId) {
        return ResponseEntity.ok(userService.getMembersForLeader(leaderId));
    }
}
