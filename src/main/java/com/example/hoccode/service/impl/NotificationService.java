package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.entity.Notification;
import com.example.hoccode.repository.NotificationRepository;
import com.example.hoccode.service.INotificationService;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.constant.NotificationStatusEnum;
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

    @Override
    public List<Notification> getMyNotification(){
        return notificationRepository.findAllByReceiverOrderByCreatedAtDesc(securityUtil.getCurrentUserLogin().get());
    }

    @Override
    public void read(Integer id){
        Notification notification = notificationRepository.findFirstById(id).orElseThrow(() -> new BadRequestException("Thông báo không tồn tại"));

        if(!notification.getReceiver().equals(securityUtil.getCurrentUserLogin().get())){
            throw new BadRequestException("Bạn không thể xem");
        }
        notification.setStatus(NotificationStatusEnum.READ);
        notificationRepository.save(notification);
    }
}
