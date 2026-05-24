package com.reven.project.service.bd.dto;

/**
 * AI News 검색 화면에서 상태 체크박스를 렌더링하기 위한 옵션 DTO다.
 */
public record BDAiNewsStatusOptionDto(
        String code,
        String label
) {
}
