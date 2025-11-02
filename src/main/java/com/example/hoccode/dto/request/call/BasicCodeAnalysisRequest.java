package com.example.hoccode.dto.request.call;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BasicCodeAnalysisRequest {
    String code;
}
