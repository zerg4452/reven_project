// AI News 목록 페이지 응답 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record BDAiNewsPageResponseDto(

        @Schema(description = "검색 조건", example = "검색 조건")
        BDAiNewsSearchRequestDto search,


        @Schema(description = "전체 건수", example = "42")
        long totalCount,


        @Schema(description = "AI News 목록", example = "[]")
        List<BDAiNewsListItemResponseDto> news
) {
}
