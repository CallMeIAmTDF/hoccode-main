package com.example.hoccode.dto.mapper;

import com.example.hoccode.dto.response.MyContestResponse;
import com.example.hoccode.dto.response.contest.ContestDetailResponse;
import com.example.hoccode.dto.response.contest.ContestResponse;
import com.example.hoccode.entity.Contest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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
//                .numAttendees(contest.getNumAttendees())
                .numAttendees(contest.getParticipants().size())
                .endTime(contest.getEndTime())
                .startTime(contest.getStartTime())
                .hasPassword(contest.getPassword() != null)
                .build();
    }

    public ContestDetailResponse toContestDetailResponse(Contest contest){
        return toContestDetailResponse(contest, true);
    }

    public ContestDetailResponse toContestDetailResponse(Contest contest, boolean showPassword) {
        return ContestDetailResponse.builder()
                .id(contest.getId())
                .description(contest.getDescription())
                .endTime(contest.getEndTime())
                .numAttendees(contest.getParticipants().size())
//                .numAttendees(contest.getNumAttendees())
                .password(showPassword ? contest.getPassword() : null)
                .problems(
                        contest.getContestProblems().isEmpty() ? List.of() : contest.getContestProblems().stream().map(cp -> problemMapper.toPublicProblemResponse(cp.getProblem(), true)).collect(Collectors.toList())
                )
                .startTime(contest.getStartTime())
                .title(contest.getTitle())
                .hasPassword(contest.getPassword() != null)
                .build();
    }

    public MyContestResponse toMyContestResponse(Contest contest, Map<Integer, Boolean> isEditable){
        return MyContestResponse.builder()
                .id(contest.getId())
                .title(contest.getTitle())
                .description(contest.getDescription())
                .endTime(contest.getEndTime())
                .startTime(contest.getStartTime())
                .isEditable(isEditable.getOrDefault(contest.getId(), false))
                .numAttendees(contest.getParticipants().size())
                .maxAttendees(contest.getNumAttendees())
                .deleted(contest.getDeleted())
                .build();
    }
}
