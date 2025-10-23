package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.mapper.TestCaseMapper;
import com.example.hoccode.dto.projection.IContestLeaderboardProjection;
import com.example.hoccode.dto.record.DailySubmission;
import com.example.hoccode.dto.record.DifficultyStats;
import com.example.hoccode.dto.record.ProblemStats;
import com.example.hoccode.dto.request.SubmissionUpdateRequest;
import com.example.hoccode.dto.request.call.CodeRunnerRequest;
import com.example.hoccode.dto.request.create.CreateSubmissionRequest;
import com.example.hoccode.dto.response.SubmissionCategoryCount;
import com.example.hoccode.dto.response.SubmissionProblemResponse;
import com.example.hoccode.dto.response.call.CodeRunnerResponse;
import com.example.hoccode.dto.response.contest.ContestLeaderboardEntryDTO;
import com.example.hoccode.dto.response.contest.ContestLeaderboardResponse;
import com.example.hoccode.dto.response.contest.ContestProblemStatusResponse;
import com.example.hoccode.entity.*;
import com.example.hoccode.repository.*;
import com.example.hoccode.service.ICodeRunnerService;
import com.example.hoccode.service.IStreakService;
import com.example.hoccode.service.ISubmissionService;
import com.example.hoccode.service.redis.IStringStringIntegerRedis;
import com.example.hoccode.util.RandomStringUtil;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.constant.DifficultyEnum;
import com.example.hoccode.util.constant.StatusEnum;
import jakarta.transaction.Transactional;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
    ContestParticipantRepository contestParticipantRepository;


    @Override
    @Transactional
    public Submission create(CreateSubmissionRequest createSubmissionRequest){
        Problem p = problemRepository.findByIdAndDeletedFalse(createSubmissionRequest.getPid()).orElseThrow(() -> new BadRequestException("Pid not found"));
        String id = RandomStringUtil.generateRandomString();

        // submission from contest
        if(p.getIsContestProblem()){
            User user = userRepository.findByEmail(securityUtil.getCurrentUserLogin().get()).orElseThrow(() -> new BadRequestException("Email not found"));
            Contest contest = p.getContestProblem().getContest();
            // user khong thuoc contest
            if(!contestParticipantRepository.existsByContestIdAndUserId(contest.getId(), user.getId())){
                throw new BadRequestException("Bạn không tham gia cuộc thi này");
            }

            // submit qua thoi gian
            // submit som hon

            LocalDateTime now = LocalDateTime.now();

            if (now.isBefore(contest.getStartTime())) {
                throw new BadRequestException("Cuộc thi vẫn chưa bắt đầu");
            }

            if (now.isAfter(contest.getEndTime())) {
                throw new BadRequestException("Cuộc thi đã kết thúc");
            }

        }

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
                () -> new BadRequestException("Không tìm thấy SubmitId")
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
                () -> new BadRequestException("Không tìm thấy SubmitId")
        );
        if(!submission.getCreatedBy().equals(securityUtil.getCurrentUserLogin().get())){
            throw new BadRequestException("Insufficient access");
        }
        return submission;
    }

    @Override
    public void handleCallBack(SubmissionUpdateRequest request) {
        Submission submission = submissionRepository.findById(request.getId()).orElseThrow(
                () -> new BadRequestException("Không tìm thấy SubmitId")
        );

        System.out.println("submissionId: " + submission.getId());

        // chỉ cho problem không phải của contest
        Problem p = problemRepository.findByIdAndDeletedFalse(submission.getPid()).orElseThrow(() -> new BadRequestException("Pid Not Found"));
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
                    System.out.println("streak ++");
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
        Contest contest = contestRepository.findFirstByIdAndDeletedFalse(contestId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy cuộc thi"));

//        Pageable pageable = PageRequest.of(page, size);
//        Page<IContestLeaderboardProjection> leaderboardPage =
//                submissionRepository.findContestLeaderboard(contestId, pageable);

        List<IContestLeaderboardProjection> leaderboard = submissionRepository.findContestLeaderboardNoPage(contestId);


        Map<String, IContestLeaderboardProjection> submissionMap =
                leaderboard.stream()
                        .collect(Collectors.toMap(IContestLeaderboardProjection::getUsername, p -> p));

        List<User> participants = contest.getParticipants().stream()
                .map(ContestParticipant::getUser)
                .toList();

        List<Problem> problems = contest.getContestProblems().stream()
                .map(ContestProblem::getProblem)
                .toList();

        List<ContestLeaderboardEntryDTO> entries = participants.stream()
                .map(user -> {
                    IContestLeaderboardProjection projection = submissionMap.get(user.getEmail());
                    if (projection != null) {
                        List<ContestProblemStatusResponse> problemStatuses = submissionRepository
                                .findUserContestProblemStatuses(contestId, projection.getUsername())
                                .stream()
                                .map(status -> ContestProblemStatusResponse.builder()
                                        .problemName(status.getProblemName())
                                        .status(status.getAcceptedCount() != null ? "ACCEPTED" : "UNACCEPTED")
                                        .acceptedCount(status.getAcceptedCount() != null ? status.getAcceptedCount() : 0)
                                        .wrongCount(status.getWrongCount() != null ? status.getWrongCount() : 0)
                                        .build())
                                .collect(Collectors.toList());
                        return ContestLeaderboardEntryDTO.builder()
                                .username(user.getName())
                                .totalPoints(projection.getTotalPoints())
                                .problemStatuses(problemStatuses)
                                .build();
                    } else {
                        List<ContestProblemStatusResponse> problemStatuses = problems.stream()
                                .map(problem -> ContestProblemStatusResponse.builder()
                                        .problemName(problem.getTitle())
                                        .status("NOT_SUBMITTED")
                                        .acceptedCount(0)
                                        .wrongCount(0)
                                        .build())
                                .collect(Collectors.toList());

                        return ContestLeaderboardEntryDTO.builder()
                                .username(user.getName())
                                .totalPoints(0)
                                .problemStatuses(problemStatuses)
                                .build();
                    }
                })
                .collect(Collectors.toList());

//        PaginationInfo paginationInfo = PaginationInfo.builder()
//                .currentPage(leaderboardPage.getNumber())
//                .totalPages(leaderboardPage.getTotalPages())
//                .totalElements(leaderboardPage.getTotalElements())
//                .pageSize(leaderboardPage.getSize())
//                .build();

        return ContestLeaderboardResponse.builder()
                .contestName(contest.getTitle())
                .entries(entries)
                .pagination(null)
                .build();
    }

    @Override
    public CodeRunnerResponse run(CreateSubmissionRequest request, boolean isPublic) {
        Problem p = problemRepository.findByIdAndDeletedFalse(request.getPid()).orElseThrow(() -> new BadRequestException("Pid not found"));
        List<TestCase> casesToRun;
        if (isPublic) {
            casesToRun = p.getTestCases();
        } else {
            casesToRun = p.getAllTestCases();
        }
        CodeRunnerRequest req = CodeRunnerRequest.builder()
                .language(request.getLanguage())
                .submissionId("IAMTDF")
                .memoryLimit(p.getMemoryLimit())
                .problemId(p.getId())
                .timeLimit(p.getTimeLimit())
                .code(request.getCode())
                .testcases(casesToRun.stream().map(testCaseMapper::toCallTestCaseRequest).collect(Collectors.toList()))
                .build();
        return codeRunnerService.run(req);
    }

    @Override
    public Map<LocalDate, Integer> getAcceptedCountLastYear() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.withDayOfYear(1);   // 01-01 năm nay
        LocalDate endDate = today.withMonth(12).withDayOfMonth(31);


        List<Object[]> results = submissionRepository.countAcceptedByDay(
                securityUtil.getCurrentUserLogin().get(),
                startDate.atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );
        Map<LocalDate, Integer> dbMap = results.stream()
                .collect(Collectors.toMap(
                        r -> ((Date) r[0]).toLocalDate(),
                        r -> ((Number) r[1]).intValue()
                ));
        Map<LocalDate, Integer> finalMap = new LinkedHashMap<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            finalMap.put(d, dbMap.getOrDefault(d, 0));
        }
        return finalMap;
    }

    @Override
    public List<SubmissionCategoryCount> getSubmissonCategoryCount(){
        return submissionRepository.countAcceptedByCategory(securityUtil.getCurrentUserLogin().get());
    }

    public ProblemStats getProblemStats() {
        List<Object[]> solvedList = submissionRepository.countSolvedByDifficulty(securityUtil.getCurrentUserLogin().get());
        List<Object[]> totalList = problemRepository.countTotalByDifficulty();

        Map<DifficultyEnum, Integer> solvedMap = solvedList.stream()
                .collect(Collectors.toMap(
                        row -> (DifficultyEnum) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        Map<DifficultyEnum, Integer> totalMap = totalList.stream()
                .collect(Collectors.toMap(
                        row -> (DifficultyEnum) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        Map<String, DifficultyStats> diffStats = new HashMap<>();
        for (DifficultyEnum diff : DifficultyEnum.values()) {
            diffStats.put(diff.name().toLowerCase(), new DifficultyStats(
                    solvedMap.getOrDefault(diff, 0),
                    totalMap.getOrDefault(diff, 0)
            ));
        }

        int totalSolved = solvedMap.values().stream().mapToInt(Integer::intValue).sum();
        int totalProblems = totalMap.values().stream().mapToInt(Integer::intValue).sum();

        return new ProblemStats(totalSolved, totalProblems, diffStats);
    }

    @Override
    public List<DailySubmission> getDailySubmissions() {
        List<Object[]> rows = submissionRepository.countSubmissionLast10Days(securityUtil.getCurrentUserLogin().get());
        return rows.stream()
                .map(r -> new DailySubmission(r[0].toString(), (Long) r[1]))
                .toList();
    }


    @Override
    public List<SubmissionProblemResponse> getAllContestSubmission(Integer contestId){
        return submissionRepository.findAllByContestId(contestId);
    }
}



