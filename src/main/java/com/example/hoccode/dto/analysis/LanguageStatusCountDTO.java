package com.example.hoccode.dto.analysis;

import com.example.hoccode.util.constant.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LanguageStatusCountDTO {
    private String language;
    private StatusEnum status;
    private Long count;
}