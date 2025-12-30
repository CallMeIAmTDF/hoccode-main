package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.SocketMessage;
import com.example.hoccode.dto.request.create.CreateNotificationRequest;
import com.example.hoccode.entity.Notification;
import com.example.hoccode.repository.NotificationRepository;
import com.example.hoccode.service.INotificationService;
import com.example.hoccode.service.redis.IStringStringIntegerRedis;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.constant.NotificationStatusEnum;
import com.example.hoccode.util.constant.NotificationTypeEnum;
import com.example.hoccode.util.constant.SocketType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService implements INotificationService {
    NotificationRepository notificationRepository;
    SecurityUtil securityUtil;
    IStringStringIntegerRedis redis;
    @Override
    public List<Notification> getMyNotification(){
        List<Notification> notifications = notificationRepository.findAllForUser(securityUtil.getCurrentUserLogin().get());
        return notifications.stream().map(
                notification -> {
                    if(notification.getReceiver().equals("ALL")){
                        if(redis.isRead(securityUtil.getCurrentUserLogin().get(), String.valueOf(notification.getId()))){
                            notification.setStatus(NotificationStatusEnum.READ);
                            return notification;
                        }
                    }
                    return notification;
                }
        ).toList();
    }

    @Override
    public void read(Integer id){
        Notification notification = notificationRepository.findFirstById(id).orElseThrow(() -> new BadRequestException("Thông báo không tồn tại"));
        if(!notification.getReceiver().equals(securityUtil.getCurrentUserLogin().get()) && !notification.getReceiver().equals("ALL")){
            throw new BadRequestException("Bạn không thể xem");
        }
        if(notification.getReceiver().equals("ALL")){
            if(!redis.isRead(securityUtil.getCurrentUserLogin().get(), String.valueOf(id))){
                redis.saveReadStatus(securityUtil.getCurrentUserLogin().get(), String.valueOf(id));
            }
            return;
        }
        notification.setStatus(NotificationStatusEnum.READ);
        notificationRepository.save(notification);
    }

    @Override
    public Notification create(CreateNotificationRequest request){
        if(!securityUtil.isAdmin()) throw new BadRequestException("Không có quyền truy cập");

        return notificationRepository.save(
                Notification.builder()
                        .type(NotificationTypeEnum.SYSTEM)
                        .status(NotificationStatusEnum.SEND)
                        .title(request.getTitle())
                        .redirectUrl(null)
                        .receiver("ALL")
                        .message(request.getContent())
                        .build()
        );
    }

    @Override
    public List<Notification> getAllForAdmin(){
        return notificationRepository.findAllForUser("");
    }


}
