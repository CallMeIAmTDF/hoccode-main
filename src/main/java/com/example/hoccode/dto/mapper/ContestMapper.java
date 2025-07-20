package com.example.hoccode.dto.mapper;

import com.example.hoccode.dto.response.contest.ContestDetailResponse;
import com.example.hoccode.dto.response.contest.ContestResponse;
import com.example.hoccode.entity.Contest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContestMapper {
    ProblemMapper problemMapper;
    public ContestResponse toContestResponse(Contest contest){
        return ContestResponse.builder()
                .id(contest.getId())
                .title(contest.getTitle())
                .description(contest.getDescription())
                .numAttendees(contest.getNumAttendees())
                .endTime(contest.getEndTime())
                .startTime(contest.getStartTime())
                .hasPassword(contest.getPassword() != null)
                .build();
    }

    public ContestDetailResponse toContestDetailResponse(Contest contest){
        return ContestDetailResponse.builder()
                .id(contest.getId())
                .description(contest.getDescription())
                .endTime(contest.getEndTime())
                .numAttendees(contest.getNumAttendees())
                .password(contest.getPassword())
                .problems(
                    contest.getContestProblems().isEmpty() ? List.of() : contest.getContestProblems().stream().map(cp -> problemMapper.toPublicProblemResponse(cp.getProblem(), false)).collect(Collectors.toList())
                )
                .startTime(contest.getStartTime())
                .title(contest.getTitle())
                .build();
    }
}
