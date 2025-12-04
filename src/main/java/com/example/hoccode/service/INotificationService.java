package com.example.hoccode.service;

import com.example.hoccode.dto.request.create.CreateNotificationRequest;
import com.example.hoccode.entity.Notification;

import java.util.List;

public interface INotificationService {
    List<Notification> getMyNotification();
    void read(Integer id);
    Notification create(CreateNotificationRequest request);

    List<Notification> getAllForAdmin();
}
