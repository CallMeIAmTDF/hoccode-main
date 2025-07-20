package com.example.hoccode.service;

import com.example.hoccode.dto.request.call.CodeRunnerRequest;
import com.example.hoccode.dto.request.call.TestCaseSubmissionResponse;

import java.util.List;

public interface ICodeRunnerService {
    void execute(CodeRunnerRequest request);

    List<TestCaseSubmissionResponse> viewResults(String submissionId);
}
