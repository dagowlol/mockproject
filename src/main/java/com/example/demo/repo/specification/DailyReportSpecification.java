package com.example.demo.repo.specification;

import com.example.demo.model.*;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DailyReportSpecification {

    public static Specification<DailyReport> hasUserId(Integer userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<DailyReport> hasProjectId(Integer projectId) {
        return (root, query, cb) -> projectId == null ? null : cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<DailyReport> hasStatus(DailyReportStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<DailyReport> reportDateBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null)
                return null;
            if (from != null && to != null)
                return cb.between(root.get("reportDate"), from, to);
            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("reportDate"), from);
            return cb.lessThanOrEqualTo(root.get("reportDate"), to);
        };
    }

    public static Specification<DailyReport> submittedAtBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null)
                return null;
            if (from != null && to != null)
                return cb.between(root.get("submittedAt"), from, to);
            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("submittedAt"), from);
            return cb.lessThanOrEqualTo(root.get("submittedAt"), to);
        };
    }

    public static Specification<DailyReport> updatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null)
                return null;
            if (from != null && to != null)
                return cb.between(root.get("updatedAt"), from, to);
            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("updatedAt"), from);
            return cb.lessThanOrEqualTo(root.get("updatedAt"), to);
        };
    }

    public static Specification<DailyReport> userInGroup(Integer groupId) {
        return (root, query, cb) -> {
            if (groupId == null)
                return null;
            // Assuming DailyReport -> User -> ProjectGroupMember -> ProjectGroup
            Join<DailyReport, User> userJoin = root.join("user");
            Join<User, ProjectGroupMember> groupMemberJoin = userJoin.join("projectGroupMembers"); // Assuming
                                                                                                   // 'projectGroupMembers'
                                                                                                   // is a collection in
                                                                                                   // User
            Join<ProjectGroupMember, ProjectGroup> projectGroupJoin = groupMemberJoin.join("projectGroup"); // Assuming
                                                                                                            // 'projectGroup'
                                                                                                            // is a
                                                                                                            // field in
                                                                                                            // ProjectGroupMember
            return cb.equal(projectGroupJoin.get("id"), groupId);
        };
    }

    public static Specification<DailyReport> userManagedByLeader(Integer leaderUserId) {
        return (root, query, cb) -> {
            if (leaderUserId == null)
                return null;
            // Assuming DailyReport -> User (managed user) -> UserLeader (where UserLeader
            // has a 'leader' field)
            Join<DailyReport, User> userJoin = root.join("user");
            Join<User, UserLeader> userLeaderJoin = userJoin.join("managedByLeaders"); // Assuming 'managedByLeaders' is
                                                                                       // a collection in User for users
                                                                                       // they manage
            Join<UserLeader, User> leaderJoin = userLeaderJoin.join("leader"); // Assuming 'leader' is a field in
                                                                               // UserLeader pointing to the leader User
            return cb.equal(leaderJoin.get("id"), leaderUserId);
        };
    }

    public static Specification<DailyReport> hasReportIdsIn(java.util.List<Integer> reportIds) {
        return (root, query, cb) -> {
            if (reportIds == null || reportIds.isEmpty())
                return null;
            return root.get("reportId").in(reportIds);
        };
    }
}
