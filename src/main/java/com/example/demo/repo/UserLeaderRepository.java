package com.example.demo.repo;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.UserLeader;

@Repository
public interface UserLeaderRepository extends JpaRepository<UserLeader, Integer> {
    @Query("""
        select ul from UserLeader ul
        where ul.userId = ?1
          and ul.isActive = true
          and (ul.effectiveFrom is null or ul.effectiveFrom <= ?2)
          and (ul.effectiveTo is null or ul.effectiveTo >= ?2)
        """)
    Optional<UserLeader> findActiveLeader(Integer userId, LocalDate today);

    @Query("""
        select ul from UserLeader ul
        where ul.userId = ?1
          and ul.isActive = true
          and (ul.effectiveFrom is null or ul.effectiveFrom <= ?2)
          and (ul.effectiveTo is null or ul.effectiveTo >= ?2)
        """)
    java.util.List<UserLeader> findActiveLeaders(Integer userId, LocalDate today);
}
