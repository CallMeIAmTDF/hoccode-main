package com.example.hoccode.service;

import com.example.hoccode.dto.request.create.CreateContestRequest;
import com.example.hoccode.dto.response.contest.ContestDetailResponse;
import com.example.hoccode.dto.response.contest.ContestResponse;
import com.example.hoccode.entity.Contest;

import java.util.List;

public interface IContestService {
    Contest create(CreateContestRequest request);

    Contest updateContest(Integer contestId, CreateContestRequest request);

    ContestDetailResponse getContestDetail(Integer contestId);

    List<ContestResponse> getAll();

    ContestDetailResponse edit(Integer contestId);

    void joinContest(Integer contestId, String password);

    void unjoinContest(Integer contestId);

    // get problem in contest
}