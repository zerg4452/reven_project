package com.reven.project.service.co.dto;

import java.time.LocalDate;

public record COAccessLogSearchRequestDto(
        LocalDate startDate,
        LocalDate endDate,
        String keyword,
        int offset,
        int limit
) {
}
