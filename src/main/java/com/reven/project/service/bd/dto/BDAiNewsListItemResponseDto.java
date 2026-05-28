// AI News 목록 행 응답 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record BDAiNewsListItemResponseDto(

        @Schema(description = "AI News 일련번호", example = "1")
        Long newsSeq,


        @Schema(description = "제목", example = "AI 뉴스 샘플")
        String title,


        @Schema(description = "슬러그", example = "ai-news-sample")
        String slug,


        @Schema(description = "분류", example = "기술")
        String category,


        @Schema(description = "상태", example = "Y")
        String status,


        @Schema(description = "게시 일자", example = "2026-05-29")
        LocalDate publishedDate,


        @Schema(description = "등록 일자", example = "2026-05-28")
        LocalDate registeredDate,


        @Schema(description = "수정 일자", example = "2026-05-29")
        LocalDate updatedDate
) {
    public Long id() {
        return newsSeq;
    }

    public String statusText() {
        return "Y".equals(status) ? "게시" : "대기";
    }
}
