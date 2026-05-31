// 사용자 공지사항 검색 요청 DTO
package com.reven.project.service.bd.dto;

public record BDNoticePublicSearchRequestDto(
        String keyword,
        int page,
        int size
) {
    public BDNoticePublicSearchRequestDto normalized() {
        int normalizedPage = page < 1 ? 1 : page;
        int normalizedSize = size < 1 ? 10 : size;
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        return new BDNoticePublicSearchRequestDto(normalizedKeyword, normalizedPage, normalizedSize);
    }

    public int offset() {
        return (page - 1) * size;
    }
}
