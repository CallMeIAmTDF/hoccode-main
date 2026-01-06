package com.example.hoccode.service.impl;

import com.example.hoccode.dto.request.DataMailDto;
import com.example.hoccode.event.NotificationEvent;
import com.example.hoccode.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {
    private JavaMailSender mailSender;
    private SpringTemplateEngine templateEngine;

    @Value("${api.base-url}")
    @NonFinal
    String baseUrl;

    @Value("${api.path.verify-register}")
    @NonFinal
    String urlVerifyRegister;

    @Value("${api.path.verify-forgot-password}")
    @NonFinal
    String urlVerifyForgotPassword;

    @Value("${api.path.notice-contest}")
    @NonFinal
    String urlNoticeContest;

    @Value("${api.path.noti-streak}")
    @NonFinal
    String urlNoticeStreak;

    public void sendEmailRegister(NotificationEvent message) {
        try {
            putPropsDataMail(convertDataMail(message, urlVerifyRegister), "verify_register");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendEmailForgotPassword(NotificationEvent message) {
        try {
            putPropsDataMail(convertDataMail(message, urlVerifyForgotPassword), "verify_forgot_password");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendEmailContest(NotificationEvent message) {
        try {
            putPropsDataMail(convertDataMail(message, urlNoticeContest), "notice_contest");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendEmailHappyBirthday(NotificationEvent message) {
        try {
            putPropsDataMail(convertDataMail(message, ""), "happy_birthday");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendEmailStreaks(NotificationEvent message) {
        try {
            putPropsDataMail(convertDataMail(message, urlNoticeStreak), "streak_noti");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendEmailPremium(NotificationEvent message) {
        try {
            putPropsDataMail(convertDataMail(message, ""), "premium_noti");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void sendEmailPlagiarism(NotificationEvent message) {
        try {
            putPropsDataMail(convertDataMail(message, urlNoticeStreak), "plagiarism_noti");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private DataMailDto convertDataMail(NotificationEvent message, String urlBase) throws MessagingException {
        Map<String, Object> props = message.getParam().entrySet().stream()
                .filter(entry -> !"code".equals(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        if (message.getParam().containsKey("code")) {
            props.put("url", this.baseUrl + urlBase + message.getParam().get("code"));
        }
        return DataMailDto.builder().to(message.getRecipient()).subject(message.getSubject()).props(props).build();
    }

    private void putPropsDataMail(DataMailDto dataMail, String templateName) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

        Context context = new Context();
        context.setVariables(dataMail.getProps());

        String html = templateEngine.process(templateName, context);
        helper.setFrom("noreply@hoccode.cloud");
        helper.setTo(dataMail.getTo().split(","));
        helper.setSubject(dataMail.getSubject());
        helper.setText(html, true);

        mailSender.send(message);
    }
}