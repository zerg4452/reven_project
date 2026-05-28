// 관리자 접속 이력 저장 요청 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record COAccessLogCreateRequestDto(

        @Schema(description = "관리자 일련번호", example = "1")
        Long adminSeq,


        @Schema(description = "로그인 아이디", example = "admin")
        String loginId,


        @Schema(description = "요청 URI", example = "/admin/home.do")
        String requestUri,


        @Schema(description = "HTTP 메서드", example = "GET")
        String method,


        @Schema(description = "접속 IP", example = "127.0.0.1")
        String remoteAddr,


        @Schema(description = "User-Agent", example = "Mozilla/5.0")
        String userAgent,


        @Schema(description = "접속 일시", example = "2026-05-29T10:00:00")
        LocalDateTime accessAt
) {
}
