// 관리자 등록·수정 요청 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record COAdminWriteRequestDto(

        @Schema(description = "관리자 일련번호", example = "1")
        Long adminSeq,


        @Schema(description = "로그인 아이디", example = "admin")
        String loginId,


        @Schema(description = "관리자명", example = "홍길동")
        String name,


        @Schema(description = "사용 상태", example = "active")
        String status,


        @Schema(description = "비밀번호", example = "password")
        String password
) {
}
