package com.example.hoccode.dto.projection;

import com.example.hoccode.entity.Problem;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class ProblemSubmissionCount {
    Problem problem;
    Long submissionCount;
}