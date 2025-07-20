package com.example.hoccode.repository;

import com.example.hoccode.entity.Hint;
import com.example.hoccode.entity.Streak;
import com.example.hoccode.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StreakRepository extends JpaRepository<Streak, Integer> {
    Optional<Streak> findByUser(User user);
    Boolean existsByUser(User user);

    Streak findFirstByUser(User user);
}
