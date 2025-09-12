package com.example.hoccode.dto.response.contest;

import com.example.hoccode.dto.response.ProblemResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContestDetailResponse {
    private Integer id;
    private String title;
    private String description;
    private String password;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer numAttendees;
    private List<ProblemResponse> problems;

    @Builder.Default
    private Boolean hasPassword = false;
}
