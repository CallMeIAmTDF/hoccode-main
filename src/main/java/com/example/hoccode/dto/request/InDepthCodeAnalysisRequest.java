package com.example.hoccode.dto.request;

import com.example.hoccode.util.constant.StatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InDepthCodeAnalysisRequest {
    String code;
    StatusEnum status;
}
