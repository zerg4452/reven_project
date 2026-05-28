// AI News 저장 요청 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record BDAiNewsSaveRequestDto(

        @Schema(description = "AI News 일련번호", example = "1")
        Long newsSeq,


        @Schema(description = "슬러그", example = "ai-news-sample")
        String slug,


        @Schema(description = "제목", example = "AI 뉴스 샘플")
        String title,


        @Schema(description = "분류", example = "기술")
        String category,


        @Schema(description = "요약", example = "요약 문구")
        String summary,


        @Schema(description = "본문", example = "본문 HTML")
        String content,


        @Schema(description = "태그 JSON", example = "[\"AI\",\"뉴스\"]")
        String tagsJson,


        @Schema(description = "출처 JSON", example = "[]")
        String sourcesJson,


        @Schema(description = "게시 일자", example = "2026-05-29")
        LocalDate publishedDate,


        @Schema(description = "상태", example = "P")
        String status,


        @Schema(description = "처리자 아이디", example = "admin")
        String actorId
) {
}
