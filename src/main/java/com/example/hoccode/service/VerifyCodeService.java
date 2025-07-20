package com.example.hoccode.service;

import com.example.hoccode.dto.request.VerifyNewPasswordRequest;
import com.example.hoccode.entity.User;
import com.example.hoccode.entity.VerificationCode;

public interface VerifyCodeService {

     void delete (VerificationCode verificationCode);

     VerificationCode findByEmail(String email);

     VerificationCode save(VerificationCode verificationCode);

     Boolean isTimeOutRequired(VerificationCode verificationCode, long ms);

     User verifyRegister(String code);

     User verifyForgotPassword(VerifyNewPasswordRequest request);
}