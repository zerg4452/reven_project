// AI News 목록 검색 조건 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record BDAiNewsSearchRequestDto(

        @Schema(description = "검색 시작일", example = "2026-03-30")
        LocalDate startDate,


        @Schema(description = "검색 종료일", example = "2026-05-30")
        LocalDate endDate,


        @Schema(description = "키워드 유형", example = "전체")
        String keywordType,


        @Schema(description = "검색어", example = "AI")
        String keyword,


        @Schema(description = "상태 목록", example = "[\"P\",\"Y\"]")
        List<String> statuses,


        @Schema(description = "조회 시작 위치", example = "0")
        Integer offset,


        @Schema(description = "조회 건수", example = "20")
        Integer limit
) {
}
