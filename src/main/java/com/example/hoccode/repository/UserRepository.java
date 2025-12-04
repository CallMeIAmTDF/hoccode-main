package com.example.hoccode.repository;

import com.example.hoccode.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndActive(String email, Boolean active);

    Optional<User> findFirstByEmailAndActiveAndIsLocked(String email, Boolean active, Boolean lock);

    Optional<User> findByIdAndActive(String id, Boolean active);

    List<User> findByIdIn(List<String> ids);

    List<User> findTop15ByActiveTrueAndRoleOrderByPointDesc(String role);


    @Query(value = """
        SELECT COUNT(*) + 1 
        FROM users u
        WHERE u.point > (SELECT point FROM users WHERE email = :email)
        """, nativeQuery = true)
    Integer getUserRankByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u " +
            "WHERE MONTH(u.dob) = :month " +
            "AND DAY(u.dob) = :day")
    List<User> findUsersByBirthday(@Param("day") int day, @Param("month") int month);

    @Query("SELECT COUNT(u) FROM User u WHERE FUNCTION('MONTH', u.createdAt) = :month AND FUNCTION('YEAR', u.createdAt) = :year")
    long countUsersCreatedInMonth(@Param("month") int month, @Param("year") int year);

    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.createdAt < :startOfThisMonth
    """)
    long countUsersBeforeMonth(@Param("startOfThisMonth") LocalDateTime startOfThisMonth);

    @Query("""
        SELECT u
        FROM User u
            WHERE u.role = "USER"
        ORDER BY u.isLocked ASC, u.createdAt DESC
    """)
    List<User> findAllOrderByLocked();
}