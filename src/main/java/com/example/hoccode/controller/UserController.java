package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.request.ForgotPasswordRequest;
import com.example.hoccode.dto.response.AuthenticationResponse;
import com.example.hoccode.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping("/forgotPassword")
    ApiResponse<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        userService.forgotPassword(forgotPasswordRequest.getEmail());
        return ApiResponse.<AuthenticationResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Verify email register success")
                .data(null)
                .build();
    }
}
