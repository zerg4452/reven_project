// 사용자 포토 게시판 목록 페이지 응답 DTO
package com.reven.project.service.bd.dto;

import java.util.List;

public record BDPhotoBoardPublicPageResponseDto(
        BDPhotoBoardPublicSearchRequestDto search,
        List<BDPhotoBoardPublicListItemResponseDto> photos,
        int totalCount,
        int totalPages
) {
    public boolean hasPrevious() {
        return search.page() > 1;
    }

    public boolean hasNext() {
        return search.page() < totalPages;
    }

    public int previousPage() {
        return Math.max(1, search.page() - 1);
    }

    public int nextPage() {
        return Math.min(totalPages, search.page() + 1);
    }
}
