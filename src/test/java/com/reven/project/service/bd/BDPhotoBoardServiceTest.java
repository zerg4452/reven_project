package com.reven.project.service.bd;

import com.reven.project.service.bd.dto.BDPhotoBoardDetailResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardFileResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardSaveCommand;
import com.reven.project.service.bd.dto.BDPhotoBoardSaveRequestDto;
import com.reven.project.service.bd.mapper.BDPhotoBoardMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BDPhotoBoardServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void insertKeepsUploadedFilesAndDoesNotDeleteExistingAttachments() throws Exception {
        BDPhotoBoardMapper mapper = mock(BDPhotoBoardMapper.class);
        doAnswer(invocation -> {
            BDPhotoBoardSaveCommand command = invocation.getArgument(0);
            command.setPhotoSeq(1L);
            return null;
        }).when(mapper).insertPhotoBoard(any(BDPhotoBoardSaveCommand.class));

        BDPhotoBoardService service = newService(mapper);

        MockMultipartFile upload = pngFile("sample.png");

        Long photoSeq = service.savePhotoBoard(new BDPhotoBoardSaveRequestDto(null, "테스트 포토", "Y", "admin"), List.of(upload));

        assertThat(photoSeq).isEqualTo(1L);
        verify(mapper).insertPhotoBoardFile(any());
        verify(mapper, never()).deletePhotoBoardFiles(any(), any());
        verify(mapper, never()).deletePhotoBoardFile(any(), any());
        try (var paths = Files.list(tempDir)) {
            assertThat(paths.count()).isGreaterThan(0);
        }
    }

    @Test
    void updateKeepsExistingAndAppendsNewAttachments() throws Exception {
        BDPhotoBoardMapper mapper = mock(BDPhotoBoardMapper.class);
        BDPhotoBoardDetailResponseDto existing = detail(1L, "기존 포토");
        BDPhotoBoardFileResponseDto oldFile = file(10L, 1L, "old.png", 1);

        when(mapper.selectPhotoBoardDetail(1L)).thenReturn(existing);
        when(mapper.selectPhotoBoardFiles(1L)).thenReturn(List.of(oldFile));

        BDPhotoBoardService service = newService(mapper);

        service.savePhotoBoard(
                new BDPhotoBoardSaveRequestDto(1L, "수정 포토", "Y", "admin"),
                List.of(pngFile("new-1.png"), pngFile("new-2.png")),
                List.of(10L)
        );

        verify(mapper).updatePhotoBoard(any(BDPhotoBoardSaveCommand.class));
        verify(mapper, never()).deletePhotoBoardFiles(any(), any());
        verify(mapper, never()).deletePhotoBoardFile(any(), any());
        verify(mapper, times(2)).insertPhotoBoardFile(any());
    }

    @Test
    void updateRemovesUncheckedExistingAndKeepsNewUpload() {
        BDPhotoBoardMapper mapper = mock(BDPhotoBoardMapper.class);
        when(mapper.selectPhotoBoardDetail(1L)).thenReturn(detail(1L, "기존 포토"));
        when(mapper.selectPhotoBoardFiles(1L)).thenReturn(List.of(file(10L, 1L, "old.png", 1)));

        BDPhotoBoardService service = newService(mapper);

        service.savePhotoBoard(
                new BDPhotoBoardSaveRequestDto(1L, "수정 포토", "Y", "admin"),
                List.of(pngFile("new.png")),
                List.of()
        );

        verify(mapper).deletePhotoBoardFile(10L, "admin");
        verify(mapper).insertPhotoBoardFile(any());
    }

    @Test
    void updateWithoutNewFilesKeepsCheckedExistingAttachments() {
        BDPhotoBoardMapper mapper = mock(BDPhotoBoardMapper.class);
        when(mapper.selectPhotoBoardDetail(1L)).thenReturn(detail(1L, "기존 포토"));
        when(mapper.selectPhotoBoardFiles(1L)).thenReturn(List.of(file(10L, 1L, "old.png", 1)));

        BDPhotoBoardService service = newService(mapper);

        service.savePhotoBoard(
                new BDPhotoBoardSaveRequestDto(1L, "제목만 수정", "N", "admin"),
                List.of(),
                List.of(10L)
        );

        verify(mapper).updatePhotoBoard(any(BDPhotoBoardSaveCommand.class));
        verify(mapper, never()).deletePhotoBoardFiles(any(), any());
        verify(mapper, never()).deletePhotoBoardFile(any(), any());
        verify(mapper, never()).insertPhotoBoardFile(any());
    }

    @Test
    void rejectsMoreThanMaxCombinedAttachments() {
        BDPhotoBoardMapper mapper = mock(BDPhotoBoardMapper.class);
        when(mapper.selectPhotoBoardDetail(1L)).thenReturn(detail(1L, "기존 포토"));
        when(mapper.selectPhotoBoardFiles(1L)).thenReturn(List.of(
                file(10L, 1L, "old-1.png", 1),
                file(11L, 1L, "old-2.png", 2),
                file(12L, 1L, "old-3.png", 3)
        ));

        BDPhotoBoardService service = newService(mapper);

        List<MultipartFile> uploads = IntStream.range(0, 3)
                .mapToObj(index -> (MultipartFile) pngFile("sample-" + index + ".png"))
                .toList();

        assertThatThrownBy(() -> service.savePhotoBoard(
                new BDPhotoBoardSaveRequestDto(1L, "테스트 포토", "Y", "admin"),
                uploads,
                List.of(10L, 11L, 12L)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최대 업로드 갯수는 5개입니다.");

        verify(mapper, never()).updatePhotoBoard(any());
    }

    @Test
    void rejectsInsertWithMoreThanMaxAttachments() {
        BDPhotoBoardMapper mapper = mock(BDPhotoBoardMapper.class);
        BDPhotoBoardService service = newService(mapper, 5);

        List<MultipartFile> uploads = IntStream.range(0, 6)
                .mapToObj(index -> (MultipartFile) pngFile("sample-" + index + ".png"))
                .toList();

        assertThatThrownBy(() -> service.savePhotoBoard(
                new BDPhotoBoardSaveRequestDto(null, "테스트 포토", "Y", "admin"),
                uploads
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최대 업로드 갯수는 5개입니다.");

        verify(mapper, never()).insertPhotoBoard(any());
    }

    @Test
    void rejectsUpdateWithoutAnyRetainedOrNewAttachments() {
        BDPhotoBoardMapper mapper = mock(BDPhotoBoardMapper.class);
        when(mapper.selectPhotoBoardDetail(1L)).thenReturn(detail(1L, "기존 포토"));
        when(mapper.selectPhotoBoardFiles(1L)).thenReturn(List.of(file(10L, 1L, "old.png", 1)));

        BDPhotoBoardService service = newService(mapper);

        assertThatThrownBy(() -> service.savePhotoBoard(
                new BDPhotoBoardSaveRequestDto(1L, "수정 포토", "Y", "admin"),
                List.of(),
                List.of()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("첨부 파일을 최소 1개 이상 업로드해 주세요.");
    }

    @Test
    void rejectsDisallowedExtensionAndMimeType() {
        BDPhotoBoardMapper mapper = mock(BDPhotoBoardMapper.class);
        BDPhotoBoardService service = newService(mapper);

        MockMultipartFile invalidExtension = new MockMultipartFile(
                "uploadFiles",
                "sample.exe",
                "application/octet-stream",
                new byte[] {0, 1}
        );
        MockMultipartFile invalidMime = new MockMultipartFile(
                "uploadFiles",
                "sample.png",
                "application/octet-stream",
                new byte[] {0, 1}
        );

        assertThatThrownBy(() -> service.savePhotoBoard(
                new BDPhotoBoardSaveRequestDto(null, "테스트 포토", "Y", "admin"),
                List.of(invalidExtension)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파일 형식");

        assertThatThrownBy(() -> service.savePhotoBoard(
                new BDPhotoBoardSaveRequestDto(null, "테스트 포토", "Y", "admin"),
                List.of(invalidMime)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MIME");

        verify(mapper, never()).insertPhotoBoardFile(any());
    }

    @Test
    void deleteSoftDeletesPostAndSchedulesFileCleanup() {
        BDPhotoBoardMapper mapper = mock(BDPhotoBoardMapper.class);
        when(mapper.selectPhotoBoardDetail(1L)).thenReturn(detail(1L, "삭제 대상"));
        when(mapper.selectPhotoBoardFiles(1L)).thenReturn(List.of(file(10L, 1L, "old.png", 1)));

        BDPhotoBoardService service = newService(mapper);

        service.deletePhotoBoard(1L, "admin");

        verify(mapper).deletePhotoBoardFiles(eq(1L), eq("admin"));
        verify(mapper).deletePhotoBoard(eq(1L), eq("admin"));
    }

    private BDPhotoBoardService newService(BDPhotoBoardMapper mapper) {
        return newService(mapper, 5);
    }

    private BDPhotoBoardService newService(BDPhotoBoardMapper mapper, int maxFiles) {
        return new BDPhotoBoardService(mapper, tempDir.toString(), "/admin/board/photo/file.do", maxFiles);
    }

    private MockMultipartFile pngFile(String fileName) {
        return new MockMultipartFile("uploadFiles", fileName, "image/png", new byte[] {0, 1, 2, 3});
    }

    private BDPhotoBoardDetailResponseDto detail(Long photoSeq, String title) {
        return new BDPhotoBoardDetailResponseDto(
                photoSeq,
                title,
                "Y",
                "N",
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "admin",
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "admin"
        );
    }

    private BDPhotoBoardFileResponseDto file(Long fileSeq, Long photoSeq, String fileName, int sortOrder) {
        return new BDPhotoBoardFileResponseDto(
                fileSeq,
                photoSeq,
                fileName,
                "stored.png",
                "2026/05/27",
                "image/png",
                123L,
                sortOrder,
                "N",
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "admin",
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "admin",
                "/admin/board/photo/file.do?photoFileSeq=" + fileSeq
        );
    }
}
