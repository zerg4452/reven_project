// AI News 크롤링 처리 결과 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI News JSON 크롤링/임포트 처리 결과를 화면에 보여주기 위한 DTO다.
 */
public record BDAiNewsCrawlResultDto(

        @Schema(description = "처리 건수", example = "10")
        int total,


        @Schema(description = "성공 건수", example = "8")
        int success,


        @Schema(description = "실패 건수", example = "2")
        int failed
) {
    public String message() {
        return String.format("크롤링 완료. %d건 처리, %d건 성공, %d건 실패.", total, success, failed);
    }
}
