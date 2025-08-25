package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.mapper.TestCaseMapper;
import com.example.hoccode.dto.projection.IContestLeaderboardProjection;
import com.example.hoccode.dto.request.SubmissionUpdateRequest;
import com.example.hoccode.dto.request.call.CodeRunnerRequest;
import com.example.hoccode.dto.request.create.CreateSubmissionRequest;
import com.example.hoccode.dto.response.contest.ContestLeaderboardEntryDTO;
import com.example.hoccode.dto.response.contest.ContestLeaderboardResponse;
import com.example.hoccode.dto.response.contest.ContestProblemStatusResponse;
import com.example.hoccode.dto.response.pageable.PaginationInfo;
import com.example.hoccode.entity.Contest;
import com.example.hoccode.entity.Problem;
import com.example.hoccode.entity.Submission;
import com.example.hoccode.entity.User;
import com.example.hoccode.repository.*;
import com.example.hoccode.service.ICodeRunnerService;
import com.example.hoccode.service.IStreakService;
import com.example.hoccode.service.ISubmissionService;
import com.example.hoccode.service.redis.IStringStringIntegerRedis;
import com.example.hoccode.util.RandomStringUtil;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.constant.StatusEnum;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubmissionService implements ISubmissionService {

    SubmissionRepository submissionRepository;
    ICodeRunnerService codeRunnerService;
    ProblemRepository problemRepository;
    TestCaseMapper testCaseMapper;
    SecurityUtil securityUtil;
    IStringStringIntegerRedis redis;
    StreakRepository streakRepository;
    UserRepository userRepository;
    IStreakService streakService;
    ContestRepository contestRepository;

    @Override
    @Transactional
    public Submission create(CreateSubmissionRequest createSubmissionRequest){
        Problem p = problemRepository.findById(createSubmissionRequest.getPid()).orElseThrow(() -> new BadRequestException("Pid not found"));
        String id = RandomStringUtil.generateRandomString();
        Submission s = Submission.builder().id(id).pid(createSubmissionRequest.getPid()).code(createSubmissionRequest.getCode()).status(StatusEnum.IN_QUEUE).language(createSubmissionRequest.getLanguage()).message("").passedTestCase(0).totalTestCases(1).maxTime(0).build();
        submissionRepository.save(s);
        CodeRunnerRequest request = CodeRunnerRequest.builder()
                .language(createSubmissionRequest.getLanguage())
                .submissionId(id)
                .memoryLimit(p.getMemoryLimit())
                .problemId(p.getId())
                .timeLimit(p.getTimeLimit())
                .code(createSubmissionRequest.getCode())
                .testcases(p.getAllTestCases().stream().map(testCaseMapper::toCallTestCaseRequest).collect(Collectors.toList()))
                .build();
        codeRunnerService.execute(request);
        return s;
    }

    @Override
    public Map<String, Object> viewResults(String submissionId) {
        Submission submission = submissionRepository.findById(submissionId).orElseThrow(
                () -> new BadRequestException("SubmissionId not found")
        );
        if(!submission.getCreatedBy().equals(securityUtil.getCurrentUserLogin().get())){
            throw new BadRequestException("Insufficient access");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("testcases", codeRunnerService.viewResults(submissionId));
        map.put("detail", submission);
        return map;
    }

    @Override
    public Submission view(String submissionId) {
        Submission submission = submissionRepository.findById(submissionId).orElseThrow(
                () -> new BadRequestException("SubmissionId not found")
        );
        if(!submission.getCreatedBy().equals(securityUtil.getCurrentUserLogin().get())){
            throw new BadRequestException("Insufficient access");
        }
        return submission;
    }

    @Override
    public void handleCallBack(SubmissionUpdateRequest request) {
        Submission submission = submissionRepository.findById(request.getId()).orElseThrow(
                () -> new BadRequestException("SubmissionId not found")
        );

        // chỉ cho problem không phải của contest
        Problem p = problemRepository.findById(submission.getPid()).orElseThrow(() -> new BadRequestException("Pid Not Found"));
        if (!p.getIsContestProblem()){
            if(request.getStatus() == StatusEnum.ACCEPTED){
                // point
                User u = userRepository.findByEmail(submission.getCreatedBy()).orElseThrow(() -> new BadRequestException("Email Not Found"));
                if(!submissionRepository.existsByCreatedByAndPidAndStatus(submission.getCreatedBy(), submission.getPid(), StatusEnum.ACCEPTED)){
                    u.incrementPoint(p.getPoint());
                    userRepository.save(u);
                }
                // streak
                if(Objects.equals(submission.getPid(), redis.getDailyChallenge())){
                    streakService.increment(u.getEmail());
                }
            }
        }

        submission.setMessage(request.getMessage());
        submission.setPassedTestCase(request.getPassedTestCase());
        submission.setTotalTestCases(request.getTotalTestCases());
        submission.setStatus(request.getStatus());
        submission.setMaxTime(request.getMaxTime());

        submissionRepository.save(submission);
    }

    @Override
    public List<Submission> getMySubmission() {
        return submissionRepository.findAllByCreatedBy(securityUtil.getCurrentUserLogin().get());
    }


    @Override
    public ContestLeaderboardResponse getContestLeaderboard(Integer contestId, int page, int size) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new BadRequestException("Contest not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<IContestLeaderboardProjection> leaderboardPage =
                submissionRepository.findContestLeaderboard(contestId, pageable);

        List<ContestLeaderboardEntryDTO> entries = leaderboardPage.getContent().stream()
                .map(projection -> {
                    List<ContestProblemStatusResponse> problemStatuses = submissionRepository
                            .findUserContestProblemStatuses(contestId, projection.getUsername())
                            .stream()
                            .map(status -> ContestProblemStatusResponse.builder()
                                    .problemName(status.getProblemName())
                                    .status(status.getStatus())
                                    .acceptedCount(status.getAcceptedCount() != null ? status.getAcceptedCount() : 0)
                                    .wrongCount(status.getWrongCount() != null ? status.getWrongCount() : 0)
                                    .build())
                            .collect(Collectors.toList());

                    return ContestLeaderboardEntryDTO.builder()
                            .username(projection.getUsername())
                            .totalPoints(projection.getTotalPoints())
                            .problemStatuses(problemStatuses)
                            .build();
                })
                .collect(Collectors.toList());

        if (page == 0 && entries.isEmpty()) {
            List<ContestProblemStatusResponse> emptyProblemStatuses = submissionRepository
                    .findUserContestProblemStatuses(contestId, "")
                    .stream()
                    .map(status -> ContestProblemStatusResponse.builder()
                            .problemName(status.getProblemName())
                            .status("NOT_SUBMITTED")
                            .acceptedCount(0)
                            .wrongCount(0)
                            .build())
                    .collect(Collectors.toList());

            entries = List.of(ContestLeaderboardEntryDTO.builder()
                    .username("No submissions yet")
                    .totalPoints(0)
                    .problemStatuses(emptyProblemStatuses)
                    .build());
        }

        PaginationInfo paginationInfo = PaginationInfo.builder()
                .currentPage(leaderboardPage.getNumber())
                .totalPages(leaderboardPage.getTotalPages())
                .totalElements(leaderboardPage.getTotalElements())
                .pageSize(leaderboardPage.getSize())
                .build();

        return ContestLeaderboardResponse.builder()
                .contestName(contest.getTitle())
                .entries(entries)
                .pagination(paginationInfo)
                .build();
    }


}
