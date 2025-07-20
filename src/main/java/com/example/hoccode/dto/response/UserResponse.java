package com.example.hoccode.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String email;
    String name;
    LocalDate dob;
    String avatar;
    @JsonProperty("no_password")
    Boolean noPassword;
    @JsonProperty("is_locked")
    Boolean isLocked;
}