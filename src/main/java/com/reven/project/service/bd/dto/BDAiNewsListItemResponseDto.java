package com.reven.project.service.bd.dto;

import java.time.LocalDate;

public record BDAiNewsListItemResponseDto(
        Long newsSeq,
        String title,
        String slug,
        String category,
        String status,
        LocalDate publishedDate,
        LocalDate registeredDate,
        LocalDate updatedDate
) {
    public Long id() {
        return newsSeq;
    }

    public String statusText() {
        return "Y".equals(status) ? "게시" : "대기";
    }
}
