package com.reven.project.service.bd.dto;

/**
 * AI News JSON 크롤링/임포트 처리 결과를 화면에 보여주기 위한 DTO다.
 */
public record BDAiNewsCrawlResultDto(
        int total,
        int success,
        int failed
) {
    public String message() {
        return String.format("크롤링 완료. %d건 처리, %d건 성공, %d건 실패.", total, success, failed);
    }
}
