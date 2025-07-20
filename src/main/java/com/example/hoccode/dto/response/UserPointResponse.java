package com.example.hoccode.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPointResponse {
    String id;
    Integer point;
    String avatar;
    String name;
}
