package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.response.CategoryCountResponse;
import com.example.hoccode.entity.Problem;
import com.example.hoccode.service.IProblemService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    IProblemService problemService;
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<List<CategoryCountResponse>>>  getCategoryStats() {

        ApiResponse<List<CategoryCountResponse>> apiResponse = ApiResponse.<List<CategoryCountResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Category Stats")
                .data(problemService.getCategoryCounts())
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }
}
