// 사용자 공지사항 목록 행 응답 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record BDNoticePublicListItemResponseDto(

        @Schema(description = "공지사항 일련번호", example = "1")
        Long noticeSeq,

        @Schema(description = "제목", example = "서비스 점검 안내")
        String title,

        @Schema(description = "상단 고정 여부", example = "N")
        String pinYn,

        @Schema(description = "조회수", example = "12")
        Long viewCnt,

        @Schema(description = "게시 일자", example = "2026-05-31")
        LocalDate publishDate,

        @Schema(description = "대표 썸네일 일련번호", example = "10")
        Long thumbnailFileSeq,

        @Schema(description = "대표 썸네일 URL", example = "/board/notice/file.do?noticeFileSeq=10")
        String thumbnailFileUrl
) {
    public boolean pinned() {
        return "Y".equals(pinYn);
    }

    public boolean hasThumbnail() {
        return thumbnailFileSeq != null;
    }
}
