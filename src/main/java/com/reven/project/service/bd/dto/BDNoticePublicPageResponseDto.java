// 사용자 공지사항 목록 페이지 응답 DTO
package com.reven.project.service.bd.dto;

import java.util.List;
import java.util.stream.IntStream;

public record BDNoticePublicPageResponseDto(
        BDNoticePublicSearchRequestDto search,
        List<BDNoticePublicListItemResponseDto> pinnedNotices,
        List<BDNoticePublicListItemResponseDto> notices,
        int totalCount,
        int totalPages
) {
    private static final int PAGE_GROUP_SIZE = 10;

    public boolean hasPrevious() {
        return currentPage() > 1;
    }

    public boolean hasNext() {
        return currentPage() < totalPages;
    }

    public int previousPage() {
        return Math.max(1, currentPage() - 1);
    }

    public int nextPage() {
        return Math.min(totalPages, currentPage() + 1);
    }

    public int firstPage() {
        return 1;
    }

    public int lastPage() {
        return Math.max(1, totalPages);
    }

    public boolean hasPreviousGroup() {
        return startPage() > firstPage();
    }

    public boolean hasNextGroup() {
        return endPage() < lastPage();
    }

    public int previousGroupPage() {
        return Math.max(firstPage(), startPage() - PAGE_GROUP_SIZE);
    }

    public int nextGroupPage() {
        return Math.min(lastPage(), startPage() + PAGE_GROUP_SIZE);
    }

    public int startPage() {
        return ((currentPage() - 1) / PAGE_GROUP_SIZE) * PAGE_GROUP_SIZE + 1;
    }

    public int endPage() {
        return Math.min(lastPage(), startPage() + PAGE_GROUP_SIZE - 1);
    }

    public List<Integer> pageNumbers() {
        if (totalPages < 1) {
            return List.of();
        }
        return IntStream.rangeClosed(startPage(), endPage()).boxed().toList();
    }

    public int currentPage() {
        if (search == null) {
            return 1;
        }
        return Math.min(Math.max(1, search.page()), lastPage());
    }
}
