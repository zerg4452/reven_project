// AI News 관리자 목록 화면의 상태 필터 구성을 검증한다.
package com.reven.project.admin.bd;

import com.reven.project.service.bd.dto.BDAiNewsSaveRequestDto;
import com.reven.project.service.bd.BDAiNewsService;
import com.reven.project.service.bd.dto.BDAiNewsListItemResponseDto;
import com.reven.project.service.bd.dto.BDAiNewsPageResponseDto;
import com.reven.project.service.bd.dto.BDAiNewsSearchRequestDto;
import java.time.LocalDate;
import java.util.List;
import java.security.Principal;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BDAiNewsAdminControllerTest {

    @Test
    void listExposesOnlyCurrentStatusFilters() {
        BDAiNewsService service = mock(BDAiNewsService.class);
        when(service.searchAiNews(any())).thenReturn(new BDAiNewsPageResponseDto(
                new BDAiNewsSearchRequestDto(null, null, null, null, List.of("P", "Y", "E"), 0, 10),
                0L,
                List.of(news(1L, "샘플", "P"))
        ));

        BDAiNewsAdminController controller = new BDAiNewsAdminController(service);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.list(new BDAiNewsSearchRequestDto(null, null, null, null, null, null, null), null, null, model);

        assertThat(view).isEqualTo("admin/news/list");
        assertThat((List<?>) model.getAttribute("statusOptions"))
                .extracting("code")
                .containsExactly("P", "Y", "E");
    }

    @Test
    void insertRedirectsToListWithFlashMessage() {
        BDAiNewsService service = mock(BDAiNewsService.class);
        when(service.saveAiNews(any())).thenReturn(11L);

        BDAiNewsAdminController controller = new BDAiNewsAdminController(service);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.insert(requestDto(null), principal(), redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/board/ai-news/list.do");
        assertThat(redirectAttributes.getFlashAttributes().get("aiNewsSavedMessage"))
                .isEqualTo("AI News를 등록했습니다.");
    }

    @Test
    void updateRedirectsToListWithFlashMessage() {
        BDAiNewsService service = mock(BDAiNewsService.class);
        when(service.saveAiNews(any())).thenReturn(11L);

        BDAiNewsAdminController controller = new BDAiNewsAdminController(service);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.update(11L, requestDto(11L), principal(), redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/board/ai-news/list.do");
        assertThat(redirectAttributes.getFlashAttributes().get("aiNewsSavedMessage"))
                .isEqualTo("AI News를 수정했습니다.");
    }

    @Test
    void deleteRedirectsToListWithFlashMessage() {
        BDAiNewsService service = mock(BDAiNewsService.class);

        BDAiNewsAdminController controller = new BDAiNewsAdminController(service);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.delete(11L, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/board/ai-news/list.do");
        assertThat(redirectAttributes.getFlashAttributes().get("aiNewsSavedMessage"))
                .isEqualTo("AI News를 삭제했습니다.");
        verify(service).deleteAiNews(11L);
    }

    private BDAiNewsListItemResponseDto news(Long newsSeq, String title, String status) {
        return new BDAiNewsListItemResponseDto(
                newsSeq,
                title,
                "slug-" + newsSeq,
                "AI News",
                status,
                LocalDate.of(2026, 5, 30),
                LocalDate.of(2026, 5, 29),
                LocalDate.of(2026, 5, 30)
        );
    }

    private BDAiNewsSaveRequestDto requestDto(Long newsSeq) {
        return new BDAiNewsSaveRequestDto(
                newsSeq,
                "ai-news-sample",
                "샘플",
                "AI News",
                "요약",
                "본문",
                "[]",
                "[]",
                LocalDate.of(2026, 5, 30),
                "P",
                "admin"
        );
    }

    private Principal principal() {
        return () -> "admin";
    }
}
