// 공지사항 상세 응답 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record BDNoticeDetailResponseDto(

        @Schema(description = "공지사항 일련번호", example = "1")
        Long noticeSeq,

        @Schema(description = "제목", example = "서비스 점검 안내")
        String title,

        @Schema(description = "본문", example = "본문 HTML")
        String content,

        @Schema(description = "노출 여부", example = "Y")
        String publishYn,

        @Schema(description = "상단 고정 여부", example = "N")
        String pinYn,

        @Schema(description = "조회수", example = "12")
        Long viewCnt,

        @Schema(description = "게시일", example = "2026-05-31T09:00:00")
        LocalDateTime publishDtm,

        @Schema(description = "삭제 여부", example = "N")
        String deleteFlg,

        @Schema(description = "등록 일시", example = "2026-05-28T09:00:00")
        LocalDateTime registeredAt,

        @Schema(description = "등록자 아이디", example = "admin")
        String registeredBy,

        @Schema(description = "수정 일시", example = "2026-05-29T10:00:00")
        LocalDateTime modifiedAt,

        @Schema(description = "수정자 아이디", example = "admin")
        String modifiedBy
) {
    public Long id() {
        return noticeSeq;
    }

    public String publishText() {
        return "Y".equals(publishYn) ? "노출" : "대기";
    }

    public boolean pinned() {
        return "Y".equals(pinYn);
    }

    public String pinText() {
        return pinned() ? "고정" : "일반";
    }
}
