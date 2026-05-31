// 공지사항 관리자 목록 행 응답 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record BDNoticeListItemResponseDto(

        @Schema(description = "공지사항 일련번호", example = "1")
        Long noticeSeq,

        @Schema(description = "제목", example = "서비스 점검 안내")
        String title,

        @Schema(description = "노출 여부", example = "Y")
        String publishYn,

        @Schema(description = "상단 고정 여부", example = "N")
        String pinYn,

        @Schema(description = "조회수", example = "12")
        Long viewCnt,

        @Schema(description = "게시 일자", example = "2026-05-31")
        LocalDate publishDate,

        @Schema(description = "수정 일자", example = "2026-05-29")
        LocalDate updatedDate
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
