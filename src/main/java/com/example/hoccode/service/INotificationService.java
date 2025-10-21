package com.example.hoccode.service;

import com.example.hoccode.entity.Notification;

import java.util.List;

public interface INotificationService {
    List<Notification> getMyNotification();

    void read(Integer id);
}
