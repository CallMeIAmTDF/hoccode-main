package com.example.hoccode.service;

import com.example.hoccode.advice.exception.IdInvalidException;
import com.example.hoccode.dto.request.create.CreateContestRequest;
import com.example.hoccode.dto.request.update.UpdateContestRequest;
import com.example.hoccode.dto.response.MyContestResponse;
import com.example.hoccode.dto.response.ProblemResponse;
import com.example.hoccode.dto.response.contest.ContestDetailResponse;
import com.example.hoccode.dto.response.contest.ContestResponse;
import com.example.hoccode.entity.Contest;

import java.util.List;
import java.util.Map;

public interface IContestService {
    Contest create(CreateContestRequest request);

    Contest updateContest(Integer contestId, UpdateContestRequest request) throws IdInvalidException;

    ContestDetailResponse getContestDetail(Integer contestId);

    List<ContestResponse> getAll();

    ContestDetailResponse edit(Integer contestId);

    void joinContest(Integer contestId, String password);

    void unjoinContest(Integer contestId);

    boolean hasUserJoinedContest(Integer contestId);

    ProblemResponse problemDetailForContest(Integer id, Integer contestId);

    boolean isEditable(Integer contestId);

    List<MyContestResponse> getMyContests();

    Map<String, Object> getContestStats(Integer contestId);

    List<String> getAllContestParticipant(Integer contestId);

    // get problem in contest
}