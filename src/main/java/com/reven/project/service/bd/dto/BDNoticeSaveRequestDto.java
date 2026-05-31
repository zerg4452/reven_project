// 공지사항 저장 요청 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record BDNoticeSaveRequestDto(

        @Schema(description = "공지사항 일련번호", example = "1")
        Long noticeSeq,

        @Schema(description = "제목", example = "서비스 점검 안내")
        String title,

        @Schema(description = "본문", example = "본문 HTML")
        String content,

        @Schema(description = "게시일", example = "2026-05-31T09:00:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime publishDtm,

        @Schema(description = "노출 여부", example = "Y")
        String publishYn,

        @Schema(description = "상단 고정 여부", example = "N")
        String pinYn,

        @Schema(description = "처리자 아이디", example = "admin")
        String actorId
) {
}
