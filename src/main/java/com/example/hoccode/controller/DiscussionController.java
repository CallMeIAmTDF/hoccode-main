package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.request.create.CreateDiscussionRequest;
import com.example.hoccode.entity.Contest;
import com.example.hoccode.entity.Discussion;
import com.example.hoccode.service.IDiscussionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/discussion")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DiscussionController {
    IDiscussionService discussionService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<Discussion>>> getProblemDiscussions(@RequestParam(defaultValue = "0") Integer pid){
        ApiResponse<List<Discussion>> apiResponse = ApiResponse.<List<Discussion>>builder()
                .code(HttpStatus.OK.value())
                .message("Get Problem Discussions")
                .data(discussionService.getDiscussions(pid))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Discussion>> getDiscussion(@PathVariable(required = true) Integer id){
        ApiResponse<Discussion> apiResponse = ApiResponse.<Discussion>builder()
                .code(HttpStatus.OK.value())
                .message("Get Discussion Detail")
                .data(discussionService.view(id))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Discussion>> create(@RequestBody CreateDiscussionRequest request){
        ApiResponse<Discussion> apiResponse = ApiResponse.<Discussion>builder()
                .code(HttpStatus.OK.value())
                .message("Create Discussion")
                .data(discussionService.create(request))
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/{id}/delete")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable(required = true) Integer id){
        discussionService.delete(id);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Delete Discussion")
                .data(null)
                .build();
        return ResponseEntity.ok()
                .body(apiResponse);
    }

}
