// AI News 상태 체크박스 옵션 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI News 검색 화면에서 상태 체크박스를 렌더링하기 위한 옵션 DTO다.
 */
public record BDAiNewsStatusOptionDto(

        @Schema(description = "상태 코드", example = "P")
        String code,


        @Schema(description = "상태 라벨", example = "게시")
        String label
) {
}
