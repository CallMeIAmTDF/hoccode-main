package com.example.hoccode.service;

import com.example.hoccode.dto.request.SubmissionUpdateRequest;
import com.example.hoccode.dto.request.create.CreateSubmissionRequest;
import com.example.hoccode.dto.request.call.TestCaseSubmissionResponse;
import com.example.hoccode.dto.response.contest.ContestLeaderboardResponse;
import com.example.hoccode.entity.Submission;

import java.util.List;
import java.util.Map;

public interface ISubmissionService {
    Submission create(CreateSubmissionRequest createSubmissionRequest);

    Map<String, Object> viewResults(String submissionId);

    Submission view(String submissionId);

    void handleCallBack(SubmissionUpdateRequest submissionUpdateRequest);

    List<Submission> getMySubmission();

    public ContestLeaderboardResponse getContestLeaderboard(Integer contestId, int page, int size);
}
