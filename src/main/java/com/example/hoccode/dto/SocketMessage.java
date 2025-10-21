package com.example.hoccode.dto;

import com.example.hoccode.util.constant.SocketType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class SocketMessage {
        private SocketType type;
        private String message;
        private Object data;
    }