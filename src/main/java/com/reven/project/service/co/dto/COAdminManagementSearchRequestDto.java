package com.reven.project.service.co.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record COAdminManagementSearchRequestDto(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateTo,
        String keywordType,
        String keyword
) {
}
