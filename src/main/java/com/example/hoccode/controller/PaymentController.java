package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.service.IPaymentService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

@RestController
@RequestMapping("/payment")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    IPaymentService paymentService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<CreatePaymentLinkResponse>> payment() throws Exception {
        ApiResponse<CreatePaymentLinkResponse> apiResponse = ApiResponse.<CreatePaymentLinkResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Payment")
                .data(paymentService.createPayment())
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<WebhookData>> handleWebhook(@RequestBody Webhook webhook) throws Exception {
        ApiResponse<WebhookData> apiResponse = ApiResponse.<WebhookData>builder()
                .code(HttpStatus.OK.value())
                .message("webhook")
                .data(paymentService.webhook(webhook))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);

    }
}
