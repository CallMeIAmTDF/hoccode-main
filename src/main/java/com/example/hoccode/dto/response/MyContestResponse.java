package com.example.hoccode.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyContestResponse {
    Integer id;
    String title;
    String description;
    Integer numAttendees;
    Integer maxAttendees;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Boolean deleted;
    Boolean isEditable;
}
