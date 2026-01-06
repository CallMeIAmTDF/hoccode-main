package com.example.hoccode.controller;

import com.example.hoccode.config.template.RabbitKafkaTemplate;
import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.SocketMessage;
import com.example.hoccode.dto.analysis.GPTProfileAnalysisResponse;
import com.example.hoccode.dto.response.pdf.PdfFileInfo;
import com.example.hoccode.repository.SubmissionRepository;
import com.example.hoccode.service.*;
import com.example.hoccode.util.constant.SocketType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestController {
    RabbitKafkaTemplate kafkaTemplate;
    SubmissionRepository submissionRepository;
    IContestService contestService;
    IGPTService gptService;
    IPlagiarismService plagiarismService;

    SimpMessagingTemplate webSocketTemplate;
    IPaymentService paymentService;

    @GetMapping("/payment")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentStats() {
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>builder()
                .code(HttpStatus.OK.value())
                .message("Payment Stats Item")
                .data(paymentService.stats())
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/payment/file")
    public ResponseEntity<ApiResponse<List<PdfFileInfo>>> getReportFile() {
        ApiResponse<List<PdfFileInfo>> apiResponse = ApiResponse.<List<PdfFileInfo>>builder()
                .code(HttpStatus.OK.value())
                .message("Payment Stats File")
                .data(paymentService.getReportFile())
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/testWebSocket")
    public void testWebSocket(){
        webSocketTemplate.convertAndSendToUser(
                "thaidangfa@gmail.com",
                "/topic/normal",
                new SocketMessage(SocketType.STREAK_UPDATE, "STREAK_UPDATE",
                        17)
        );
    }

    @GetMapping("/testWebSocket2")
    public void testWebSocket2(){
        webSocketTemplate.convertAndSendToUser(
                "thaidangfa@gmail.com",
                "/topic/notification",
                new SocketMessage(SocketType.NEW_NOTIFICATION, "STREAK_UPDATE",
                        17)
        );
    }

    @GetMapping("/payment/genFullFile")
    public ResponseEntity<ApiResponse<Void>> genFullFile() throws Exception {
        paymentService.genPdf();
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Payment Stats File")
                .data(null)
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }
}
