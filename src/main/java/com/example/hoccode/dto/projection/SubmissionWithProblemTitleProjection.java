package com.example.hoccode.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


public interface SubmissionWithProblemTitleProjection {
    String getSubmissionId();
    Integer getProblemId();
    String getCode();
    String getLanguage();
    String getUsername();
    String getStatus();
    String getProblemTitle();
}