package com.example.hoccode.repository;

import com.example.hoccode.entity.Streak;
import com.example.hoccode.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StreakRepository extends JpaRepository<Streak, Integer> {
    Optional<Streak> findByUser(User user);
    Boolean existsByUser(User user);

    Streak findFirstByUser(User user);

    @Query("SELECT s FROM Streak s " +
            "WHERE s.updatedAt IS NULL " +
            "   OR s.updatedAt < :todayStart AND s.currentStreak > 0")
    List<Streak> findNotUpdatedToday(@Param("todayStart") LocalDateTime todayStart);

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Streak s
        WHERE s.user.email = :userId
          AND s.updatedAt IS NOT NULL
          AND s.updatedAt >= CURRENT_DATE
          AND s.currentStreak > 0
    """)
    boolean isStreakUpdatedToday(
            @Param("userId") String userId
    );
}
