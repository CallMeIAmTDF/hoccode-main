package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.config.template.RabbitKafkaTemplate;
import com.example.hoccode.dto.SocketMessage;
import com.example.hoccode.dto.rag.ChatRequest;
import com.example.hoccode.dto.rag.ChatResponse;
import com.example.hoccode.dto.rag.FileUploadResponse;
import com.example.hoccode.dto.request.DocumentUploadRequest;
import com.example.hoccode.entity.Document;
import com.example.hoccode.entity.Report;
import com.example.hoccode.event.NotificationEvent;
import com.example.hoccode.repository.ChatMessageRepository;
import com.example.hoccode.repository.DocumentRepository;
import com.example.hoccode.repository.ReportRepository;
import com.example.hoccode.service.IChatService;
import com.example.hoccode.service.ISensitiveWordService;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.WebClientUtil;
import com.example.hoccode.util.constant.FileUploadStatus;
import com.example.hoccode.util.constant.ReportTypeEnum;
import com.example.hoccode.util.constant.SocketType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static com.example.hoccode.util.WebClientUtil.safeCall;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService implements IChatService {
    @Qualifier("gptClient")
    WebClient gptClient;

    ChatMessageRepository chatMessageRepository;
    DocumentRepository documentRepository;
    ReportRepository reportRepository;
    SecurityUtil securityUtil;
    SimpMessagingTemplate messagingTemplate;
    RabbitKafkaTemplate kafkaTemplate;

//    SensitiveWordValidationUtil wordValidationUtil;
    ISensitiveWordService sensitiveWordService;

    @Override
    @Transactional
    public ChatResponse chat(ChatRequest request) {
        String question = request.getQuestion();
        if(sensitiveWordService.contains(question)) {
            String match = sensitiveWordService.findMatchWord(question);

            return ChatResponse.builder()
                    .answer("Nội dung chứa từ bị cấm: " + match)
                    .build();
        }

        if (question.startsWith("/report")) {
            ChatResponse chatResponse = ChatResponse.builder().build();
            String payload = question.substring("/report".length()).trim();
            if (payload.isEmpty()) {
                chatResponse.setAnswer(
                        "Vui lòng điền đầy đủ thông tin báo cáo " +
                                "(/report <tiêu đề>: <nội dung> hoặc /report <nội dung>). \n" +
                                "Ví dụ: /report lỗi giao diện: bảng bị lệch"
                );
                return chatResponse;
            }
            String title = "Báo lỗi";
            String description;

            int colonIndex = payload.indexOf(":");
            if (colonIndex != -1) {
                description = payload.substring(colonIndex + 1).trim();
                if (description.isEmpty()) {
                    chatResponse.setAnswer(
                            "Nội dung báo cáo không được để trống (/report <tiêu đề>: <nội dung> hoặc /report <nội dung>). \n" +
                                    "Ví dụ: /report lỗi giao diện: bảng bị lệch"
                    );
                    return chatResponse;
                }

                String extractedTitle = payload.substring(0, colonIndex).trim();
                if (!extractedTitle.isEmpty()) {
                    title = extractedTitle;
                }
            } else {
                description = payload;
            }

            reportRepository.save(
                    Report.builder()
                            .target("")
                            .type(ReportTypeEnum.SYSTEM)
                            .solved(false)
                            .title(title)
                            .description(description)
                            .build()
            );
            chatResponse.setAnswer("Hệ thống đã ghi nhận báo cáo. Xin cảm ơn!!!");
            return chatResponse;
        }

        try {
            return safeCall(
                    () -> gptClient.post()
                            .uri("/chat")
                            .bodyValue(request)
                            .retrieve()
                            .bodyToMono(ChatResponse.class),
                    "Dịch vụ GPT gặp lỗi, vui lòng thử lại sau"
            );
        } catch (Exception e) {
            log.error("Lỗi khi gọi GPT", e);
            return ChatResponse.builder().answer("Lỗi hệ thống, vui lòng thử lại sau").build();
        }
    }

    @Transactional
    @Override
    public Document upload(DocumentUploadRequest request) {
        if(!securityUtil.isAdmin()){
            throw new BadRequestException("Không có quyền truy cập");
        }
        MultipartFile file = request.getFile();
        FileUploadResponse response = WebClientUtil.safeCall(
                () -> {
                    try {
                        return gptClient.post()
                                .uri("/files")
                                .body(BodyInserters.fromMultipartData("file", new ByteArrayResource(file.getBytes()) {
                                    @Override
                                    public String getFilename() {
                                        return file.getOriginalFilename();
                                    }
                                }))
                                .retrieve()
                                .bodyToMono(FileUploadResponse.class);
                    } catch (IOException e) {
                        throw new BadRequestException("Lỗi đọc file");
                    }
                },
                "Dịch vụ Upload gặp lỗi, vui lòng thử lại sau"
        );

        return documentRepository.save(Document.builder()
                        .title(request.getTitle())
                        .status(response.getStatus())
                        .fileName(response.getFile())
                        .size(response.getSize())
                .build());
    }

    @Override
    public List<Document> getAll(){
        if(!securityUtil.isAdmin()){
            throw new BadRequestException("Không có quyền truy cập");
        }
        return documentRepository.findAll();
    }

    @Transactional
    @Override
    public void handleCallBack(String fileName, FileUploadStatus status){
        Document doc = documentRepository.findFirstByFileName(fileName).orElseThrow(() -> new BadRequestException("File Not Found"));
        if(!FileUploadStatus.RECEIVED.equals(doc.getStatus())){
            return;
        }

        doc.setStatus(status);
        documentRepository.save(doc);
        String message = "File xử lý không thành công. Vui lòng thử lại";
        if(FileUploadStatus.UPLOADED.equals(status)){
            message = "File đã được xử lý thành công.";
        }
        messagingTemplate.convertAndSendToUser(
                doc.getCreatedBy(),
                "/topic/admin",
                new SocketMessage(SocketType.NEW_NOTIFICATION, message,
                        null)
        );

        messagingTemplate.convertAndSendToUser(
                doc.getCreatedBy(),
                "/topic/admin/files",
                new SocketMessage(SocketType.NEW_NOTIFICATION, message,
                        doc)
        );



    }
}
