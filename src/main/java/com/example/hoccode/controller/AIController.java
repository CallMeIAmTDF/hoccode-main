package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.response.InDepthCodeAnalysisResponse;
import com.example.hoccode.dto.response.call.BasicCodeAnalysisResponse;
import com.example.hoccode.service.IGPTService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AIController {
    IGPTService gptService;

    @GetMapping("/codeAnalysis/{submissionId}")
    public ResponseEntity<ApiResponse<BasicCodeAnalysisResponse>> getCodeAnalysis(@PathVariable(required = true) String submissionId) {
        ApiResponse<BasicCodeAnalysisResponse> apiResponse = ApiResponse.<BasicCodeAnalysisResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Basic Code Analysis")
                .data(gptService.basicCodeAnalysis(submissionId))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/codeAnalysis/inDepth/{problemId}")
    public ResponseEntity<ApiResponse<InDepthCodeAnalysisResponse>> getInDepthCodeAnalysis(@PathVariable(required = true) Integer problemId) {
        ApiResponse<InDepthCodeAnalysisResponse> apiResponse = ApiResponse.<InDepthCodeAnalysisResponse>builder()
                .code(HttpStatus.OK.value())
                .message("In-Depth Code Analysis")
                .data(gptService.inDepthCodeAnalysis(problemId))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }
}
