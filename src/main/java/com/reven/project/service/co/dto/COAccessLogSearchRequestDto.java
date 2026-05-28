// 관리자 접속 이력 검색 조건 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record COAccessLogSearchRequestDto(

        @Schema(description = "검색 시작일", example = "2026-03-30")
        LocalDate startDate,


        @Schema(description = "검색 종료일", example = "2026-05-30")
        LocalDate endDate,


        @Schema(description = "검색어", example = "admin")
        String keyword,


        @Schema(description = "조회 시작 위치", example = "0")
        Integer offset,


        @Schema(description = "조회 건수", example = "20")
        Integer limit
) {
}
