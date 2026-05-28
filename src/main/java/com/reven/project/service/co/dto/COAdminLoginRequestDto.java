// 관리자 로그인 요청 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record COAdminLoginRequestDto(

        @Schema(description = "로그인 아이디", example = "admin")
        String loginId,


        @Schema(description = "비밀번호", example = "password")
        String password
) {
}
