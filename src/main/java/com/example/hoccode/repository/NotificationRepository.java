package com.example.hoccode.repository;

import com.example.hoccode.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository  extends JpaRepository<Notification, Integer> {
    List<Notification> findAllByReceiverOrderByCreatedAtDesc(String email);


    @Query("SELECT n FROM Notification n WHERE n.receiver = :email OR n.receiver = 'ALL' ORDER BY n.createdAt DESC")
    List<Notification> findAllForUser(@Param("email") String email);


    Optional<Notification> findFirstById(Integer id);
}
