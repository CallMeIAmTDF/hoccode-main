package com.example.hoccode.dto.mapper;

import com.example.hoccode.dto.response.MySubmissionResponse;
import com.example.hoccode.entity.Submission;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubmissionMapper {

    public MySubmissionResponse toMySubmissionResponse(Submission submission){
        return MySubmissionResponse.builder()
                .pid(submission.getPid())
                .id(submission.getId())
                .code(submission.getCode())
                .createdAt(submission.getCreatedAt())
                .isContest(false)
                .passedTestCase(submission.getPassedTestCase())
                .totalTestCases(submission.getTotalTestCases())
                .status(submission.getStatus())
                .maxTime(submission.getMaxTime())
                .language(submission.getLanguage())
                .build();
    }
}
