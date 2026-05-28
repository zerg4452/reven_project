package com.reven.project.admin.bd;

import com.reven.project.service.bd.BDPhotoBoardService;
import com.reven.project.service.bd.dto.BDPhotoBoardDetailResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardFileResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardListItemResponseDto;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class BDPhotoBoardControllerTest {

    @TempDir
    Path tempDir;

    @Test
    void photoBoardRoutesRenderExpectedViews() throws Exception {
        BDPhotoBoardService service = mock(BDPhotoBoardService.class);
        when(service.findPhotoBoards()).thenReturn(List.of(photo(1L, "포토 1")));
        when(service.findPhotoBoard(1L)).thenReturn(detail(1L, "포토 1"));
        when(service.findPhotoBoardFiles(1L)).thenReturn(List.of(file(11L, 1L, "sample.jpg")));
        when(service.findPhotoBoardFile(11L)).thenReturn(file(11L, 1L, "sample.jpg"));
        Path downloadPath = tempDir.resolve("sample.jpg");
        Files.write(downloadPath, new byte[] {1, 2, 3});
        when(service.resolvePhotoBoardFilePath(11L)).thenReturn(downloadPath);
        when(service.savePhotoBoard(any(), any(), any())).thenReturn(1L);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDPhotoBoardController(service)).build();

        mvc.perform(get("/admin/board/photo/list.do"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/photo/list"))
                .andExpect(model().attributeExists("photoList", "totalCount"));

        mvc.perform(get("/admin/board/photo/detail.do").param("photoSeq", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/photo/detail"))
                .andExpect(model().attributeExists("photo", "photoFiles"));

        mvc.perform(get("/admin/board/photo/write.do").param("photoSeq", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/photo/edit"))
                .andExpect(model().attributeExists("photo", "photoFiles"));

        mvc.perform(post("/admin/board/photo/insert.do")
                        .param("title", "새 포토")
                        .param("publishYn", "Y")
                        .principal(auth()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/board/photo/list.do"))
                .andExpect(flash().attribute("photoSavedMessage", "저장되었습니다."));

        mvc.perform(post("/admin/board/photo/update.do")
                        .param("photoSeq", "1")
                        .param("title", "수정 포토")
                        .param("publishYn", "N")
                        .param("keepPhotoFileSeqs", "11")
                        .principal(auth()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/board/photo/list.do"))
                .andExpect(flash().attribute("photoSavedMessage", "저장되었습니다."));

        mvc.perform(post("/admin/board/photo/delete.do")
                        .param("photoSeq", "1")
                        .principal(auth()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/board/photo/list.do"));

        mvc.perform(get("/admin/board/photo/download.do").param("photoFileSeq", "11"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")));

        verify(service).deletePhotoBoard(1L, "admin");
    }

    @Test
    void insertValidationFailureRedirectsBackToWriteFormWithError() throws Exception {
        BDPhotoBoardService service = mock(BDPhotoBoardService.class);
        doThrow(new IllegalArgumentException("제목을 입력해 주세요."))
                .when(service).savePhotoBoard(any(), any(), any());

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDPhotoBoardController(service)).build();

        mvc.perform(post("/admin/board/photo/insert.do")
                        .param("title", "")
                        .param("publishYn", "Y")
                        .principal(auth()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/board/photo/write.do"))
                .andExpect(flash().attribute("error", "제목을 입력해 주세요."));
    }

    @Test
    void updateValidationFailureRedirectsBackToWriteFormWithError() throws Exception {
        BDPhotoBoardService service = mock(BDPhotoBoardService.class);
        doThrow(new IllegalArgumentException("최대 업로드 갯수는 5개입니다."))
                .when(service).savePhotoBoard(any(), any(), any());

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDPhotoBoardController(service)).build();

        mvc.perform(post("/admin/board/photo/update.do")
                        .param("photoSeq", "1")
                        .param("title", "수정 포토")
                        .param("publishYn", "Y")
                        .principal(auth()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/board/photo/write.do?photoSeq=1"))
                .andExpect(flash().attribute("error", "최대 업로드 갯수는 5개입니다."));
    }

    @Test
    void detailReturnsNotFoundWhenPhotoDoesNotExist() throws Exception {
        BDPhotoBoardService service = mock(BDPhotoBoardService.class);
        when(service.findPhotoBoard(999L)).thenReturn(null);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDPhotoBoardController(service)).build();

        mvc.perform(get("/admin/board/photo/detail.do").param("photoSeq", "999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void fileEndpointReturnsInlineAttachment() throws Exception {
        BDPhotoBoardService service = mock(BDPhotoBoardService.class);
        when(service.findPhotoBoardFile(11L)).thenReturn(file(11L, 1L, "sample.jpg"));
        Path filePath = tempDir.resolve("sample.jpg");
        Files.write(filePath, new byte[] {1, 2, 3});
        when(service.resolvePhotoBoardFilePath(11L)).thenReturn(filePath);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDPhotoBoardController(service)).build();

        mvc.perform(get("/admin/board/photo/file.do").param("photoFileSeq", "11"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")));
    }

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(
                "admin",
                "admin",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    private BDPhotoBoardListItemResponseDto photo(Long photoSeq, String title) {
        return new BDPhotoBoardListItemResponseDto(photoSeq, title, 1, "Y", LocalDate.of(2026, 5, 27), LocalDate.of(2026, 5, 27));
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

    private BDPhotoBoardFileResponseDto file(Long fileSeq, Long photoSeq, String fileName) {
        return new BDPhotoBoardFileResponseDto(
                fileSeq,
                photoSeq,
                fileName,
                "stored.jpg",
                "2026/05/27",
                "image/jpeg",
                123L,
                1,
                "N",
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "admin",
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "admin",
                "/admin/board/photo/file.do?photoFileSeq=11"
        );
    }
}
