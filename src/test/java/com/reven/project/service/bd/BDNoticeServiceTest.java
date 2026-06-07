// 공개 공지사항 페이지 보정과 페이징 데이터를 검증하는 테스트
package com.reven.project.service.bd;

import com.reven.project.service.bd.dto.BDNoticeDetailResponseDto;
import com.reven.project.service.bd.dto.BDNoticeFileResponseDto;
import com.reven.project.service.bd.dto.BDNoticePublicListItemResponseDto;
import com.reven.project.service.bd.dto.BDNoticePublicPageResponseDto;
import com.reven.project.service.bd.dto.BDNoticePublicSearchRequestDto;
import com.reven.project.service.bd.dto.BDNoticeSaveCommand;
import com.reven.project.service.bd.dto.BDNoticeSaveRequestDto;
import com.reven.project.service.bd.mapper.BDNoticeMapper;
import com.reven.project.service.bd.support.BDFileStorageConstants;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    @Test
    void saveInsertStoresAttachmentOnDiskAndInsertsFileMeta() throws Exception {
        BDNoticeMapper mapper = mock(BDNoticeMapper.class);
        doAnswer(invocation -> {
            BDNoticeSaveCommand command = invocation.getArgument(0);
            command.setNoticeSeq(1L);
            return null;
        }).when(mapper).insertNotice(any(BDNoticeSaveCommand.class));

        BDNoticeService service = newService(mapper);

        Long noticeSeq = service.saveNotice(saveRequest(null, "신규 공지"), null, List.of(attachFile("notice.pdf")), null);

        assertThat(noticeSeq).isEqualTo(1L);
        verify(mapper).insertNoticeFile(any());
        verify(mapper, never()).deleteNoticeFile(any(), any());
        try (var paths = Files.list(tempDir)) {
            assertThat(paths.count()).isGreaterThan(0);
        }
    }

    @Test
    void saveUpdateRemovesUncheckedExistingAndStoresNewAttachment() {
        BDNoticeMapper mapper = mock(BDNoticeMapper.class);
        when(mapper.selectNoticeDetail(1L)).thenReturn(noticeDetail(1L, "기존 공지"));
        when(mapper.selectNoticeFiles(1L, BDFileStorageConstants.FILE_TYPE_ATTACH))
                .thenReturn(List.of(noticeFile(10L, BDFileStorageConstants.FILE_TYPE_ATTACH)));

        BDNoticeService service = newService(mapper);

        service.saveNotice(saveRequest(1L, "수정 공지"), null, List.of(attachFile("new.pdf")), List.of());

        verify(mapper).updateNotice(any());
        verify(mapper).deleteNoticeFile(10L, "admin");
        verify(mapper).insertNoticeFile(any());
    }

    @Test
    void saveReplacesExistingThumbnailWithNewUpload() {
        BDNoticeMapper mapper = mock(BDNoticeMapper.class);
        when(mapper.selectNoticeDetail(1L)).thenReturn(noticeDetail(1L, "기존 공지"));
        when(mapper.selectNoticeFiles(1L, BDFileStorageConstants.FILE_TYPE_THUMB))
                .thenReturn(List.of(noticeFile(5L, BDFileStorageConstants.FILE_TYPE_THUMB)));

        BDNoticeService service = newService(mapper);

        service.saveNotice(saveRequest(1L, "썸네일 교체"), thumbnailFile("thumb.png"), List.of(), List.of());

        verify(mapper).updateNotice(any());
        verify(mapper).deleteNoticeFile(5L, "admin");
        verify(mapper).insertNoticeFile(any());
    }

    @Test
    void deleteSoftDeletesNoticeAndSchedulesFileCleanup() {
        BDNoticeMapper mapper = mock(BDNoticeMapper.class);
        when(mapper.selectNoticeDetail(1L)).thenReturn(noticeDetail(1L, "삭제 대상"));
        when(mapper.selectNoticeFiles(1L, null))
                .thenReturn(List.of(noticeFile(10L, BDFileStorageConstants.FILE_TYPE_ATTACH)));

        BDNoticeService service = newService(mapper);

        service.deleteNotice(1L, "admin");

        verify(mapper).deleteNoticeFiles(eq(1L), eq("admin"));
        verify(mapper).deleteNotice(eq(1L), eq("admin"));
    }

    @Test
    void resolveNoticeFilePathReturnsStoredPathAndNullForMissing() {
        BDNoticeMapper mapper = mock(BDNoticeMapper.class);
        when(mapper.selectNoticeFile(10L)).thenReturn(noticeFile(10L, BDFileStorageConstants.FILE_TYPE_ATTACH));
        when(mapper.selectNoticeFile(99L)).thenReturn(null);

        BDNoticeService service = newService(mapper);

        assertThat(service.resolveNoticeFilePath(10L))
                .isEqualTo(tempDir.resolve("2026/05/27/stored.pdf").toAbsolutePath().normalize());
        assertThat(service.resolveNoticeFilePath(99L)).isNull();
    }

    @Test
    void resolvePublicNoticeFilePathReturnsStoredPathAndNullForMissing() {
        BDNoticeMapper mapper = mock(BDNoticeMapper.class);
        when(mapper.selectPublicNoticeFile(10L)).thenReturn(noticeFile(10L, BDFileStorageConstants.FILE_TYPE_ATTACH));
        when(mapper.selectPublicNoticeFile(99L)).thenReturn(null);

        BDNoticeService service = newService(mapper);

        assertThat(service.resolvePublicNoticeFilePath(10L))
                .isEqualTo(tempDir.resolve("2026/05/27/stored.pdf").toAbsolutePath().normalize());
        assertThat(service.resolvePublicNoticeFilePath(99L)).isNull();
        assertThat(service.resolvePublicNoticeFilePath(null)).isNull();
    }

    private BDNoticeService newService(BDNoticeMapper mapper) {
        return new BDNoticeService(mapper, tempDir.toString(), "/board/notice/file.do", 10, 5, 20);
    }

    private BDNoticeSaveRequestDto saveRequest(Long noticeSeq, String title) {
        return new BDNoticeSaveRequestDto(noticeSeq, title, "본문", null, "Y", "N", "admin");
    }

    private MockMultipartFile attachFile(String fileName) {
        return new MockMultipartFile("attachFiles", fileName, "application/pdf", new byte[] {0, 1, 2, 3});
    }

    private MockMultipartFile thumbnailFile(String fileName) {
        return new MockMultipartFile("thumbnailFile", fileName, "image/png", new byte[] {0, 1, 2, 3});
    }

    private BDNoticeDetailResponseDto noticeDetail(Long noticeSeq, String title) {
        return new BDNoticeDetailResponseDto(
                noticeSeq,
                title,
                "본문",
                "Y",
                "N",
                0L,
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "N",
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "admin",
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "admin"
        );
    }

    private BDNoticeFileResponseDto noticeFile(Long noticeFileSeq, String fileType) {
        return new BDNoticeFileResponseDto(
                noticeFileSeq,
                1L,
                fileType,
                "notice.pdf",
                "stored.pdf",
                "2026/05/27",
                "application/pdf",
                123L,
                1,
                "N",
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "admin",
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "admin",
                "/board/notice/file.do?noticeFileSeq=" + noticeFileSeq
        );
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
