// 포토 게시판 저장 요청 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BDPhotoBoardSaveRequestDto(

        @Schema(description = "포토 게시판 일련번호", example = "1")
        Long photoSeq,


        @Schema(description = "제목", example = "봄 사진 전시")
        String title,


        @Schema(description = "게시 여부", example = "Y")
        String publishYn,


        @Schema(description = "처리자 아이디", example = "admin")
        String actorId
) {
}
