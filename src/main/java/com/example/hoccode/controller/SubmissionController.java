package com.example.hoccode.controller;


import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.request.SubmissionUpdateRequest;
import com.example.hoccode.dto.request.create.CreateSubmissionRequest;
import com.example.hoccode.dto.response.call.CodeRunnerResponse;
import com.example.hoccode.entity.Submission;
import com.example.hoccode.service.ISubmissionService;
import com.example.hoccode.util.CustomHeaders;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/submission")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubmissionController {
    ISubmissionService submissionService;
    @GetMapping("/{id}/results")
    public ResponseEntity<ApiResponse<Map<String, Object>>> viewResult(@PathVariable(required = true) String id){
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>builder()
                .code(HttpStatus.OK.value())
                .message("View Submission " + id)
                .data(submissionService.viewResults(id))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<String>> create(@RequestBody CreateSubmissionRequest request){
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Submit")
                .data(submissionService.create(request).getId())
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @PostMapping("/handleCallBackApiHeheheheasd")
    public void handleCallBackApi(@RequestBody SubmissionUpdateRequest request,
        @RequestHeader(value = CustomHeaders.X_CODE_RUNNER_API, required = false) String header
    ){
        if(header == null || header.isEmpty()){
            throw new BadRequestException("Don't use API");
        }
        submissionService.handleCallBack(request);
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<Submission>>> getMySubmission(){
        ApiResponse<List<Submission>> apiResponse = ApiResponse.<List<Submission>>builder()
                .code(HttpStatus.OK.value())
                .message("Get My Submission")
                .data(submissionService.getMySubmission())
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }


    @PostMapping("/run")
    public ResponseEntity<ApiResponse<CodeRunnerResponse>> run(@RequestBody CreateSubmissionRequest request){
        ApiResponse<CodeRunnerResponse> apiResponse = ApiResponse.<CodeRunnerResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Submit")
                .data(submissionService.run(request, true))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }
}
