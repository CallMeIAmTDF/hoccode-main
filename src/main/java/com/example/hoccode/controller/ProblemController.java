package com.example.hoccode.controller;

import com.example.hoccode.advice.exception.IdInvalidException;
import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.request.ProblemSearchRequest;
import com.example.hoccode.dto.request.create.CreateProblemRequest;
import com.example.hoccode.dto.request.update.UpdateProblemRequest;
import com.example.hoccode.dto.response.ProblemResponse;
import com.example.hoccode.dto.response.UserPointResponse;
import com.example.hoccode.entity.Problem;
import com.example.hoccode.service.IProblemService;
import com.example.hoccode.service.UserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/problem")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProblemController {
    IProblemService problemService;
    UserService userService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<Problem>> create(@RequestBody CreateProblemRequest createProblemRequest){
        ApiResponse<Problem> apiResponse = ApiResponse.<Problem>builder()
                .code(HttpStatus.OK.value())
                .message("Create Problem")
                .data(problemService.create(createProblemRequest, false))
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }


    @GetMapping("")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAll(ProblemSearchRequest request){
        Map<String, Object> map = new HashMap<>();
        map.put("daily", problemService.getDailyProblem());
        map.put("problems", problemService.getAll(request));
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>builder()
                .code(HttpStatus.OK.value())
                .message("Get Problem")
                .data(map)
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @PutMapping("/{problem_id}")
    public ResponseEntity<ApiResponse<ProblemResponse>> update(@PathVariable Integer problem_id, @RequestBody UpdateProblemRequest request) throws IdInvalidException {
        ApiResponse<ProblemResponse> apiResponse = ApiResponse.<ProblemResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update Problem")
                .data(problemService.update(problem_id, request))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<UserPointResponse>>> getLeaderBoard(){
        ApiResponse<List<UserPointResponse>> apiResponse = ApiResponse.<List<UserPointResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Get Leaderboard")
                .data(userService.getLeaderBoard())
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getProblem(@PathVariable(required = true) Integer id){
        ApiResponse<ProblemResponse> apiResponse = ApiResponse.<ProblemResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Get Problem")
                .data(problemService.problemDetail(id))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }
}