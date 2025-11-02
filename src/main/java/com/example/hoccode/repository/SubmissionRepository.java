package com.example.hoccode.repository;

import com.example.hoccode.dto.projection.IContestLeaderboardProjection;
import com.example.hoccode.dto.projection.IContestProblemStatusProjection;
import com.example.hoccode.dto.projection.ProblemSubmissionCount;

import com.example.hoccode.dto.projection.SubmissionWithProblemTitleProjection;
import com.example.hoccode.dto.response.SubmissionCategoryCount;
import com.example.hoccode.dto.response.SubmissionProblemResponse;
import com.example.hoccode.entity.Submission;
import com.example.hoccode.util.constant.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.List;


public interface SubmissionRepository extends JpaRepository<Submission, String> {
    @Query("SELECT DISTINCT s.pid FROM Submission s WHERE s.status = 'ACCEPTED' AND s.createdBy = :email")
    Set<Integer> findAcceptedProblemIdsByUser(@Param("email") String email);

    List<Submission> findAllByCreatedBy(String email);

    Boolean existsByCreatedByAndPidAndStatus(String email, Integer pid, StatusEnum statusEnum);

    @Query(value = """
    SELECT s1.created_by as username,
           SUM(p.point) as totalPoints
    FROM submissions s1
    JOIN problems p ON s1.pid = p.id
    JOIN contest_problems cp ON p.id = cp.problem_id AND cp.contest_id = :contestId
    WHERE s1.status = 'ACCEPTED'
      AND NOT EXISTS (
          SELECT 1
          FROM submissions s2
          JOIN contest_problems cp2 ON s2.pid = cp2.problem_id AND cp2.contest_id = :contestId
          WHERE s2.created_by = s1.created_by
            AND s2.pid = s1.pid
            AND s2.status = 'ACCEPTED'
            AND s2.created_at < s1.created_at
      )
    GROUP BY s1.created_by
    ORDER BY totalPoints DESC
    """,
            countQuery = """
    SELECT COUNT(DISTINCT s1.created_by)
    FROM submissions s1
    JOIN problems p ON s1.pid = p.id
    JOIN contest_problems cp ON p.id = cp.problem_id AND cp.contest_id = :contestId
    WHERE s1.status = 'ACCEPTED'
      AND NOT EXISTS (
          SELECT 1
          FROM submissions s2
          JOIN contest_problems cp2 ON s2.pid = cp2.problem_id AND cp2.contest_id = :contestId
          WHERE s2.created_by = s1.created_by
            AND s2.pid = s1.pid
            AND s2.status = 'ACCEPTED'
            AND s2.created_at < s1.created_at
      )
    """,
            nativeQuery = true)
    Page<IContestLeaderboardProjection> findContestLeaderboard(
            @Param("contestId") Integer contestId,
            Pageable pageable
    );

    @Query(value = """
    SELECT s1.created_by as username,
           SUM(p.point) as totalPoints
    FROM submissions s1
    JOIN problems p ON s1.pid = p.id
    JOIN contest_problems cp ON p.id = cp.problem_id AND cp.contest_id = :contestId
    WHERE s1.status = 'ACCEPTED'
      AND NOT EXISTS (
          SELECT 1
          FROM submissions s2
          JOIN contest_problems cp2 ON s2.pid = cp2.problem_id AND cp2.contest_id = :contestId
          WHERE s2.created_by = s1.created_by
            AND s2.pid = s1.pid
            AND s2.status = 'ACCEPTED'
            AND s2.created_at < s1.created_at
      )
    GROUP BY s1.created_by
    ORDER BY totalPoints DESC
    """,
            nativeQuery = true)
    List<IContestLeaderboardProjection> findContestLeaderboardNoPage(
            @Param("contestId") Integer contestId
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


    @Query(value = """
        SELECT DATE(s.created_at) as day, COUNT(*) as total
        FROM submissions s
        JOIN problems p ON s.pid = p.id
        WHERE s.created_by = :email
          AND s.created_at >= :startDate
          AND s.created_at <= :endDate
          AND p.is_contest_problem = false
        GROUP BY DATE(s.created_at)
""", nativeQuery = true)
    List<Object[]> countAcceptedByDay(
            @Param("email") String email,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT new com.example.hoccode.dto.response.SubmissionCategoryCount(CAST(c AS string), COUNT(DISTINCT p.id))
        FROM Submission s
        JOIN Problem p ON s.pid = p.id
        JOIN p.categories c
        WHERE s.createdBy = :email
          AND s.status = com.example.hoccode.util.constant.StatusEnum.ACCEPTED
          AND p.isContestProblem = false
        GROUP BY c
    """)
    List<SubmissionCategoryCount> countAcceptedByCategory(@Param("email") String email);

    @Query("""
    SELECT COUNT(DISTINCT s.pid)
    FROM Submission s
    JOIN Problem p ON s.pid = p.id
    WHERE s.createdBy = :email
      AND s.status = com.example.hoccode.util.constant.StatusEnum.ACCEPTED
      AND p.isContestProblem = false
""")
    long countAcceptedProblems(@Param("email") String email);

    @Query("""
    SELECT p.difficulty AS diff, COUNT(DISTINCT s.pid) AS solved
    FROM Submission s
    JOIN Problem p ON s.pid = p.id
    WHERE s.status = com.example.hoccode.util.constant.StatusEnum.ACCEPTED
      AND s.createdBy = :email
      AND p.isContestProblem = false
    GROUP BY p.difficulty
""")
    List<Object[]> countSolvedByDifficulty(@Param("email") String email);


    @Query(value = """
        WITH RECURSIVE days AS (
            SELECT CURDATE() - INTERVAL 9 DAY AS d
            UNION ALL
            SELECT d + INTERVAL 1 DAY FROM days WHERE d < CURDATE()
        )
        SELECT d AS day, COUNT(s.id) AS submissions
        FROM days
        LEFT JOIN submissions s
          ON DATE(s.created_at) = d
          AND s.created_by = :email
          AND NOT EXISTS (
              SELECT 1 FROM problems p
              WHERE p.id = s.pid AND p.is_contest_problem = true
          )
        GROUP BY d
        ORDER BY d
    """, nativeQuery = true)
    List<Object[]> countSubmissionLast10Days(@Param("email") String email);


//    @Query(value = """
//        SELECT s.*
//        FROM submissions s
//        JOIN problems p ON p.id = s.pid
//        JOIN contest_problems cp ON cp.problem_id = p.id
//        WHERE cp.contest_id = :contestId
//    """, nativeQuery = true)
//    List<Submission> findAllByContestId(@Param("contestId") Integer contestId);


    @Query(value = """
        SELECT s.*
        FROM submissions s
        JOIN problems p ON p.id = s.pid
        JOIN contest_problems cp ON cp.problem_id = p.id
        WHERE cp.contest_id = :contestId
          AND s.created_by = :email
    """, nativeQuery = true)
    List<Submission> findAllByContestIdAndUserEmail(
            @Param("contestId") Integer contestId,
            @Param("email") String email
    );


    @Query("""
        SELECT new com.example.hoccode.dto.projection.ProblemSubmissionCount(p, COUNT(s))
        FROM Problem p
        JOIN ContestProblem cp ON cp.problem.id = p.id
        LEFT JOIN Submission s ON s.pid = p.id
        WHERE cp.contest.id = :contestId
        GROUP BY p
""")
    List<ProblemSubmissionCount> countSubmissionsByProblem(
            @Param("contestId") Integer contestId
    );


    @Query(value = """
        SELECT s.created_by AS username,
               p.title AS problemName,
               s.status AS status,
               s.created_at AS submitTime
        FROM submissions s
        JOIN problems p ON p.id = s.pid
        JOIN contest_problems cp ON cp.problem_id = p.id
        WHERE cp.contest_id = :contestId
        ORDER BY s.created_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findRecentSubmissions(@Param("contestId") Integer contestId, @Param("limit") Integer limit);


    @Query("""
        SELECT new com.example.hoccode.dto.response.SubmissionProblemResponse(
                    s.id,
                    s.pid,
                    new com.example.hoccode.dto.response.SimpleProblemInfo(p.id, p.title, p.difficulty),
                    s.code,
                    s.language,
                    s.status,
                    s.totalTestCases,
                    s.passedTestCase,
                    s.message,
                    s.maxTime,
                    s.createdAt,
                    s.createdBy,
                    s.updatedAt,
                    s.updatedBy
                )
                FROM Submission s
                JOIN Problem p ON p.id = s.pid
                JOIN ContestProblem cp ON cp.problem.id = p.id
                WHERE cp.contest.id = :contestId
                ORDER BY s.createdAt DESC
    """)
    List<SubmissionProblemResponse> findAllByContestId(@Param("contestId") Integer contestId);


    @Query(
            value = """
        SELECT sub.id AS submissionId,
               sub.pid AS problemId,
               sub.code AS code,
               sub.language AS language,
               sub.created_by AS username,
               sub.status AS status,
               p.title AS problemTitle
        FROM (
            SELECT s.id,
                   s.pid,
                   s.code,
                   s.language,
                   s.created_by,
                   s.status,
                   ROW_NUMBER() OVER (
                       PARTITION BY s.created_by, s.pid
                       ORDER BY s.created_at ASC
                   ) as rn
            FROM submissions s
            JOIN contest_problems cp ON cp.problem_id = s.pid
            WHERE cp.contest_id = :contestId
              AND s.status = 'ACCEPTED'
        ) sub
        JOIN problems p ON p.id = sub.pid
        WHERE sub.rn = 1
        """,
            nativeQuery = true
    )
    List<SubmissionWithProblemTitleProjection> findFirstAcceptedSubmissionsWithProblemTitle(@Param("contestId") Integer contestId);

    @Query("SELECT DISTINCT s.createdBy FROM Submission s")
    List<String> findDistinctCreatedBy();


    @Query(value = """
        SELECT * FROM submissions s
        WHERE s.created_by = :user
          AND s.pid = :pid
          AND s.created_at > (
              SELECT COALESCE(
                  (
                      SELECT s2.created_at
                      FROM submissions s2
                      WHERE s2.created_by = :user
                        AND s2.pid = :pid
                        AND s2.status = 'ACCEPTED'
                      ORDER BY s2.created_at DESC
                      LIMIT 1 OFFSET 1
                  ),
                  '1970-01-01 00:00:00'
              )
          )
          AND s.created_at <= (
              SELECT s3.created_at
              FROM submissions s3
              WHERE s3.created_by = :user
                AND s3.pid = :pid
                AND s3.status = 'ACCEPTED'
              ORDER BY s3.created_at DESC
              LIMIT 1
          )
        ORDER BY s.created_at ASC
        """, nativeQuery = true)
    List<Submission> findRecentSubmissionsBetweenLastTwoAccepted(
            @Param("user") String user,
            @Param("pid") Integer pid
    );

    Optional<Submission> findFirstById(String id);

    Optional<Submission> findTopByCreatedByAndPidOrderByCreatedAtDesc(String createdBy, Integer pid);
}
