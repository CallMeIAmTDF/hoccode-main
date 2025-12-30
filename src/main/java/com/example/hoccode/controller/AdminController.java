package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.request.create.CreateNotificationRequest;
import com.example.hoccode.dto.response.AdminProblemResponse;
import com.example.hoccode.dto.response.UserResponse;
import com.example.hoccode.entity.Notification;
import com.example.hoccode.entity.Solution;
import com.example.hoccode.service.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminController {
    IAdminService adminService;
    IProblemService problemService;
    UserService userService;
    INotificationService notificationService;
    ISolutionService solutionService;
    IPaymentService paymentService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>builder()
                .code(HttpStatus.OK.value())
                .message("Dashboard Stats Item")
                .data(adminService.dashboardStatsItemList())
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/problems")
    public ResponseEntity<ApiResponse<List<AdminProblemResponse>>> getProblems() {
        ApiResponse<List<AdminProblemResponse>> apiResponse = ApiResponse.<List<AdminProblemResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Admin Problem Response")
                .data(problemService.getProblemForAdmin())
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);

    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers() {
        ApiResponse<List<UserResponse>> apiResponse = ApiResponse.<List<UserResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Admin User Response")
                .data(userService.getAllUsers())
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/users/{uid}/lock")
    public ResponseEntity<ApiResponse<Void>> lockUsers(@PathVariable(required = true) String uid) {
        userService.lock(uid);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Lock User")
                .data(null)
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications() {
        ApiResponse<List<Notification>> apiResponse = ApiResponse.<List<Notification>>builder()
                .code(HttpStatus.OK.value())
                .message("Admin Notification Response")
                .data(notificationService.getAllForAdmin())
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @PostMapping("/notifications")
    public ResponseEntity<ApiResponse<Notification>> createNotification(@RequestBody CreateNotificationRequest request) {
        ApiResponse<Notification> apiResponse = ApiResponse.<Notification>builder()
                .code(HttpStatus.OK.value())
                .message("Create Notification")
                .data(notificationService.create(request))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/solution/{pid}")
    public ResponseEntity<ApiResponse<List<Solution>>> getSolutions(@PathVariable(required = true) Integer pid) {
        ApiResponse<List<Solution>> apiResponse = ApiResponse.<List<Solution>>builder()
                .code(HttpStatus.OK.value())
                .message("Problem Solution")
                .data(solutionService.getSolutions(pid))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/payment/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentStats() {
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>builder()
                .code(HttpStatus.OK.value())
                .message("Dashboard Stats Item")
                .data(paymentService.stats())
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/payment/stats/download/{fileName}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable String fileName) {
        byte[] fileData = paymentService.downloadReports(fileName);

        if (fileData == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(fileName)
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileData);
    }
}

