// 관리자 홈 화면 응답 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record COAdminHomeResponseDto(

        @Schema(description = "화면명", example = "관리자 홈")
        String screenName,


        @Schema(description = "화면 목적", example = "관리자 대시보드 요약을 표시합니다.")
        String purpose,


        @Schema(description = "기준 일자", example = "2026-05-29")
        LocalDate baseDate,


        @Schema(description = "오늘 접속 건수", example = "12")
        long todayAccessCount
) {
}
