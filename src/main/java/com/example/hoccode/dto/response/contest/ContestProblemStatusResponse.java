package com.example.hoccode.dto.response.contest;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContestProblemStatusResponse {
    private String problemName;
    private String status; // ACCEPTED, UNACCEPTED, or NOT_SUBMITTED
    private Integer acceptedCount;
    private Integer wrongCount;
}
