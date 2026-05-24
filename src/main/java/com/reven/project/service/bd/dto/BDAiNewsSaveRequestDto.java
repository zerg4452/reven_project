package com.reven.project.service.bd.dto;

import java.time.LocalDate;

public record BDAiNewsSaveRequestDto(
        Long newsSeq,
        String slug,
        String title,
        String category,
        String summary,
        String content,
        String tagsJson,
        String sourcesJson,
        LocalDate publishedDate,
        String status,
        String actorId
) {
}
