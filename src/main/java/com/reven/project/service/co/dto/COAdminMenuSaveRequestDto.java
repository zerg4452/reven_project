// 관리자 메뉴 저장 요청 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record COAdminMenuSaveRequestDto(

        @Schema(description = "관리자 메뉴 일련번호", example = "1")
        Long adminMenuSeq,


        @Schema(description = "메뉴 코드", example = "SURVEY_MGMT")
        String menuCode,


        @Schema(description = "상위 메뉴 코드", example = "SURVEY_OPS")
        String parentMenuCode,


        @Schema(description = "메뉴명", example = "설문 관리")
        String menuName,


        @Schema(description = "메뉴 URL", example = "/admin/surveys/list.do")
        String menuUrl,


        @Schema(description = "매칭 URL 목록(줄바꿈 구분)", example = "/admin/surveys/list.do")
        String matchUrlsText,


        @Schema(description = "메뉴 유형", example = "page")
        String menuType,


        @Schema(description = "게시판 키", example = "")
        String boardKey,


        @Schema(description = "사용 여부", example = "Y")
        String useYn,


        @Schema(description = "정렬 순서", example = "10")
        Integer sortOrder,


        @Schema(description = "처리자 아이디", example = "admin")
        String actorId
) {
}
