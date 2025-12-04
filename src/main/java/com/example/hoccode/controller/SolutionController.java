package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.request.create.CreateSolutionRequest;
import com.example.hoccode.entity.Solution;
import com.example.hoccode.service.ISolutionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/solution")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SolutionController {
    ISolutionService solutionService;

    @GetMapping("/{pid}")
    public ResponseEntity<ApiResponse<List<Solution>>> getSolutions(@PathVariable(required = true) Integer pid) {
        ApiResponse<List<Solution>> apiResponse = ApiResponse.<List<Solution>>builder()
                .code(HttpStatus.OK.value())
                .message("Problem Solution")
                .data(solutionService.getSolutions(pid))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

//    @PostMapping("")
//    public ResponseEntity<ApiResponse<List<Solution>>> create(@RequestBody List<CreateSolutionRequest> request) {
//        ApiResponse<List<Solution>> apiResponse = ApiResponse.<List<Solution>>builder()
//                .code(HttpStatus.OK.value())
//                .message("Problem Solution Create")
//                .data(solutionService.create(request))
//                .build();
//        return ResponseEntity.ok()
//                .body(apiResponse);
//    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Solution>> create(@RequestBody CreateSolutionRequest request) {
        ApiResponse<Solution> apiResponse = ApiResponse.<Solution>builder()
                .code(HttpStatus.OK.value())
                .message("Problem Solution Create")
                .data(solutionService.create(request))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Solution>> update(@PathVariable(required = true) Integer id, @RequestBody CreateSolutionRequest request) {
        ApiResponse<Solution> apiResponse = ApiResponse.<Solution>builder()
                .code(HttpStatus.OK.value())
                .message("Problem Solution Update")
                .data(solutionService.update(request, id))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<Solution>> delete(@PathVariable(required = true) Integer id) {
        ApiResponse<Solution> apiResponse = ApiResponse.<Solution>builder()
                .code(HttpStatus.OK.value())
                .message("Problem Solution Delete")
                .data(solutionService.delete(id))
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

}
