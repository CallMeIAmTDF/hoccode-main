package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.request.create.CreateNotificationRequest;
import com.example.hoccode.entity.Notification;
import com.example.hoccode.service.INotificationService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserNotificationController {
    INotificationService notificationService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications() {
        ApiResponse<List<Notification>> apiResponse = ApiResponse.<List<Notification>>builder()
                .code(HttpStatus.OK.value())
                .message("Get all notifications")
                .data(notificationService.getMyNotification())
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> read(@PathVariable(required = true) Integer id) {
        notificationService.read(id);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Read Notification")
                .data(null)
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Notification>> create(@RequestBody CreateNotificationRequest request) {
        ApiResponse<Notification> apiResponse = ApiResponse.<Notification>builder()
                .code(HttpStatus.OK.value())
                .message("Create Notification")
                .data(notificationService.create(request))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }
}
