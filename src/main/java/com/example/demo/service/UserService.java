package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ImportUserRow;
import com.example.demo.dto.AssignMemberRequest;
import com.example.demo.model.User;
import com.example.demo.model.Mentorship;
import com.example.demo.repo.MentorshipRepository;
import com.example.demo.repo.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final MentorshipRepository mentorshipRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public UserService(
            UserRepository userRepository,
            MentorshipRepository mentorshipRepository,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }   

    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }
    public List<User> getAllUsers() {
        System.out.println("Fetching all users from the database"+ userRepository.findAll());
        return userRepository.findAll();
    }

    public List<User> searchUsers(String query) {
        String term = safeTrim(query);
        if (term.isEmpty()) {
            return userRepository.findAll();
        }
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                term, term, term);
    }

    public List<User> getLeadersForMentee(Integer menteeId) {
        User mentee = userRepository.findById(menteeId).orElse(null);
        if (mentee == null) {
            return List.of();
        }
        return mentorshipRepository.findByMentee(mentee).stream()
                .map(Mentorship::getLeader)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<User> getMembersForLeader(Integer leaderId) {
        User leader = userRepository.findById(leaderId).orElse(null);
        if (leader == null) {
            return List.of();
        }
        return mentorshipRepository.findByLeader(leader).stream()
                .map(Mentorship::getMentee)
                .collect(java.util.stream.Collectors.toList());
    }

    public Mentorship addLeaderForMentee(Integer menteeId, Integer leaderId) {
        if (menteeId.equals(leaderId)) {
            return null;
        }
        User mentee = userRepository.findById(menteeId).orElse(null);
        User leader = userRepository.findById(leaderId).orElse(null);
        if (mentee == null || leader == null) {
            return null;
        }
        if (mentorshipRepository.findByLeaderAndMentee(leader, mentee).isPresent()) {
            return null;
        }
        Mentorship mentorship = new Mentorship(leader, mentee);
        return mentorshipRepository.save(mentorship);
    }

    public int updateMembersForLeader(Integer leaderId, AssignMemberRequest request) {
        if (request == null || request.getMemberIds() == null || request.getMemberIds().isEmpty()) {
            return 0;
        }
        User leader = userRepository.findById(leaderId).orElse(null);
        if (leader == null) {
            return 0;
        }

        String mode = request.getMode() == null ? "ADD" : request.getMode().toUpperCase();
        if ("REPLACE".equals(mode)) {
            List<Mentorship> existing = mentorshipRepository.findByLeader(leader);
            mentorshipRepository.deleteAll(existing);
        }

        int changed = 0;
        for (Integer menteeId : request.getMemberIds()) {
            if (menteeId == null) {
                continue;
            }
            if ("REMOVE".equals(mode)) {
                if (removeLeaderForMentee(menteeId, leaderId)) {
                    changed++;
                }
                continue;
            }
            Mentorship created = addLeaderForMentee(menteeId, leaderId);
            if (created != null) {
                changed++;
            }
        }
        return changed;
    }

    public boolean removeLeaderForMentee(Integer menteeId, Integer leaderId) {
        User mentee = userRepository.findById(menteeId).orElse(null);
        User leader = userRepository.findById(leaderId).orElse(null);
        if (mentee == null || leader == null) {
            return false;
        }
        return mentorshipRepository.findByLeaderAndMentee(leader, mentee)
                .map(existing -> {
                    mentorshipRepository.delete(existing);
                    return true;
                })
                .orElse(false);
    }

    public User updateUser(Integer id, User updates) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        if (updates.getUsername() != null) {
            existing.setUsername(updates.getUsername());
        }
        if (updates.getFullName() != null) {
            existing.setFullName(updates.getFullName());
        }
        if (updates.getEmail() != null) {
            existing.setEmail(updates.getEmail());
        }
        if (updates.getRole() != null) {
            existing.setRole(updates.getRole());
        }
        if (updates.getRoleName() != null) {
            existing.setRoleName(updates.getRoleName());
        }
        if (updates.getPositionTitle() != null) {
            existing.setPositionTitle(updates.getPositionTitle());
        }
        if (updates.getDescription() != null) {
            existing.setDescription(updates.getDescription());
        }
        if (updates.getStatus() != null) {
            existing.setStatus(updates.getStatus());
        }
        if (updates.getLastLoginAt() != null) {
            existing.setLastLoginAt(updates.getLastLoginAt());
        }
        if (updates.getLeader() != null) {
            existing.setLeader(updates.getLeader());
        }
        if (updates.getSkills() == null) {
            existing.setSkills(null);
        } else {
            existing.setSkills(normalizeSkills(updates.getSkills()));
        }
        return userRepository.save(existing);
    }

    public List<ImportUserRow> previewImport(MultipartFile file) {
        List<ImportUserRow> results = new ArrayList<>();
        Set<String> seenUsernames = new HashSet<>();

        try (InputStream inputStream = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(0);
            HeaderMap headerMap = new HeaderMap(headerRow);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                if (isRowEmpty(row)) {
                    continue;
                }

                ImportUserRow dto = new ImportUserRow();
                String username = headerMap.getCellValue(row, "username", formatter);
                String email = headerMap.getCellValue(row, "email", formatter);
                String password = headerMap.getCellValue(row, "password", formatter);
                String role = headerMap.getCellValue(row, "role", formatter);
                String skills = headerMap.getCellValue(row, "skills", formatter);
                logger.info("Import preview row {} skills raw: [{}]", row.getRowNum() + 1, skills);

                if (role.isEmpty()) {
                    role = "USER";
                }

                List<String> errors = new ArrayList<>();
                if (username.isEmpty()) {
                    errors.add("username required");
                } else if (seenUsernames.contains(username)) {
                    errors.add("duplicate username in file");
                } else if (userRepository.findByUsername(username).isPresent()) {
                    errors.add("username already exists");
                }

                if (email.isEmpty()) {
                    errors.add("email required");
                }
                if (password.isEmpty()) {
                    errors.add("password required");
                }
                if (!skills.isEmpty() && !isValidSkillsFormat(skills)) {
                    errors.add("skills must be JSON array or comma-separated, e.g. [\"Java\", \"Spring\"] or Java, Spring");
                }

                if (!username.isEmpty()) {
                    seenUsernames.add(username);
                }

                dto.setUsername(username);
                dto.setEmail(email);
                dto.setPassword(password);
                dto.setRole(role);
                dto.setSkills(skills);
                dto.setValid(errors.isEmpty());
                dto.setMessage(String.join(", ", errors));
                results.add(dto);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }

        return results;
    }

    public int saveImported(List<ImportUserRow> rows) {
        int saved = 0;
        for (ImportUserRow row : rows) {
            if (!row.isValid()) {
                continue;
            }
            String username = safeTrim(row.getUsername());
            String email = safeTrim(row.getEmail());
            String password = safeTrim(row.getPassword());
            String role = safeTrim(row.getRole());
            String skills = safeTrim(row.getSkills());
            logger.info("Import confirm skills raw: [{}] for username: {}", skills, username);

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                continue;
            }
            if (userRepository.findByUsername(username).isPresent()) {
                continue;
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role.isEmpty() ? "USER" : role);
            if (!skills.isEmpty()) {
                user.setSkills(parseSkills(skills));
            }
            userRepository.save(user);
            saved++;
        }
        return saved;
    }

    private boolean isRowEmpty(Row row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isValidSkillsFormat(String value) {
        return isValidJsonArray(value) || isValidCsvSkills(value);
    }

    private boolean isValidJsonArray(String value) {
        try {
            JsonNode node = objectMapper.readTree(value);
            return node != null && node.isArray();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidCsvSkills(String value) {
        String[] parts = value.split(",");
        for (String part : parts) {
            if (part.trim().isEmpty()) {
                return false;
            }
        }
        return parts.length > 0;
    }

    private JsonNode parseSkills(String value) {
        if (isValidJsonArray(value)) {
            try {
                return objectMapper.readTree(value);
            } catch (Exception e) {
                throw new RuntimeException("Invalid skills JSON", e);
            }
        }
        return parseCsvSkills(value);
    }

    private JsonNode parseCsvSkills(String value) {
        List<String> list = new ArrayList<>();
        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return objectMapper.valueToTree(list);
    }

    private JsonNode normalizeSkills(JsonNode incoming) {
        if (incoming == null || incoming.isNull()) {
            return null;
        }
        if (incoming.isTextual()) {
            String text = safeTrim(incoming.asText());
            if (text.isEmpty()) {
                return null;
            }
            if (isValidSkillsFormat(text)) {
                return parseSkills(text);
            }
            return objectMapper.valueToTree(text);
        }
        return incoming;
    }

    public byte[] exportUsersToExcel() {
        List<User> users = userRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");
            int rowIndex = 0;

            Row header = sheet.createRow(rowIndex++);
            header.createCell(0).setCellValue("id");
            header.createCell(1).setCellValue("username");
            header.createCell(2).setCellValue("email");
            header.createCell(3).setCellValue("role");
            header.createCell(4).setCellValue("skills");

            for (User user : users) {
                Row row = sheet.createRow(rowIndex++);
                if (user.getId() == null) {
                    row.createCell(0).setCellValue("");
                } else {
                    row.createCell(0).setCellValue(user.getId());
                }
                row.createCell(1).setCellValue(safeTrim(user.getUsername()));
                row.createCell(2).setCellValue(safeTrim(user.getEmail()));
                row.createCell(3).setCellValue(safeTrim(user.getRole()));
                row.createCell(4).setCellValue(formatSkills(user.getSkills()));
            }

            for (int i = 0; i <= 4; i++) {
                sheet.autoSizeColumn(i);
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                workbook.write(out);
                return out.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to export users to Excel", e);
        }
    }

    private String formatSkills(JsonNode skills) {
        if (skills == null) {
            return "";
        }
        if (skills.isArray()) {
            List<String> list = new ArrayList<>();
            skills.forEach(n -> list.add(n.asText()));
            return String.join(", ", list);
        }
        if (skills.isObject() && skills.has("skills") && skills.get("skills").isArray()) {
            List<String> list = new ArrayList<>();
            skills.get("skills").forEach(n -> list.add(n.asText()));
            return String.join(", ", list);
        }
        if (skills.isTextual()) {
            String text = skills.asText();
            if (isValidJsonArray(text)) {
                try {
                    JsonNode parsed = objectMapper.readTree(text);
                    List<String> list = new ArrayList<>();
                    parsed.forEach(n -> list.add(n.asText()));
                    return String.join(", ", list);
                } catch (Exception e) {
                    // fall through to return text
                }
            }
            return text;
        }
        try {
            return objectMapper.writeValueAsString(skills);
        } catch (JsonProcessingException e) {
            return skills.toString();
        }
    }

    private static final class HeaderMap {
        private final java.util.Map<String, Integer> headers = new java.util.HashMap<>();

        private HeaderMap(Row headerRow) {
            if (headerRow == null) {
                return;
            }
            for (int i = headerRow.getFirstCellNum(); i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell == null) {
                    continue;
                }
                String name = cell.toString().trim().toLowerCase();
                if (!name.isEmpty()) {
                    headers.putIfAbsent(name, i);
                }
            }
        }

        private String getCellValue(Row row, String headerName, DataFormatter formatter) {
            Integer index = headers.get(headerName.toLowerCase());
            if (index == null) {
                return "";
            }
            Cell cell = row.getCell(index);
            if (cell == null) {
                return "";
            }
            return formatter.formatCellValue(cell).trim();
        }
    }
    
}
