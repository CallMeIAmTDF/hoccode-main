package com.example.hoccode.service;

import com.example.hoccode.event.NotificationEvent;

public interface EmailService {
    void sendEmailRegister(NotificationEvent message);

    void sendEmailForgotPassword(NotificationEvent message);

    void sendEmailContest(NotificationEvent message);

    void sendEmailHappyBirthday(NotificationEvent message);

    void sendEmailStreaks(NotificationEvent message);

    void sendEmailPlagiarism(NotificationEvent message);
}