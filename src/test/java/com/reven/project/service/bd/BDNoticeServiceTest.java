// 공개 공지사항 페이지 보정과 페이징 데이터를 검증하는 테스트
package com.reven.project.service.bd;

import com.reven.project.service.bd.dto.BDNoticePublicListItemResponseDto;
import com.reven.project.service.bd.dto.BDNoticePublicPageResponseDto;
import com.reven.project.service.bd.dto.BDNoticePublicSearchRequestDto;
import com.reven.project.service.bd.mapper.BDNoticeMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BDNoticeServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void searchPublicNoticesClampsPageBeyondLastPage() {
        BDNoticeMapper mapper = mock(BDNoticeMapper.class);
        when(mapper.selectPublicPinnedNotices()).thenReturn(List.of(publicItem(1L, "고정 공지", "Y")));
        when(mapper.selectPublicNoticeCount(any())).thenReturn(12);
        when(mapper.selectPublicNoticeList(any())).thenReturn(List.of(publicItem(2L, "일반 공지", "N")));

        BDNoticeService service = new BDNoticeService(mapper, tempDir.toString(), "/board/notice/file.do", 10, 5, 20);

        BDNoticePublicPageResponseDto page = service.searchPublicNotices(
                new BDNoticePublicSearchRequestDto("  공지  ", 99, 10)
        );

        ArgumentCaptor<BDNoticePublicSearchRequestDto> searchCaptor =
                ArgumentCaptor.forClass(BDNoticePublicSearchRequestDto.class);
        verify(mapper).selectPublicNoticeCount(searchCaptor.capture());
        verify(mapper).selectPublicNoticeList(searchCaptor.capture());
        assertThat(searchCaptor.getAllValues()).hasSize(2);
        assertThat(searchCaptor.getAllValues().get(0).keyword()).isEqualTo("공지");
        assertThat(searchCaptor.getAllValues().get(0).page()).isEqualTo(99);
        assertThat(searchCaptor.getAllValues().get(0).size()).isEqualTo(10);
        assertThat(searchCaptor.getAllValues().get(0).offset()).isEqualTo(980);
        assertThat(searchCaptor.getAllValues().get(1).keyword()).isEqualTo("공지");
        assertThat(searchCaptor.getAllValues().get(1).page()).isEqualTo(2);
        assertThat(searchCaptor.getAllValues().get(1).size()).isEqualTo(10);
        assertThat(searchCaptor.getAllValues().get(1).offset()).isEqualTo(10);
        assertThat(page.search().page()).isEqualTo(2);
        assertThat(page.totalCount()).isEqualTo(12);
        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(page.pageNumbers()).containsExactly(1, 2);
    }

    private BDNoticePublicListItemResponseDto publicItem(Long noticeSeq, String title, String pinYn) {
        return new BDNoticePublicListItemResponseDto(
                noticeSeq,
                title,
                pinYn,
                0L,
                LocalDate.of(2026, 5, 31),
                null,
                null
        );
    }
}
