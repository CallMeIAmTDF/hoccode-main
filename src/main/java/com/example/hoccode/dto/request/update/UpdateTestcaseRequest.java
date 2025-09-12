package com.example.hoccode.dto.request.update;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTestcaseRequest {
    private String input;
    private String expectedOutput;
    private Boolean isHidden;
}
