package com.example.hoccode.dto.google;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExchangeTokenRequest {
    private String code;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String grantType;
}