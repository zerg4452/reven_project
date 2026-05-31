// 공지사항 관리자 목록 검색 조건 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record BDNoticeAdminSearchRequestDto(

        @Schema(description = "게시일 검색 시작일", example = "2026-03-30")
        LocalDate startDate,


        @Schema(description = "게시일 검색 종료일", example = "2026-05-31")
        LocalDate endDate
) {
}
