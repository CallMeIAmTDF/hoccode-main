package com.example.hoccode.dto.request.call;

import com.example.hoccode.util.constant.StatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TestCaseSubmissionResponse {
    Integer case_id;
    StatusEnum status;
    String message;
    Integer time;
}
