package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.request.ForgotPasswordRequest;
import com.example.hoccode.dto.request.UserUpdateRequest;
import com.example.hoccode.dto.response.AuthenticationResponse;
import com.example.hoccode.entity.Solution;
import com.example.hoccode.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("")
    public ResponseEntity<ApiResponse<Void>> update(@RequestBody UserUpdateRequest request) {
        userService.update(request);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("User Update")
                .data(null)
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }
}
