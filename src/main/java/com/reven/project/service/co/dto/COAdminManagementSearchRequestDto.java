// 관리자 목록 검색 조건 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record COAdminManagementSearchRequestDto(

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "검색 시작일", example = "2026-03-30")
        LocalDate dateFrom,


        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "검색 종료일", example = "2026-05-30")
        LocalDate dateTo,


        @Schema(description = "키워드 유형", example = "전체")
        String keywordType,


        @Schema(description = "검색어", example = "admin")
        String keyword
) {
}
