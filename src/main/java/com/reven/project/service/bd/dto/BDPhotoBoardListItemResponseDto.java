// 포토 게시판 목록 행 응답 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record BDPhotoBoardListItemResponseDto(

        @Schema(description = "포토 게시판 일련번호", example = "1")
        Long photoSeq,


        @Schema(description = "제목", example = "봄 사진 전시")
        String title,


        @Schema(description = "첨부 파일 수", example = "3")
        Integer fileCount,


        @Schema(description = "게시 여부", example = "Y")
        String publishYn,


        @Schema(description = "등록 일자", example = "2026-05-28")
        LocalDate registeredDate,


        @Schema(description = "수정 일자", example = "2026-05-29")
        LocalDate updatedDate
) {
    public Long id() {
        return photoSeq;
    }

    public String publishText() {
        return "Y".equals(publishYn) ? "게시" : "대기";
    }
}
