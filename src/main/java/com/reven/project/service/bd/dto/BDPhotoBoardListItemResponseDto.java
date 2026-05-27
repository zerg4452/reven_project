package com.reven.project.service.bd.dto;

import java.time.LocalDate;

public record BDPhotoBoardListItemResponseDto(
        Long photoSeq,
        String title,
        Integer fileCount,
        String publishYn,
        LocalDate registeredDate,
        LocalDate updatedDate
) {
    public Long id() {
        return photoSeq;
    }

    public String publishText() {
        return "Y".equals(publishYn) ? "게시" : "대기";
    }
}
