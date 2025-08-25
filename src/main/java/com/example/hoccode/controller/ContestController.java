package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.request.create.CreateContestRequest;
import com.example.hoccode.dto.response.contest.ContestDetailResponse;
import com.example.hoccode.dto.response.contest.ContestLeaderboardResponse;
import com.example.hoccode.dto.response.contest.ContestResponse;
import com.example.hoccode.entity.Contest;
import com.example.hoccode.service.IContestService;
import com.example.hoccode.service.ISubmissionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contest")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContestController {
    IContestService contestService;
    ISubmissionService submissionService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<Contest>> create(@RequestBody CreateContestRequest request){
        ApiResponse<Contest> apiResponse = ApiResponse.<Contest>builder()
                .code(HttpStatus.OK.value())
                .message("Create Contest")
                .data(contestService.create(request))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<ContestResponse>>> getAll(){
        ApiResponse<List<ContestResponse>> apiResponse = ApiResponse.<List<ContestResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch Contest")
                .data(contestService.getAll())
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Contest>> update(CreateContestRequest request, @PathVariable(value = "0") Integer id){
        ApiResponse<Contest> apiResponse = ApiResponse.<Contest>builder()
                .code(HttpStatus.OK.value())
                .message("Update Contest")
                .data(contestService.updateContest(id, request))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/{id}/edit")
    public ResponseEntity<ApiResponse<ContestDetailResponse>> edit(@PathVariable Integer id){
        ApiResponse<ContestDetailResponse> apiResponse = ApiResponse.<ContestDetailResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Get Contest")
                .data(contestService.edit(id))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContestDetailResponse>> getDetail(@PathVariable(required = true) Integer id){
        ApiResponse<ContestDetailResponse> apiResponse = ApiResponse.<ContestDetailResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Get Contest")
                .data(contestService.getContestDetail(id))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }


    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<Void>> joinContest(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "") String password
    ) {
        contestService.joinContest(id, password);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Join Contest")
                .data(null)
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<ApiResponse<ContestLeaderboardResponse>> getLeaderboard(@PathVariable Integer id, @RequestParam(defaultValue = "1") int page){
        int size = 15;
        ApiResponse<ContestLeaderboardResponse> apiResponse = ApiResponse.<ContestLeaderboardResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Contest Leaderboard")
                .data(submissionService.getContestLeaderboard(id, page, size))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }


}
