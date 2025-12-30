package com.example.hoccode.dto.analysis;

import com.example.hoccode.util.constant.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionStatusCountDTO {
    private StatusEnum status;
    private Long count;
}