// 관리자 세션 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public record COAdminSessionDto(

        @Schema(description = "관리자 일련번호", example = "1")
        Long adminSeq,


        @Schema(description = "로그인 아이디", example = "admin")
        String loginId,


        @Schema(description = "비밀번호 해시", example = "{bcrypt}$2a$10$example")
        String passwordHash,


        @Schema(description = "관리자명", example = "홍길동")
        String adminName,


        @Schema(description = "권한 CSV", example = "ADMIN,SUPER")
        String roleCsv,


        @Schema(description = "마지막 로그인 일시", example = "2026-05-29T08:30:00")
        LocalDateTime lastLoginAt
) {
    public List<String> roles() {
        if (roleCsv == null || roleCsv.isBlank()) {
            return List.of("ADMIN");
        }
        return Arrays.stream(roleCsv.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .toList();
    }
}
