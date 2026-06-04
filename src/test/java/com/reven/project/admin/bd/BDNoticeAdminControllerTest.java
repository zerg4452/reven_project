package com.reven.project.admin.bd;

import com.reven.project.service.bd.BDNoticeService;
import com.reven.project.service.bd.support.BDFileStorageConstants;
import com.reven.project.service.bd.dto.BDNoticeAdminPageResponseDto;
import com.reven.project.service.bd.dto.BDNoticeAdminSearchRequestDto;
import com.reven.project.service.bd.dto.BDNoticeDetailResponseDto;
import com.reven.project.service.bd.dto.BDNoticeListItemResponseDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class BDNoticeAdminControllerTest {

    @Test
    void noticeListRendersWithDateSearchDefaults() throws Exception {
        BDNoticeService service = mock(BDNoticeService.class);
        LocalDate start = LocalDate.of(2026, 3, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);
        var search = new BDNoticeAdminSearchRequestDto(start, end, 1, 10);
        when(service.searchAdminNotices(any())).thenReturn(new BDNoticeAdminPageResponseDto(
                search,
                List.of(noticeItem(1L, "공지 1")),
                1,
                1
        ));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDNoticeAdminController(service)).build();

        mvc.perform(get("/admin/board/notice/list.do"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/notice/list"))
                .andExpect(model().attribute("dateFrom", start))
                .andExpect(model().attribute("dateTo", end))
                .andExpect(model().attributeExists("noticeList", "totalCount", "page"));

        verify(service).searchAdminNotices(any(BDNoticeAdminSearchRequestDto.class));
    }

    @Test
    void noticeWriteKeepsListContext() throws Exception {
        BDNoticeService service = mock(BDNoticeService.class);
        LocalDate start = LocalDate.of(2026, 4, 1);
        LocalDate end = LocalDate.of(2026, 6, 1);
        when(service.normalizedAdminSearch(any())).thenReturn(new BDNoticeAdminSearchRequestDto(start, end, 2, 10));
        when(service.findNotice(1L)).thenReturn(noticeDetail(1L, "수정 공지"));
        when(service.findNoticeThumbnail(1L)).thenReturn(null);
        when(service.findNoticeFiles(1L, BDFileStorageConstants.FILE_TYPE_ATTACH)).thenReturn(List.of());

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDNoticeAdminController(service)).build();

        mvc.perform(get("/admin/board/notice/write.do")
                        .param("noticeSeq", "1")
                        .param("dateFrom", "2026-04-01")
                        .param("dateTo", "2026-06-01")
                        .param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/notice/edit"))
                .andExpect(model().attribute("listDateFrom", start))
                .andExpect(model().attribute("listDateTo", end))
                .andExpect(model().attribute("listPage", 2));
    }

    @Test
    void noticeWriteAndSaveRedirectToList() throws Exception {
        BDNoticeService service = mock(BDNoticeService.class);
        when(service.normalizedAdminSearch(any())).thenReturn(new BDNoticeAdminSearchRequestDto(
                LocalDate.of(2026, 4, 5),
                LocalDate.of(2026, 6, 5),
                1,
                10
        ));
        when(service.findNotice(1L)).thenReturn(noticeDetail(1L, "수정 공지"));
        when(service.findNoticeThumbnail(1L)).thenReturn(null);
        when(service.findNoticeFiles(1L, BDFileStorageConstants.FILE_TYPE_ATTACH)).thenReturn(List.of());
        when(service.saveNotice(any(), any(), any(), any())).thenReturn(1L);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDNoticeAdminController(service)).build();

        mvc.perform(get("/admin/board/notice/write.do").param("noticeSeq", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/notice/edit"))
                .andExpect(model().attributeExists("notice"));

        mvc.perform(post("/admin/board/notice/insert.do")
                        .param("title", "신규 공지")
                        .param("publishYn", "Y")
                        .param("pinYn", "N")
                        .principal(auth()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/board/notice/list.do"))
                .andExpect(flash().attribute("noticeSavedMessage", "저장되었습니다."));
    }

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(
                "admin",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    private BDNoticeListItemResponseDto noticeItem(Long noticeSeq, String title) {
        return new BDNoticeListItemResponseDto(
                noticeSeq,
                title,
                "Y",
                "N",
                0L,
                LocalDate.of(2026, 5, 31),
                LocalDate.of(2026, 5, 31)
        );
    }

    private BDNoticeDetailResponseDto noticeDetail(Long noticeSeq, String title) {
        return new BDNoticeDetailResponseDto(
                noticeSeq,
                title,
                "본문",
                "Y",
                "N",
                0L,
                LocalDateTime.of(2026, 5, 31, 9, 0),
                "N",
                LocalDateTime.of(2026, 5, 31, 9, 0),
                "admin",
                LocalDateTime.of(2026, 5, 31, 9, 0),
                "admin"
        );
    }
}
