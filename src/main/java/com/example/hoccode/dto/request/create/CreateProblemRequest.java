package com.example.hoccode.dto.request.create;

import com.example.hoccode.util.constant.DifficultyEnum;
import com.example.hoccode.util.constant.ProblemCategoryEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateProblemRequest {

    @NotBlank(message = "Tiêu đề là bắt buộc")
    String title;

    @NotBlank(message = "Mô tả là bắt buộc")
    String description;
    String constrains;
    String followUp;

    @NotNull(message = "Giới hạn thời gian là bắt buộc")
    Integer timeLimit;

    @NotNull(message = "Giới hạn bộ nhớ là bắt buộc")
    Integer memoryLimit;
    DifficultyEnum difficulty;

    @NotNull(message = "Điểm là bắt buộc")
    @Min(value = 0, message = "Điểm >= 0")
    Integer point;

    List<String> hints;

    @NotEmpty(message = "Test cases không để trống")
    @Valid
    List<CreateTestCaseRequest> testCases;

    @NotEmpty(message = "Danh mục không để trống")
    @Valid
    List<ProblemCategoryEnum> categories;
}