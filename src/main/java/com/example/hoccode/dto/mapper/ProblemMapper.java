package com.example.hoccode.dto.mapper;

import com.example.hoccode.dto.response.ProblemResponse;
import com.example.hoccode.entity.Problem;
import com.example.hoccode.repository.ProblemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProblemMapper {
    public ProblemResponse toPublicProblemResponse(Problem problem, boolean isPublic){
        return ProblemResponse.builder()
                .id(problem.getId())
                .hints(problem.getHints())
                .allTestCases(isPublic ? problem.getTestCases() : problem.getAllTestCases())
                .point(problem.getPoint())
                .categories(problem.getCategories())
                .constrains(problem.getConstrains())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty())
                .followUp(problem.getFollowUp())
                .memoryLimit(problem.getMemoryLimit())
                .timeLimit(problem.getTimeLimit())
                .title(problem.getTitle())
                .failCount(0L)
                .passCount(0L)
                .build();
    }

    public ProblemResponse toProblemResponse(Problem problem, boolean isPublic, Set<Integer> acceptedProblemIds){
        ProblemResponse.ProblemResponseBuilder builder = ProblemResponse.builder()
                .id(problem.getId())
                .hints(problem.getHints())
                .allTestCases(isPublic ? problem.getTestCases() : problem.getAllTestCases())
                .point(problem.getPoint())
                .constrains(problem.getConstrains())
                .description(problem.getDescription())
                .categories(problem.getCategories())
                .difficulty(problem.getDifficulty())
                .followUp(problem.getFollowUp())
                .memoryLimit(problem.getMemoryLimit())
                .timeLimit(problem.getTimeLimit())
                .title(problem.getTitle());
        if (acceptedProblemIds != null) {
            builder.accepted(acceptedProblemIds.contains(problem.getId()));
        }
        return builder.build();
    }
}
