package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.example.demo.dto.ImportUserRow;
import com.example.demo.model.User;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam(value = "q", required = false) String query) {
        return userService.searchUsers(query);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User updates) {
        User updated = userService.updateUser(id, updates);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUserPatch(@PathVariable Integer id, @RequestBody User updates) {
        User updated = userService.updateUser(id, updates);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/import/preview")
    public List<ImportUserRow> previewImport(@RequestParam("file") MultipartFile file) {
        return userService.previewImport(file);
    }

    @PostMapping("/import/confirm")
    public ResponseEntity<Map<String, Object>> confirmImport(@RequestBody List<ImportUserRow> rows) {
        int saved = userService.saveImported(rows);
        Map<String, Object> response = new HashMap<>();
        response.put("saved", saved);
        response.put("requested", rows.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportUsers() {
        byte[] data = userService.exportUsersToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "users.xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
