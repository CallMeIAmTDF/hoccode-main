package com.example.hoccode.service;

import com.example.hoccode.dto.response.PlagiarismDetailResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface IPlagiarismService {
    List<PlagiarismDetailResponse> plagiarismChecking(Integer contestId) throws JsonProcessingException;

    void callBack(Integer contestId);

    Boolean isPlagiarismChecked(Integer contestId);
}
