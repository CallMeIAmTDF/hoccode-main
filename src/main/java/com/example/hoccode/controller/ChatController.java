package com.example.hoccode.controller;

import com.example.hoccode.dto.ApiResponse;
import com.example.hoccode.dto.rag.ChatRequest;
import com.example.hoccode.dto.rag.ChatResponse;
import com.example.hoccode.dto.request.DocumentUploadRequest;
import com.example.hoccode.entity.Document;
import com.example.hoccode.service.IChatService;
import com.example.hoccode.util.constant.FileUploadStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.print.Doc;
import java.util.List;


@RestController
@RequestMapping("/chat")
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {
    IChatService chatService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@RequestBody ChatRequest request) {

        ApiResponse<ChatResponse> apiResponse = ApiResponse.<ChatResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Chat")
                .data(chatService.chat(request))
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Document>> upload(@RequestParam("title") String title, @RequestParam("file") MultipartFile file) throws Exception {
        log.info("TITLE = {}", title);
        log.info("FILE = {}, size={}", file.getOriginalFilename(), file.getSize());
        DocumentUploadRequest request = new DocumentUploadRequest(title, file);
        ApiResponse<Document> apiResponse = ApiResponse.<Document>builder()
                .code(HttpStatus.OK.value())
                .message("Upload")
                .data(chatService.upload(request))
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/file")
    public ResponseEntity<ApiResponse<List<Document>>> getAll() {

        ApiResponse<List<Document>> apiResponse = ApiResponse.<List<Document>>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch")
                .data(chatService.getAll())
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @GetMapping("/upload/{file}/callback/{status}")
    public ResponseEntity<ApiResponse<Void>> webhook(@PathVariable("file") String file, @PathVariable("status") FileUploadStatus status) {
        chatService.handleCallBack(file, status);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Webhook")
                .data(null)
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }
}
