package com.reven.project.service.bd.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BDAiNewsDetailResponseDto(
        Long newsSeq,
        String slug,
        String title,
        String category,
        String summary,
        String content,
        String tagsJson,
        String sourcesJson,
        String status,
        String deleteFlg,
        LocalDate publishedDate,
        String sourceFile,
        String crawlError,
        LocalDateTime registeredAt,
        String registeredBy,
        LocalDateTime modifiedAt,
        String modifiedBy
) {
    public Long id() {
        return newsSeq;
    }

    public String statusText() {
        return "Y".equals(status) ? "게시" : "대기";
    }
}
