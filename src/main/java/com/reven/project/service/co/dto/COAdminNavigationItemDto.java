// 관리자 GNB·LNB 네비게이션 항목 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record COAdminNavigationItemDto(

        @Schema(description = "메뉴 코드", example = "SURVEY_OPS")
        String menuCode,


        @Schema(description = "상위 메뉴 코드", example = "")
        String parentMenuCode,


        @Schema(description = "메뉴 깊이", example = "1")
        Integer depthNo,


        @Schema(description = "메뉴명", example = "설문 운영")
        String menuName,


        @Schema(description = "링크 URL", example = "/admin/surveys/list.do")
        String href,


        @Schema(description = "활성 여부", example = "true")
        boolean active,


        @Schema(description = "하위 메뉴", example = "[]")
        List<COAdminNavigationItemDto> children
) {
}
