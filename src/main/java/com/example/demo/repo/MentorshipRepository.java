package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Mentorship;
import com.example.demo.model.User;

@Repository
public interface MentorshipRepository extends JpaRepository<Mentorship, Integer> {
    Optional<Mentorship> findByLeaderAndMentee(User leader, User mentee);
    List<Mentorship> findByMentee(User mentee);
    List<Mentorship> findByLeader(User leader);
}
