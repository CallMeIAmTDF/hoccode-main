package com.example.hoccode.repository;

import com.example.hoccode.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository  extends JpaRepository<Notification, Integer> {
    List<Notification> findAllByReceiverOrderByCreatedAtDesc(String email);

    Optional<Notification> findFirstById(Integer id);
}
