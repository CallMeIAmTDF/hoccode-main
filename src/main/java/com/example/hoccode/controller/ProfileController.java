package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.record.DailySubmission;
import com.example.hoccode.dto.record.ProblemStats;
import com.example.hoccode.dto.response.SubmissionCategoryCount;
import com.example.hoccode.service.ISubmissionService;
import com.example.hoccode.service.UserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/profile")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileController {

    ISubmissionService submissionService;
    UserService userService;
// streak


// problem solved diff
@GetMapping("/problemSolvedDiff")
public ResponseEntity<ApiResponse<ProblemStats>> getProblemSolvedDiff(){
    ApiResponse<ProblemStats> apiResponse = ApiResponse.<ProblemStats>builder()
            .code(HttpStatus.OK.value())
            .message("Get Problem Solved Diff")
            .data(submissionService.getProblemStats())
            .build();
    return ResponseEntity.ok()
            .body(apiResponse);
}

// submission by category
    @GetMapping("/submissionByCategory")
    public ResponseEntity<ApiResponse<List<SubmissionCategoryCount>>> getSubmissionByCategory(){
        ApiResponse<List<SubmissionCategoryCount>> apiResponse = ApiResponse.<List<SubmissionCategoryCount>>builder()
                .code(HttpStatus.OK.value())
                .message("Get Submission By Category")
                .data(submissionService.getSubmissonCategoryCount())
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

// activity
    //heatmap
    @GetMapping("/heatmap")
    public ResponseEntity<ApiResponse<Map<LocalDate, Integer>>> getHeatmap(){
        ApiResponse<Map<LocalDate, Integer>> apiResponse = ApiResponse.<Map<LocalDate, Integer>>builder()
                .code(HttpStatus.OK.value())
                .message("Get Heatmap")
                .data(submissionService.getAcceptedCountLastYear())
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    //bar
    @GetMapping("/getDailySubmissions")
    public ResponseEntity<ApiResponse<List<DailySubmission>>> getDailySubmissions(){
        ApiResponse<List<DailySubmission>> apiResponse = ApiResponse.<List<DailySubmission>>builder()
                .code(HttpStatus.OK.value())
                .message("Get Daily Submissions")
                .data(submissionService.getDailySubmissions())
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }


    // stats (point, rank, solve, streak)
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats(){
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>builder()
                .code(HttpStatus.OK.value())
                .message("Get Stats")
                .data(userService.getUserStats())
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }


}
