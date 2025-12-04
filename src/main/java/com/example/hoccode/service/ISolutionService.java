package com.example.hoccode.service;

import com.example.hoccode.dto.request.create.CreateSolutionRequest;
import com.example.hoccode.entity.Solution;

import java.util.List;

public interface ISolutionService {
//    @Transactional
//    List<Solution> create(List<CreateSolutionRequest> requests);

    Solution create(CreateSolutionRequest request);

    Solution update(CreateSolutionRequest request, Integer solutionId);

    List<Solution> getSolutionsForAdmin(Integer pid);

    List<Solution> getSolutions(Integer pid);

    Solution delete(Integer id);
}
