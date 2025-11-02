package com.example.hoccode.util;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.service.ISensitiveWordService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SensitiveWordValidationUtil {
    ISensitiveWordService sensitiveWordService;
    public void validateSensitiveWords(List<String> fields) {
        if (fields.stream().filter(Objects::nonNull).anyMatch(sensitiveWordService::contains)) {
            throw new BadRequestException("Chứa từ ngữ nhạy cảm");
        }
    }
}
