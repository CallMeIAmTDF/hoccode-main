package com.example.hoccode.repository;

import com.example.hoccode.dto.projection.IContestLeaderboardProjection;
import com.example.hoccode.dto.projection.IContestProblemStatusProjection;
import com.example.hoccode.entity.Submission;
import com.example.hoccode.util.constant.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.Set;
import java.util.List;


public interface SubmissionRepository extends JpaRepository<Submission, String> {
    @Query("SELECT DISTINCT s.pid FROM Submission s WHERE s.status = 'ACCEPTED' AND s.createdBy = :email")
    Set<Integer> findAcceptedProblemIdsByUser(@Param("email") String email);

    List<Submission> findAllByCreatedBy(String email);

    Boolean existsByCreatedByAndPidAndStatus(String email, Integer pid, StatusEnum statusEnum);

    @Query("""
        SELECT DISTINCT s.createdBy as username,
            SUM(CASE WHEN s.status = 'ACCEPTED' AND
                NOT EXISTS (
                    SELECT 1 FROM Submission s2
                    WHERE s2.createdBy = s.createdBy
                    AND s2.pid = s.pid
                    AND s2.status = 'ACCEPTED'
                    AND s2.createdAt < s.createdAt
                ) THEN p.point ELSE 0 END) as totalPoints
        FROM Submission s
        JOIN Problem p ON s.pid = p.id
        JOIN ContestProblem cp ON p.id = cp.problem.id
        WHERE cp.contest.id = :contestId
        GROUP BY s.createdBy
        ORDER BY totalPoints DESC
        """)
    Page<IContestLeaderboardProjection> findContestLeaderboard(
            @Param("contestId") Integer contestId,
            Pageable pageable
    );

    @Query("""
        SELECT cp.problem.title as problemName,
            MAX(CASE WHEN s.status = 'ACCEPTED' THEN 'ACCEPTED'
                WHEN s.id IS NOT NULL THEN 'UNACCEPTED'
                ELSE 'NOT_SUBMITTED' END) as status,
            COUNT(CASE WHEN s.status = 'ACCEPTED' THEN 1 END) as acceptedCount,
            COUNT(CASE WHEN s.status != 'ACCEPTED' AND s.id IS NOT NULL THEN 1 END) as wrongCount
        FROM ContestProblem cp
        LEFT JOIN cp.problem p
        LEFT JOIN Submission s ON s.pid = p.id AND s.createdBy = :email
        WHERE cp.contest.id = :contestId
        GROUP BY cp.problem.title, p.id
        ORDER BY p.id
        """)
    List<IContestProblemStatusProjection> findUserContestProblemStatuses(
            @Param("contestId") Integer contestId,
            @Param("email") String email
    );

    @Query("SELECT s.pid, " +
            "SUM(CASE WHEN s.status = 'ACCEPTED' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN s.status <> 'ACCEPTED' THEN 1 ELSE 0 END) " +
            "FROM Submission s WHERE s.pid IN :problemIds GROUP BY s.pid")
    List<Object[]> countSubmissionByProblemIds(@Param("problemIds") List<Integer> problemIds);
}
