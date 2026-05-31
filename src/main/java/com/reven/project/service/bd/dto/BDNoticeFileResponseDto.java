// 공지사항 첨부 파일 응답 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record BDNoticeFileResponseDto(

        @Schema(description = "첨부 일련번호", example = "1")
        Long noticeFileSeq,

        @Schema(description = "공지사항 일련번호", example = "1")
        Long noticeSeq,

        @Schema(description = "파일 유형(THUMB/ATTACH)", example = "ATTACH")
        String fileType,

        @Schema(description = "원본 파일명", example = "notice.pdf")
        String originalFileName,

        @Schema(description = "저장 파일명", example = "20260531_notice.pdf")
        String storedFileName,

        @Schema(description = "저장 경로", example = "2026/05/31")
        String storedPath,

        @Schema(description = "MIME 타입", example = "application/pdf")
        String contentType,

        @Schema(description = "파일 크기(바이트)", example = "102400")
        Long fileSize,

        @Schema(description = "정렬 순서", example = "1")
        Integer sortOrder,

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

        @Schema(description = "파일 URL", example = "/admin/board/notice/file.do?noticeFileSeq=1")
        String fileUrl
) {
    public boolean thumbnail() {
        return "THUMB".equals(fileType);
    }

    public boolean image() {
        return contentType != null && contentType.startsWith("image/");
    }

    public String displayFileSizeKb() {
        long size = fileSize == null ? 0L : fileSize;
        long kb = Math.max(1L, (size + 1023L) / 1024L);
        return kb + " KB";
    }
}
