// 사용자 포토 게시판 카드 응답 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record BDPhotoBoardPublicListItemResponseDto(

        @Schema(description = "포토 게시판 일련번호", example = "1")
        Long photoSeq,


        @Schema(description = "제목", example = "봄 사진 전시")
        String title,


        @Schema(description = "등록 일자", example = "2026-05-30")
        LocalDate registeredDate,


        @Schema(description = "대표 첨부 일련번호", example = "10")
        Long thumbnailFileSeq,


        @Schema(description = "대표 첨부 MIME 타입", example = "image/jpeg")
        String thumbnailContentType,


        @Schema(description = "대표 첨부 URL", example = "/board/photo/file.do?photoFileSeq=10")
        String thumbnailFileUrl,


        @Schema(description = "이미지 포함 여부", example = "true")
        Boolean hasImage,


        @Schema(description = "동영상 포함 여부", example = "false")
        Boolean hasVideo
) {
    public boolean thumbnailImage() {
        return thumbnailContentType != null && thumbnailContentType.startsWith("image/");
    }

    public boolean thumbnailVideo() {
        return thumbnailContentType != null && thumbnailContentType.startsWith("video/");
    }
}
