// 포토 게시판 첨부 저장 요청 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BDPhotoBoardFileSaveRequestDto(

        @Schema(description = "포토 게시판 일련번호", example = "1")
        Long photoSeq,


        @Schema(description = "원본 파일명", example = "sample.jpg")
        String originalFileName,


        @Schema(description = "저장 파일명", example = "20260529_sample.jpg")
        String storedFileName,


        @Schema(description = "저장 경로", example = "/upload/photo/2026/05/29/sample.jpg")
        String storedPath,


        @Schema(description = "MIME 타입", example = "image/jpeg")
        String contentType,


        @Schema(description = "파일 크기(바이트)", example = "102400")
        Long fileSize,


        @Schema(description = "정렬 순서", example = "1")
        Integer sortOrder,


        @Schema(description = "처리자 아이디", example = "admin")
        String actorId
) {
}
