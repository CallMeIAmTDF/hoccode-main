package com.example.hoccode.dto.response.call;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BasicCodeAnalysisResponse {
    String time_complexity;
    String space_complexity;
    Content content;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Content{
    List<String> overview;
    List<String> advantage;
    List<String> disadvantages;
    List<String> proposal;
}