package com.example.hoccode.dto.request.create;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTestCaseRequest {
    String input;
    String expectedOutput;
    Boolean isHidden;
}
