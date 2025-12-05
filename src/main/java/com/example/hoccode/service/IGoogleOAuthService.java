package com.example.hoccode.service;

import com.example.hoccode.dto.google.ExchangeTokenResponse;
import com.example.hoccode.dto.google.GoogleUserResponse;
import com.example.hoccode.dto.response.AuthenticationResponse;
import com.example.hoccode.entity.User;

public interface IGoogleOAuthService {
    ExchangeTokenResponse exchangeCodeForToken(String code);

    GoogleUserResponse getUserInfo(String accessToken);

    User getOrCreateUser(GoogleUserResponse googleUser);

    AuthenticationResponse handleGoogleLogin(String code);
}
