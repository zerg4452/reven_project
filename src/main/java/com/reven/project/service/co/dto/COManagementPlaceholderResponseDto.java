// 관리자 공통 플레이스홀더 화면 응답 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record COManagementPlaceholderResponseDto(

        @Schema(description = "화면명", example = "관리자 관리")
        String screenName,


        @Schema(description = "화면 목적", example = "관리자 계정을 관리합니다.")
        String purpose,


        @Schema(description = "기준 일자", example = "2026-05-29")
        LocalDate baseDate,


        @Schema(description = "준비 중 섹션 목록", example = "[\"메뉴\",\"권한\"]")
        List<String> pendingSections
) {
}
