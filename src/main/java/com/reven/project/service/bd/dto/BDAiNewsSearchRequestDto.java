package com.reven.project.service.bd.dto;

import java.time.LocalDate;
import java.util.List;

public record BDAiNewsSearchRequestDto(
        LocalDate startDate,
        LocalDate endDate,
        String keywordType,
        String keyword,
        List<String> statuses,
        int offset,
        int limit
) {
}
