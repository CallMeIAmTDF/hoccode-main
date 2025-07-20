package com.example.hoccode.dto.request.update;

import lombok.Data;

@Data
public class UpdateTestcaseRequest {
    private String input;
    private String expectedOutput;
    private Boolean isHidden;
}
