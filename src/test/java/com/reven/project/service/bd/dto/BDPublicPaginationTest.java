// 공개 게시판 페이징 구간 계산을 검증하는 테스트
package com.reven.project.service.bd.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BDPublicPaginationTest {

    @Test
    void noticePaginationBuildsFirstTenPageWindow() {
        BDNoticePublicPageResponseDto page = new BDNoticePublicPageResponseDto(
                new BDNoticePublicSearchRequestDto("", 1, 10),
                java.util.List.of(),
                java.util.List.of(),
                0,
                23
        );

        assertThat(page.currentPage()).isEqualTo(1);
        assertThat(page.startPage()).isEqualTo(1);
        assertThat(page.endPage()).isEqualTo(10);
        assertThat(page.pageNumbers()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertThat(page.hasPreviousGroup()).isFalse();
        assertThat(page.previousGroupPage()).isEqualTo(1);
        assertThat(page.hasNextGroup()).isTrue();
        assertThat(page.nextGroupPage()).isEqualTo(11);
        assertThat(page.lastPage()).isEqualTo(23);
    }

    @Test
    void photoPaginationBuildsTrailingPageWindow() {
        BDPhotoBoardPublicPageResponseDto page = new BDPhotoBoardPublicPageResponseDto(
                new BDPhotoBoardPublicSearchRequestDto("", false, false, 21, 9),
                java.util.List.of(),
                0,
                23
        );

        assertThat(page.currentPage()).isEqualTo(21);
        assertThat(page.startPage()).isEqualTo(21);
        assertThat(page.endPage()).isEqualTo(23);
        assertThat(page.pageNumbers()).containsExactly(21, 22, 23);
        assertThat(page.hasPreviousGroup()).isTrue();
        assertThat(page.previousGroupPage()).isEqualTo(11);
        assertThat(page.hasNextGroup()).isFalse();
        assertThat(page.nextGroupPage()).isEqualTo(23);
        assertThat(page.lastPage()).isEqualTo(23);
    }
}
