package com.example.hoccode.service;

import com.example.hoccode.dto.record.DailySubmission;
import com.example.hoccode.dto.record.ProblemStats;
import com.example.hoccode.dto.request.SubmissionUpdateRequest;
import com.example.hoccode.dto.request.create.CreateSubmissionRequest;
import com.example.hoccode.dto.response.MySubmissionResponse;
import com.example.hoccode.dto.response.SubmissionCategoryCount;
import com.example.hoccode.dto.response.SubmissionProblemResponse;
import com.example.hoccode.dto.response.call.CodeRunnerResponse;
import com.example.hoccode.dto.response.contest.ContestLeaderboardResponse;
import com.example.hoccode.entity.Submission;
import com.example.hoccode.util.constant.StatusEnum;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ISubmissionService {
    Submission create(CreateSubmissionRequest createSubmissionRequest);

    Map<String, Object> viewResults(String submissionId);

    Submission view(String submissionId);

    void handleCallBack(SubmissionUpdateRequest submissionUpdateRequest);

    Page<MySubmissionResponse> getMySubmission(
            Integer page,
            Integer size,
            Integer pid,
            Boolean isContest,
            StatusEnum status
    );

    public ContestLeaderboardResponse getContestLeaderboard(Integer contestId, int page, int size);

    public CodeRunnerResponse run(CreateSubmissionRequest request, boolean isPublic);

    Map<LocalDate, Integer> getAcceptedCountLastYear();

    List<SubmissionCategoryCount> getSubmissonCategoryCount();

    ProblemStats getProblemStats();

    List<DailySubmission> getDailySubmissions();

    List<SubmissionProblemResponse> getAllContestSubmission(Integer contestId);
}
