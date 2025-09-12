package com.example.hoccode.repository;

import com.example.hoccode.entity.Room;
import com.example.hoccode.util.constant.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, String> {
    @Query("SELECT r FROM Room r WHERE r.status = 'LOBBY' AND (SELECT COUNT(ru) FROM RoomUser ru WHERE ru.roomId = r.id) < 8")
    List<Room> findOpenLobbies();

    List<Room> findAllByStatus(RoomStatus status);

    @Query("SELECT r FROM Room r JOIN RoomUser ru ON r.id = ru.roomId WHERE ru.userId = :userId AND r.status IN ('LOBBY', 'STARTED')")
    Optional<Room> findActiveRoomByUserId(String userId);
}