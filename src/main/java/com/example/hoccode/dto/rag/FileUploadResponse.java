package com.example.hoccode.dto.rag;

import com.example.hoccode.util.constant.FileUploadStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileUploadResponse {
    String file;
    Integer size;
    FileUploadStatus status;
}
