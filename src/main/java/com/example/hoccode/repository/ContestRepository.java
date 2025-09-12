package com.example.hoccode.repository;

import com.example.hoccode.entity.Contest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Integer> {
    Contest findFirstById(Integer id);

    @Query("""
    SELECT c FROM Contest c
    WHERE c.deleted = false
    ORDER BY
      CASE
        WHEN c.startTime <= CURRENT_TIMESTAMP AND c.endTime >= CURRENT_TIMESTAMP THEN 0
        WHEN c.startTime <= CURRENT_TIMESTAMP AND FUNCTION('DATEDIFF', CURRENT_TIMESTAMP, c.startTime) <= 2 THEN 1
        WHEN c.startTime > CURRENT_TIMESTAMP THEN 2
        ELSE 3
      END,
      c.startTime ASC
""")
    List<Contest> findAllSortedByTimePriority();

    List<Contest> findByStartTimeBetweenAndDeletedFalse(LocalDateTime start, LocalDateTime end);

    Optional<Contest> findFirstByIdAndDeletedFalse(Integer id);

    List<Contest> findByCreatedByOrderByCreatedAtDesc(String createdBy);
}