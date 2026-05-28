// 관리자 메뉴 트리 항목 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record COAdminMenuTreeItemDto(

        @Schema(description = "관리자 메뉴 일련번호", example = "1")
        Long adminMenuSeq,


        @Schema(description = "상위 메뉴 일련번호", example = "0")
        Long parentAdminMenuSeq,


        @Schema(description = "메뉴 코드", example = "SURVEY_MGMT")
        String menuCode,


        @Schema(description = "상위 메뉴 코드", example = "SURVEY_OPS")
        String parentMenuCode,


        @Schema(description = "메뉴 깊이", example = "2")
        Integer depthNo,


        @Schema(description = "메뉴명", example = "설문 관리")
        String menuName,


        @Schema(description = "메뉴 URL", example = "/admin/surveys/list.do")
        String menuUrl,


        @Schema(description = "메뉴 유형", example = "page")
        String menuType,


        @Schema(description = "사용 여부", example = "Y")
        String useYn,


        @Schema(description = "정렬 순서", example = "10")
        Integer sortOrder,


        @Schema(description = "선택 여부", example = "true")
        boolean selected
) {
}
