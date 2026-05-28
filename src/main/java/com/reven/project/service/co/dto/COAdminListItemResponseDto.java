// 관리자 목록 행 응답 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record COAdminListItemResponseDto(

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


        @Schema(description = "등록 일자", example = "2026-05-28")
        LocalDate registeredDate,


        @Schema(description = "수정 일자", example = "2026-05-29")
        LocalDate updatedDate
) {
}
