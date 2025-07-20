package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.mapper.ContestMapper;
import com.example.hoccode.dto.request.create.CreateContestRequest;
import com.example.hoccode.dto.response.contest.ContestDetailResponse;
import com.example.hoccode.dto.response.contest.ContestResponse;
import com.example.hoccode.entity.*;
import com.example.hoccode.repository.ContestParticipantRepository;
import com.example.hoccode.repository.ContestRepository;
import com.example.hoccode.repository.UserRepository;
import com.example.hoccode.service.IContestService;
import com.example.hoccode.service.IProblemService;
import com.example.hoccode.util.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    public Contest updateContest(Integer contestId, CreateContestRequest request) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new BadRequestException("Contest not found with id: " + contestId));

        if(!contest.getCreatedBy().equals(securityUtil.getCurrentUserLogin().get()))
            throw new BadRequestException("Access Denied");

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
        contest.getContestProblems().clear();
        List<ContestProblem> newContestProblems = request.getProblems().stream()
                .map(problemRequest -> {
                    Problem createdProblem = problemService.create(problemRequest, true);
                    return ContestProblem.builder()
                            .contest(contest)
                            .problem(createdProblem)
                            .build();
                })
                .toList();
        contest.setContestProblems(newContestProblems);
        return contestRepository.save(contest);
    }

    @Override
    public ContestDetailResponse getContestDetail(Integer contestId){
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new BadRequestException("Contest not found with id: " + contestId));
        if(LocalDateTime.now().isBefore(contest.getStartTime())){
            contest.setContestProblems(List.of());
        }
        return contestMapper.toContestDetailResponse(contest);
    }

    @Override
    public List<ContestResponse> getAll() {
        return contestRepository.findAllSortedByTimePriority().stream().map(contestMapper::toContestResponse).collect(Collectors.toList());
    }

    @Override
    public ContestDetailResponse edit(Integer contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new BadRequestException("Contest not found with id: " + contestId));

        if(!contest.getCreatedBy().equals(securityUtil.getCurrentUserLogin().get()))
            throw new BadRequestException("Access Denied");

        return contestMapper.toContestDetailResponse(contest);
    }

    @Override
    public void joinContest(Integer contestId, String password) {
        String email = securityUtil.getCurrentUserLogin().get();

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new BadRequestException("Contest not found"));

        User user = userRepository.findById(email)
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

        ContestParticipant participant = ContestParticipant.builder()
                .contest(contest)
                .user(user)
                .build();

        contestParticipantRepository.save(participant);
    }

    @Override
    public void unjoinContest(Integer contestId) {
        String email = securityUtil.getCurrentUserLogin().get();
        User user = userRepository.findById(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        if (!contestParticipantRepository.existsByContestIdAndUserId(contestId, user.getId())) {
            throw new BadRequestException("You haven't joined this contest");
        }
        contestParticipantRepository.deleteByContestIdAndUserId(contestId, user.getId());
    }

}
