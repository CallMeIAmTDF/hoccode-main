package com.example.hoccode.dto.request.create;

import com.example.hoccode.util.constant.ReportTypeEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReportRequest {
    String title;
    String description;
    ReportTypeEnum type;
    String target;
}
