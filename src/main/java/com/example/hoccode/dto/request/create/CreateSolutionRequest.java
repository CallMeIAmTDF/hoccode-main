package com.example.hoccode.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateSolutionRequest {
    @NotBlank(message = "Ngôn ngữ là bắt buộc")
    String language;

    @NotBlank(message = "Mã nguồn là bắt buộc")
    String code;
    String time;
    String space;
    String note;

    @NotBlank(message = "Id là bắt buộc")
    Integer pid;
}
