package com.example.hoccode.dto.response;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionCategoryCount {
    String category;
    long count;
}