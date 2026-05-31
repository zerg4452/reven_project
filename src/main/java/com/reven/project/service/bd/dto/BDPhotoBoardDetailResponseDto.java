// 포토 게시판 상세 응답 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record BDPhotoBoardDetailResponseDto(

        @Schema(description = "포토 게시판 일련번호", example = "1")
        Long photoSeq,


        @Schema(description = "제목", example = "봄 사진 전시")
        String title,


        @Schema(description = "게시 여부", example = "Y")
        String publishYn,


        @Schema(description = "삭제 여부", example = "N")
        String deleteFlg,


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
        return photoSeq;
    }

    public String publishText() {
        return "Y".equals(publishYn) ? "게시" : "대기";
    }
}
