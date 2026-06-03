// AI News 상세 응답 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BDAiNewsDetailResponseDto(

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


        @Schema(description = "태그 JSON", example = "[\"AI\"]")
        String tagsJson,


        @Schema(description = "출처 JSON", example = "[]")
        String sourcesJson,


        @Schema(description = "상태", example = "Y")
        String status,


        @Schema(description = "삭제 여부", example = "N")
        String deleteFlg,


        @Schema(description = "게시 일자", example = "2026-05-29")
        LocalDate publishedDate,


        @Schema(description = "원본 파일명", example = "source.json")
        String sourceFile,


        @Schema(description = "크롤 오류 메시지", example = "")
        String crawlError,


        @Schema(description = "등록 일시", example = "2026-05-28T09:00:00")
        LocalDateTime registeredAt,


        @Schema(description = "등록자 아이디", example = "admin")
        String registeredBy,


        @Schema(description = "수정 일시", example = "2026-05-29T10:00:00")
        LocalDateTime modifiedAt,


        @Schema(description = "수정자 아이디", example = "admin")
        String modifiedBy,


        @Schema(description = "조회수", example = "12")
        Long viewCnt
) {
    public Long id() {
        return newsSeq;
    }

    public String statusText() {
        return switch (status == null ? "" : status) {
            case "P" -> "처리중";
            case "Y" -> "완료";
            case "E" -> "에러";
            default -> "대기";
        };
    }
}
