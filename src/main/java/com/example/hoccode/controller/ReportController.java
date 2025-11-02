package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.request.SolveReportRequest;
import com.example.hoccode.dto.request.create.CreateProblemRequest;
import com.example.hoccode.dto.request.create.CreateReportRequest;
import com.example.hoccode.entity.Problem;
import com.example.hoccode.entity.Report;
import com.example.hoccode.service.IReportService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportController {

    IReportService reportService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<Report>> create(@RequestBody CreateReportRequest request){
        ApiResponse<Report> apiResponse = ApiResponse.<Report>builder()
                .code(HttpStatus.OK.value())
                .message("Create Report")
                .data(reportService.create(request))
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Report>>> getAll(){
        ApiResponse<List<Report>> apiResponse = ApiResponse.<List<Report>>builder()
                .code(HttpStatus.OK.value())
                .message("Get Report")
                .data(reportService.getReports())
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @PostMapping("/solved")
    public ResponseEntity<ApiResponse<Void>> solved(@RequestBody SolveReportRequest request){
        reportService.solved(request);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Solved Report")
                .data(null)
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

}
