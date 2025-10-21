package com.example.hoccode.repository;

import com.example.hoccode.entity.Contest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = """
        SELECT 
            p.id AS id,
            p.title AS title,
            p.difficulty AS difficulty,
            COUNT(s.id) AS submissionCount,
            COUNT(DISTINCT CASE WHEN s.status = 'ACCEPTED' THEN s.created_by END) AS solvedCount,
            ROUND(
                (COUNT(DISTINCT CASE WHEN s.status = 'ACCEPTED' THEN s.created_by END) * 100.0) 
                / NULLIF(COUNT(s.id), 0), 1
            ) AS acceptanceRate,
            COALESCE(
                (COUNT(DISTINCT CASE WHEN s.status = 'ACCEPTED' THEN s.created_by END) * p.point * 1.0) 
                / NULLIF((
                    SELECT COUNT(*) FROM contest_participant cp2 WHERE cp2.contest_id = cp.contest_id
                ), 0), 0
            ) AS averageScore
        FROM contest_problems cp
        JOIN problems p ON cp.problem_id = p.id
        LEFT JOIN submissions s ON s.pid = p.id
        WHERE cp.contest_id = :contestId
        GROUP BY p.id, p.title, p.difficulty, p.point, cp.contest_id
    """, nativeQuery = true)
    List<Object[]> findProblemStatsByContestId(@Param("contestId") Integer contestId);
}