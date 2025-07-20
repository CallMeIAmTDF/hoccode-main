package com.example.hoccode.dto.response.pageable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationInfo {
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
