// 관리자 GNB·LNB 네비게이션 응답 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record COAdminNavigationResponseDto(

        @Schema(description = "GNB 항목 목록", example = "[]")
        List<COAdminNavigationItemDto> gnbItems,


        @Schema(description = "활성 1-depth 메뉴", example = "활성 루트")
        COAdminNavigationItemDto activeRoot,


        @Schema(description = "LNB 항목 목록", example = "[]")
        List<COAdminNavigationItemDto> lnbItems
) {
}
