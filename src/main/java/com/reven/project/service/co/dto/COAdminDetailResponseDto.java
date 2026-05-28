// 관리자 상세 응답 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record COAdminDetailResponseDto(

        @Schema(description = "관리자 일련번호", example = "1")
        Long adminSeq,


        @Schema(description = "로그인 아이디", example = "admin")
        String loginId,


        @Schema(description = "관리자명", example = "홍길동")
        String name,


        @Schema(description = "권한", example = "admin")
        String role,


        @Schema(description = "사용 상태", example = "active")
        String status,


        @Schema(description = "비밀번호 해시", example = "{bcrypt}$2a$10$example")
        String passwordHash,


        @Schema(description = "등록 일시", example = "2026-05-28T09:00:00")
        LocalDateTime regDtm,


        @Schema(description = "등록자 아이디", example = "system")
        String regId,


        @Schema(description = "수정 일시", example = "2026-05-29T10:00:00")
        LocalDateTime modDtm,


        @Schema(description = "수정자 아이디", example = "admin")
        String modId
) {
}
