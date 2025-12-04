package com.example.hoccode.dto.mapper;

import com.example.hoccode.dto.request.create.CreateProblemRequest;
import com.example.hoccode.dto.request.update.UpdateContestProblemRequest;
import com.example.hoccode.dto.request.update.UpdateHintRequest;
import com.example.hoccode.dto.request.update.UpdateProblemRequest;
import com.example.hoccode.dto.request.update.UpdateTestcaseRequest;
import com.example.hoccode.dto.response.AdminProblemResponse;
import com.example.hoccode.dto.response.ProblemResponse;
import com.example.hoccode.entity.Problem;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProblemMapper {
    public ProblemResponse toPublicProblemResponse(Problem problem, boolean isPublic) {
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

    public ProblemResponse toProblemResponse(Problem problem, boolean isPublic, Set<Integer> acceptedProblemIds) {
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

    public CreateProblemRequest toCreateProblemRequest(UpdateContestProblemRequest request) {
        return CreateProblemRequest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .timeLimit(request.getTimeLimit())
                .memoryLimit(request.getMemoryLimit())
                .hints(request.getHints())
                .constrains(request.getConstrains())
                .difficulty(request.getDifficulty())
                .followUp(request.getFollowUp())
                .categories(request.getCategories())
                .testCases(request.getTestCases())
                .point(request.getPoint())
                .build();
    }

    public UpdateProblemRequest toUpdateProblemRequest(UpdateContestProblemRequest request, boolean isContestProblem) {
        return UpdateProblemRequest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .timeLimit(request.getTimeLimit())
                .memoryLimit(request.getMemoryLimit())
                .isContestProblem(isContestProblem)
                .hints(!request.getHints().isEmpty()
                        ? request.getHints().stream()
                        .map(h -> UpdateHintRequest.builder()
                                .description(h)
                                .build())
                        .toList()
                        : List.of())
                .constrains(request.getConstrains())
                .difficulty(request.getDifficulty())
                .followUp(request.getFollowUp())
                .testCases(request.getTestCases().isEmpty()
                        ? List.of()
                        : request.getTestCases().stream()
                            .map(t -> UpdateTestcaseRequest.builder()
                                                                .input(t.getInput())
                                                                .expectedOutput(t.getExpectedOutput())
                                                                .isHidden(t.getIsHidden())
                                                                .build()).toList()
                )
                .point(request.getPoint())
                .build();
    }

    public AdminProblemResponse toAdminProblemResponse(Problem problem){
        ProblemResponse base = toProblemResponse(problem, false, new HashSet<>());

        AdminProblemResponse admin = new AdminProblemResponse();
        BeanUtils.copyProperties(base, admin);
        admin.setDeleted(problem.getDeleted());
        return admin;
    }
}
