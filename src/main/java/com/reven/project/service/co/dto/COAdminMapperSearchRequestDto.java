// 관리자 MyBatis 검색 조건 DTO
package com.reven.project.service.co.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record COAdminMapperSearchRequestDto(

        @Schema(description = "로그인 아이디", example = "admin")
        String loginId
) {
}
