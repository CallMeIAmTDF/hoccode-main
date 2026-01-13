package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.SocketMessage;
import com.example.hoccode.dto.request.SolveReportRequest;
import com.example.hoccode.dto.request.create.CreateReportRequest;
import com.example.hoccode.entity.Notification;
import com.example.hoccode.entity.Report;
import com.example.hoccode.repository.NotificationRepository;
import com.example.hoccode.repository.ReportRepository;
import com.example.hoccode.service.IReportService;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.SensitiveWordValidationUtil;
import com.example.hoccode.util.constant.NotificationStatusEnum;
import com.example.hoccode.util.constant.NotificationTypeEnum;
import com.example.hoccode.util.constant.SocketType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportService implements IReportService {
    ReportRepository reportRepository;
    SensitiveWordValidationUtil wordValidation;
    NotificationRepository notificationRepository;
    SecurityUtil securityUtil;
    SimpMessagingTemplate messagingTemplate;

    @Override
    public Report create(CreateReportRequest request){
        wordValidation.validateSensitiveWords(List.of(
                request.getTitle(),
                request.getDescription()
        ));
        return reportRepository.save(
                Report.builder()
                        .title(request.getTitle())
                        .description(request.getDescription())
                        .target(request.getTarget())
                        .type(request.getType())
                        .solved(false)
                        .solved_message(null)
                        .build()
        );
    }

    @Override
    public void solved(SolveReportRequest request){
        if(!securityUtil.isAdmin()) {
            throw new BadRequestException("Không có quyền truy cập");
        }

        // chỉnh sửa database
        Report report = reportRepository.findById(request.getId()).orElseThrow(() -> new BadRequestException("Không tìm thấy báo cáo"));
        report.setSolved(true);
        report.setSolved_message(request.getMessage());
        reportRepository.save(report);

        // tạo thông báo người dùng
        notificationRepository.save(Notification.builder()
                        .title("Thông báo hệ thống")
                        .message("Báo cáo của bạn đã được giải quyết")
                        .receiver(report.getCreatedBy())
                        .redirectUrl("/profile/report/")
                        .type(NotificationTypeEnum.SYSTEM)
                        .status(NotificationStatusEnum.SEND)
                .build());

        // gửi socket
        messagingTemplate.convertAndSendToUser(
                report.getCreatedBy(),
                "/topic/notification",
                new SocketMessage(SocketType.NEW_NOTIFICATION, "Thông báo mới",
                        null)
        );
    }

    @Override
    public List<Report> getReports(){
        if(!securityUtil.isAdmin()){
            return reportRepository.findAllByCreatedBy(securityUtil.getCurrentUserLogin().get());
        }
        return reportRepository.findAllByOrderBySolvedAscCreatedAtDesc();
    }
//
//    @Override
//    public List<Report> myReport(){
//        return reportRepository.findAllByCreatedBy(securityUtil.getCurrentUserLogin().get());
//    }
}
