package com.example.hoccode.service;

import com.example.hoccode.dto.rag.ChatRequest;
import com.example.hoccode.dto.rag.ChatResponse;
import com.example.hoccode.dto.request.DocumentUploadRequest;
import com.example.hoccode.entity.Document;
import com.example.hoccode.util.constant.FileUploadStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IChatService {
    ChatResponse chat(ChatRequest request);

    @Transactional
    Document upload(DocumentUploadRequest request) throws Exception;

    List<Document> getAll();

    @Transactional
    void handleCallBack(String fileName, FileUploadStatus status);
}
