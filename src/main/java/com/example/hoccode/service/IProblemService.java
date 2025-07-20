package com.example.hoccode.service;

import com.example.hoccode.advice.exception.IdInvalidException;
import com.example.hoccode.dto.request.ProblemSearchRequest;
import com.example.hoccode.dto.request.create.CreateProblemRequest;
import com.example.hoccode.dto.request.update.UpdateProblemRequest;
import com.example.hoccode.dto.response.CategoryCountResponse;
import com.example.hoccode.dto.response.ProblemResponse;
import com.example.hoccode.entity.Problem;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IProblemService {
    public Problem create(CreateProblemRequest createProblemRequest, boolean isContest);

    //    List<ProblemResponse> getAll();
    Page<ProblemResponse> getAll(ProblemSearchRequest request);

    ProblemResponse update(Integer id, UpdateProblemRequest updateProblemRequest) throws IdInvalidException;

    ProblemResponse getDailyProblem();

    List<CategoryCountResponse> getCategoryCounts();

}
