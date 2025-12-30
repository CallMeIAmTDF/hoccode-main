package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.service.IPremiumUserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/premium")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PremiumUserController {
    IPremiumUserService premiumUserService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<Boolean>> getAll(){
        ApiResponse<Boolean> apiResponse = ApiResponse.<Boolean>builder()
                .code(HttpStatus.OK.value())
                .message("Is Premium User")
                .data(premiumUserService.isPremium())
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }
}
