package com.example.hoccode.dto.response.contest;

import com.example.hoccode.dto.response.pageable.PaginationInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ContestLeaderboardResponse {
    private String contestName;
    private List<ContestLeaderboardEntryDTO> entries;
    private PaginationInfo pagination;
}
