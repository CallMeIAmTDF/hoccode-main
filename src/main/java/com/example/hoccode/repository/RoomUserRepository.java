package com.example.hoccode.repository;

import com.example.hoccode.entity.RoomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomUserRepository extends JpaRepository<RoomUser, Long> {
    long countByRoomId(String roomId);

    List<RoomUser> findByRoomId(String roomId);

    void deleteByRoomId(String roomId);

    @Query("SELECT ru FROM RoomUser ru WHERE ru.roomId = :roomId ORDER BY COALESCE(ru.score, 0) DESC, " +
            "             CASE WHEN ru.submissionTime IS NULL THEN 1 ELSE 0 END," +
            "             ru.submissionTime ASC")
    List<RoomUser> findByRoomIdOrderByScoreDesc(String roomId);

    Optional<RoomUser> findByRoomIdAndUserId(String roomId, String userId);

    Optional<RoomUser> findBySubmissionId(String submissionId);
}