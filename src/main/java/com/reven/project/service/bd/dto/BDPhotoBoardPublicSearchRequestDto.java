// 사용자 포토 게시판 검색 요청 DTO
package com.reven.project.service.bd.dto;

public record BDPhotoBoardPublicSearchRequestDto(
        String keyword,
        boolean imageOnly,
        boolean videoOnly,
        int page,
        int size
) {
    public BDPhotoBoardPublicSearchRequestDto normalized() {
        int normalizedPage = page < 1 ? 1 : page;
        int normalizedSize = 9;
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        return new BDPhotoBoardPublicSearchRequestDto(
                normalizedKeyword,
                imageOnly,
                videoOnly,
                normalizedPage,
                normalizedSize
        );
    }

    public int offset() {
        return (page - 1) * size;
    }

    public boolean hasMediaFilter() {
        return imageOnly || videoOnly;
    }
}
