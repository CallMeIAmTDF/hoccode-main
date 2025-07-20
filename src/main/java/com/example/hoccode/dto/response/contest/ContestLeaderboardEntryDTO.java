package com.example.hoccode.dto.response.contest;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ContestLeaderboardEntryDTO {
    private String username;
    private Integer totalPoints;
    private List<ContestProblemStatusResponse> problemStatuses;
}
