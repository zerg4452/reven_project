// 관리자 목록 페이지 응답 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record COAdminManagementPageResponseDto(

        @Schema(description = "검색 조건", example = "검색 조건")
        COAdminManagementSearchRequestDto search,


        @Schema(description = "전체 건수", example = "5")
        long totalCount,


        @Schema(description = "관리자 목록", example = "[]")
        List<COAdminListItemResponseDto> admins
) {
}
