package com.reven.project.client.bd;

import com.reven.project.service.bd.BDNoticeService;
import com.reven.project.service.bd.dto.BDNoticeDetailResponseDto;
import com.reven.project.service.bd.dto.BDNoticeFileResponseDto;
import com.reven.project.service.bd.dto.BDNoticePublicListItemResponseDto;
import com.reven.project.service.bd.dto.BDNoticePublicPageResponseDto;
import com.reven.project.service.bd.dto.BDNoticePublicSearchRequestDto;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class BDNoticePublicControllerTest {

    @Test
    void publicNoticeListRendersPinnedAndPagedNotices() throws Exception {
        BDNoticeService service = mock(BDNoticeService.class);
        BDNoticePublicPageResponseDto page = new BDNoticePublicPageResponseDto(
                new BDNoticePublicSearchRequestDto("", 1, 10),
                List.of(publicItem(1L, "고정 공지", "Y")),
                List.of(publicItem(2L, "일반 공지", "N")),
                1,
                1
        );
        when(service.searchPublicNotices(any())).thenReturn(page);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDNoticePublicController(service)).build();

        mvc.perform(get("/board/notice/list.do"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/notice/list"))
                .andExpect(model().attribute("page", page));
    }

    @Test
    void publicNoticeDetailInvalidAccessWhenMissing() throws Exception {
        BDNoticeService service = mock(BDNoticeService.class);
        when(service.findPublicNotice(99L)).thenReturn(null);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDNoticePublicController(service)).build();

        mvc.perform(get("/board/notice/detail.do").param("noticeSeq", "99"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/notice/invalid-access"))
                .andExpect(model().attribute("message", "비정상적인 접근입니다."))
                .andExpect(model().attribute("redirectUrl", "/board/notice/list.do"));
    }

    @Test
    void publicNoticeDetailIncreasesViewCountOncePerCookie() throws Exception {
        BDNoticeService service = mock(BDNoticeService.class);
        BDNoticeDetailResponseDto notice = noticeDetail(1L, "공개 공지");
        when(service.findPublicNotice(1L)).thenReturn(notice);
        when(service.findPublicNoticeFilesForDetail(1L, "THUMB")).thenReturn(List.of());
        when(service.findPublicNoticeFilesForDetail(1L, "ATTACH")).thenReturn(List.of());

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDNoticePublicController(service)).build();

        var firstView = mvc.perform(get("/board/notice/detail.do").param("noticeSeq", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/notice/detail"))
                .andExpect(model().attribute("notice", notice))
                .andReturn();

        Cookie viewedCookie = firstView.getResponse().getCookie("bd_viewed_notice");
        assertNotNull(viewedCookie);

        mvc.perform(get("/board/notice/detail.do").param("noticeSeq", "1").cookie(viewedCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("client/notice/detail"));

        verify(service, times(1)).increaseViewCount(1L);
        verify(service, times(2)).findPublicNoticeFilesForDetail(1L, "THUMB");
        verify(service, times(2)).findPublicNoticeFilesForDetail(1L, "ATTACH");
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

    private BDNoticeDetailResponseDto noticeDetail(Long noticeSeq, String title) {
        return new BDNoticeDetailResponseDto(
                noticeSeq,
                title,
                "본문",
                "Y",
                "N",
                1L,
                LocalDateTime.of(2026, 5, 31, 9, 0),
                "N",
                LocalDateTime.of(2026, 5, 31, 9, 0),
                "admin",
                LocalDateTime.of(2026, 5, 31, 9, 0),
                "admin"
        );
    }
}
