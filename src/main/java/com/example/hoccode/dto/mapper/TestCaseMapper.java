package com.example.hoccode.dto.mapper;

import com.example.hoccode.dto.request.call.CallTestCaseRequest;
import com.example.hoccode.entity.TestCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestCaseMapper {
    public CallTestCaseRequest toCallTestCaseRequest(TestCase testCase){
        return CallTestCaseRequest.builder().input(testCase.getInput()).expected_output(testCase.getExpectedOutput()).build();
    }

}
