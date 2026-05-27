package com.reven.project.service.bd;

import com.reven.project.service.bd.dto.BDPhotoBoardSaveCommand;
import com.reven.project.service.bd.dto.BDPhotoBoardSaveRequestDto;
import com.reven.project.service.bd.mapper.BDPhotoBoardMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BDPhotoBoardServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void insertKeepsUploadedFilesAndDoesNotDeleteNewlySavedAttachments() throws Exception {
        BDPhotoBoardMapper mapper = mock(BDPhotoBoardMapper.class);
        doAnswer(invocation -> {
            BDPhotoBoardSaveCommand command = invocation.getArgument(0);
            command.setPhotoSeq(1L);
            return null;
        }).when(mapper).insertPhotoBoard(any(BDPhotoBoardSaveCommand.class));

        BDPhotoBoardService service = new BDPhotoBoardService(mapper, tempDir.toString(), "/admin/board/photo/file.do", 5);

        MockMultipartFile upload = new MockMultipartFile(
                "uploadFiles",
                "sample.png",
                "image/png",
                new byte[] {0, 1, 2, 3}
        );

        Long photoSeq = service.savePhotoBoard(new BDPhotoBoardSaveRequestDto(null, "테스트 포토", "Y", "admin"), List.of(upload));

        assertThat(photoSeq).isEqualTo(1L);
        verify(mapper).insertPhotoBoardFile(any());
        verify(mapper, never()).deletePhotoBoardFiles(any(), any());
        try (var paths = Files.list(tempDir)) {
            assertThat(paths.count()).isGreaterThan(0);
        }
    }
}
