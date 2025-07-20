package com.example.hoccode.dto.response.contest;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContestResponse {
    Integer id;
    String title;
    String description;
    Integer numAttendees;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Boolean hasPassword;
}
