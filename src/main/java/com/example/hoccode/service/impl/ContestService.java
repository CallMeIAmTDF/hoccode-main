package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.advice.exception.IdInvalidException;
import com.example.hoccode.dto.mapper.ContestMapper;
import com.example.hoccode.dto.mapper.ProblemMapper;
import com.example.hoccode.dto.request.create.CreateContestRequest;
import com.example.hoccode.dto.request.update.UpdateContestProblemRequest;
import com.example.hoccode.dto.request.update.UpdateContestRequest;
import com.example.hoccode.dto.response.MyContestResponse;
import com.example.hoccode.dto.response.ProblemResponse;
import com.example.hoccode.dto.response.ProblemStatsResponse;
import com.example.hoccode.dto.response.RecentSubmissionResponse;
import com.example.hoccode.dto.response.contest.ContestDetailResponse;
import com.example.hoccode.dto.response.contest.ContestResponse;
import com.example.hoccode.entity.*;
import com.example.hoccode.repository.ContestParticipantRepository;
import com.example.hoccode.repository.ContestRepository;
import com.example.hoccode.repository.SubmissionRepository;
import com.example.hoccode.repository.UserRepository;
import com.example.hoccode.service.IContestService;
import com.example.hoccode.service.IProblemService;
import com.example.hoccode.service.ISubmissionService;
import com.example.hoccode.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContestService implements IContestService {
    ContestRepository contestRepository;
    IProblemService problemService;
    ContestMapper contestMapper;
    SecurityUtil securityUtil;
    ContestParticipantRepository contestParticipantRepository;
    UserRepository userRepository;
    ProblemMapper problemMapper;
    SubmissionRepository submissionRepository;
    ISubmissionService submissionService;


    @Override
    public Contest create(CreateContestRequest request) {
        Contest contest = Contest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .password(
                        StringUtils.hasText(request.getPassword())
                                ? request.getPassword()
                                : null
                )
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .numAttendees(Optional.ofNullable(request.getNumAttendees())
                        .filter(n -> n > 0)
                        .orElse(999999))
                .build();

        List<ContestProblem> contestProblems = request.getProblems().stream()
                .map(problemRequest -> ContestProblem.builder().problem(problemService.create(problemRequest, true)).contest(contest).build())
                .toList();

        contest.setContestProblems(contestProblems);
        return contestRepository.save(contest);
    }

    @Override
    public Contest updateContest(Integer contestId, UpdateContestRequest request) throws IdInvalidException {
        Contest contest = contestRepository.findFirstByIdAndDeletedFalse(contestId)
                .orElseThrow(() -> new BadRequestException("Contest not found with id: " + contestId));

        if(!contest.getCreatedBy().equals(securityUtil.getCurrentUserLogin().get()))
            throw new BadRequestException("Access Denied");

        if(request.getNumAttendees() != null && contest.getParticipants().size() > request.getNumAttendees()){
            throw new BadRequestException("Cannot decrease number of attendees");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean contestStarted = contest.getStartTime() != null && now.isAfter(contest.getStartTime());

        // --- Update metadata ---
        if(!contestStarted){
            contest.setTitle(request.getTitle());
            contest.setDescription(request.getDescription());
            contest.setPassword(StringUtils.hasText(request.getPassword()) ? request.getPassword() : null);
            contest.setStartTime(request.getStartTime());
            contest.setEndTime(request.getEndTime());
            contest.setNumAttendees(
                    Optional.ofNullable(request.getNumAttendees())
                            .filter(n -> n > 0)
                            .orElse(999999)
            );
        }

        // --- Update problems ---
        Map<Integer, ContestProblem> existingMap = contest.getContestProblems().stream()
                .collect(Collectors.toMap(cp -> cp.getProblem().getId(), cp -> cp));

        List<ContestProblem> updatedProblems = new ArrayList<>();

        if (request.getProblems() != null) {
            for (UpdateContestProblemRequest problemReq : request.getProblems()) {
                if (problemReq.getPid() != null && existingMap.containsKey(problemReq.getPid())) {
                    ContestProblem existingCp = existingMap.get(problemReq.getPid());
                    if(existingCp != null){
                        Problem problem = existingMap.get(problemReq.getPid()).getProblem();

                            problemService.update(problem.getId(),
                                    problemMapper.toUpdateProblemRequest(problemReq, true));
                        updatedProblems.add(existingMap.get(problemReq.getPid()));
                    }
                } else {
                    if (contestStarted) {
                        throw new BadRequestException("Cannot add new problem after contest started");
                    }
                    Problem newProblem = problemService.create(
                            problemMapper.toCreateProblemRequest(problemReq), true);
                    ContestProblem newCp = ContestProblem.builder()
                            .contest(contest)
                            .problem(newProblem)
                            .build();
                    updatedProblems.add(newCp);
                }
            }
            contest.setContestProblems(updatedProblems);
        }
        return contestRepository.save(contest);
    }

//    @Override
//    public ContestDetailResponse getContestDetail(Integer contestId){
//        Contest contest = contestRepository.findFirstByIdAndDeletedFalse(contestId)
//                .orElseThrow(() -> new BadRequestException("Contest not found with id: " + contestId));
//        if(LocalDateTime.now().isBefore(contest.getStartTime())){
//            contest.setContestProblems(List.of());
//        }
//        return contestMapper.toContestDetailResponse(contest, false);
//    }

    @Override
    public ContestDetailResponse getContestDetail(Integer contestId) {
        Contest contest = contestRepository.findFirstByIdAndDeletedFalse(contestId)
                .orElseThrow(() -> new BadRequestException("Contest not found with id: " + contestId));

        if (LocalDateTime.now().isBefore(contest.getStartTime())) {
            contest.setContestProblems(List.of());
        }

        List<Integer> problemIds = contest.getContestProblems().stream()
                .map(cp -> cp.getProblem().getId())
                .toList();

        Set<Integer> finalAcceptedIds = problemService.getListAcceptedId();
        Map<String, Map<Integer, Long>> stats = problemService.getProblemStats(problemIds);
        Map<Integer, Long> passCountMap = stats.getOrDefault("pass", Map.of());
        Map<Integer, Long> failCountMap = stats.getOrDefault("fail", Map.of());

        List<ProblemResponse> problemResponses = contest.getContestProblems().stream()
                .map(cp -> {
                    Problem problem = cp.getProblem();
                    ProblemResponse response = problemMapper.toProblemResponse(problem, true, finalAcceptedIds);
                    response.setPassCount(passCountMap.getOrDefault(problem.getId(), 0L));
                    response.setFailCount(failCountMap.getOrDefault(problem.getId(), 0L));
                    return response;
                })
                .toList();

        return ContestDetailResponse.builder()
                .id(contest.getId())
                .description(contest.getDescription())
                .endTime(contest.getEndTime())
                .numAttendees(contest.getParticipants().size())
                .password(null)
                .problems(problemResponses)
                .startTime(contest.getStartTime())
                .title(contest.getTitle())
                .hasPassword(contest.getPassword() != null)
                .build();
    }

    @Override
    public List<ContestResponse> getAll() {
        return contestRepository.findAllSortedByTimePriority().stream().map(contestMapper::toContestResponse).collect(Collectors.toList());
    }

    @Override
    public ContestDetailResponse edit(Integer contestId) {
        Contest contest = contestRepository.findFirstByIdAndDeletedFalse(contestId)
                .orElseThrow(() -> new BadRequestException("Contest not found with id: " + contestId));

        if(!contest.getCreatedBy().equals(securityUtil.getCurrentUserLogin().get()))
            throw new BadRequestException("Access Denied");

        return contestMapper.toContestDetailResponse(contest);
    }

    @Override
    public void joinContest(Integer contestId, String password) {
        String email = securityUtil.getCurrentUserLogin().get();

        Contest contest = contestRepository.findFirstByIdAndDeletedFalse(contestId)
                .orElseThrow(() -> new BadRequestException("Contest not found"));


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (contestParticipantRepository.existsByContestIdAndUserId(contestId, user.getId())) {
            throw new BadRequestException("You have already joined this contest");
        }

        long currentParticipants = contestParticipantRepository.countByContestId(contestId);
        if (currentParticipants >= contest.getNumAttendees()) {
            throw new BadRequestException("Contest has reached maximum number of participants");
        }

        if (StringUtils.hasText(contest.getPassword())) {
            if (!StringUtils.hasText(password) || !contest.getPassword().equals(password)) {
                throw new BadRequestException("Invalid contest password");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        if (contest.getEndTime().isBefore(now)) {
            throw new BadRequestException("Contest has already ended");
        }

        ContestParticipant participant = ContestParticipant.builder()
                .contest(contest)
                .user(user)
                .build();

        contestParticipantRepository.save(participant);
    }

    @Override
    @Transactional
    public void unjoinContest(Integer contestId) {
        String email = securityUtil.getCurrentUserLogin().get();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        if (!contestParticipantRepository.existsByContestIdAndUserId(contestId, user.getId())) {
            throw new BadRequestException("You haven't joined this contest");
        }
        contestParticipantRepository.deleteByContestIdAndUserId(contestId, user.getId());
    }

    @Override
    public boolean hasUserJoinedContest(Integer contestId){
        User user = userRepository.findByEmail(securityUtil.getCurrentUserLogin().get()).orElseThrow(() -> new BadRequestException("User not found"));
        return contestParticipantRepository.existsByContestIdAndUserId(contestId, user.getId());
    }

    @Override
    public ProblemResponse problemDetailForContest(Integer id, Integer contestId) {
        Contest contest = contestRepository.findFirstById(contestId);
        if(contest == null)
            throw new BadRequestException("Contest not found");

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(contest.getStartTime())) {
            throw new BadRequestException("Contest has not started yet");
        }

        if (now.isAfter(contest.getEndTime())) {
            throw new BadRequestException("Contest has already ended");
        }

        User user = userRepository.findByEmail(securityUtil.getCurrentUserLogin().get())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if(!contestParticipantRepository.existsByContestIdAndUserId(contestId, user.getId())){
            throw new BadRequestException("You haven't joined this contest");
        }

        return problemService.problemDetailForContest(id, contestId);
    }

    @Override
    public boolean isEditable(Integer contestId){
        Contest contest = contestRepository.findFirstByIdAndDeletedFalse(contestId)
                .orElseThrow(() -> new BadRequestException("Contest not found with id: " + contestId));

        if(!contest.getCreatedBy().equals(securityUtil.getCurrentUserLogin().get()))
            return false;

        return contest.getEndTime() == null || !contest.getEndTime().isBefore(LocalDateTime.now());
    }

    @Override
    public List<MyContestResponse> getMyContests() {
        List<Contest> contests = contestRepository.findByCreatedByOrderByCreatedAtDesc(securityUtil.getCurrentUserLogin().get());

        Map<Integer, Boolean> map = contests.stream()
                .collect(Collectors.toMap(
                        Contest::getId,
                        contest -> contest.getEndTime() == null
                                || !contest.getEndTime().isBefore(LocalDateTime.now())
                ));

        return contests.stream()
                .map(contest -> contestMapper.toMyContestResponse(contest, map))
                .toList();
    }

    @Override
    public Map<String, Object> getContestStats(Integer contestId){
        Contest contest = contestRepository.findFirstById(contestId);
        if(contest == null){
            throw new BadRequestException("Contest not found with id: " + contestId);
        }

        if(!contest.getCreatedBy().equals(securityUtil.getCurrentUserLogin().get())){
            throw new BadRequestException("Access Denied");
        }

        Map<String, Object> stats = new HashMap<>();

        List<Object[]> rows = contestRepository.findProblemStatsByContestId(contestId);
        List<Object[]> recents = submissionRepository.findRecentSubmissions(contestId, 5);
        stats.put("contest", contest);

        stats.put("countSubmissionByProblem", rows.stream().map(row -> new ProblemStatsResponse(
                ((Number) row[0]).intValue(),   // problemId
                (String) row[1],                // title
                (String) row[2],                // difficulty
                ((Number) row[3]).longValue(),  // submissionCount
                ((Number) row[4]).longValue(),  // solvedCount
                row[6] != null ? ((Number) row[6]).doubleValue() : 0.0, // averageScore
                row[5] != null ? ((Number) row[5]).doubleValue() : 0.0
        )).collect(Collectors.toList()));


        Map<String, Object> activity = new HashMap<>();
        activity.put("recentSubmissions", recents.stream().map(row -> new RecentSubmissionResponse(
                (String) row[0],  // username
                (String) row[1],  // problemName
                (String) row[2],  // status
                row[3] instanceof Timestamp ts ? ts.toLocalDateTime() : (LocalDateTime) row[3]
        )).collect(Collectors.toList()));
        stats.put("contestActivity", activity);

        stats.put("leaderboard", submissionService.getContestLeaderboard(contestId, 0, 0));
        return stats;
    }

    @Override
    public List<String> getAllContestParticipant(Integer contestId){
        Contest contest = contestRepository.findFirstById(contestId);
        if(contest == null){
            throw new BadRequestException("Contest not found with id: " + contestId);
        }

        if(!contest.getCreatedBy().equals(securityUtil.getCurrentUserLogin().get())){
            throw new BadRequestException("Access Denied");
        }

        List<ContestParticipant> cp = contestParticipantRepository.findByContest(contest);

        if(cp == null || cp.isEmpty()){
            return List.of();
        }

        return cp.stream().map(c -> c.getUser().getEmail()).toList();
    }
}
