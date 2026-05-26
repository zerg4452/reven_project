package com.reven.project.service.co;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reven.project.service.co.dto.COAdminMenuResponseDto;
import com.reven.project.service.co.mapper.COAdminMenuMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class COAdminMenuServiceTest {

    @Test
    void adminNavigationUsesManagedMenuDataAndMarksCurrentBranchActive() {
        COAdminMenuMapper mapper = mock(COAdminMenuMapper.class);
        when(mapper.selectAdminMenus()).thenReturn(List.of(
                menu("admin_home", "", 1, "관리자 홈", "/admin/home.do", "[\"/admin/home.do\"]", "page", "Y", 10),
                menu("hidden", "", 1, "숨김", "/admin/hidden.do", "[\"/admin/hidden.do\"]", "page", "N", 20),
                menu("survey_operation", "", 1, "설문 운영", "/admin/surveys/list.do", "[\"/admin/surveys\",\"/admin/survey-submissions\"]", "group", "Y", 30),
                menu("survey_manage", "survey_operation", 2, "설문 관리", "/admin/surveys/list.do", "[\"/admin/surveys\"]", "page", "Y", 10),
                menu("survey_history", "survey_operation", 2, "설문 이력 관리", "/admin/survey-submissions/list.do", "[\"/admin/survey-submissions\"]", "page", "Y", 20),
                menu("news", "", 1, "뉴스", "/admin/news/list.do", "[\"/admin/news\",\"/admin/news/ai-news\"]", "group", "Y", 40),
                menu("news_ai_news", "news", 2, "AI News", "/admin/news/list.do", "[\"/admin/news\",\"/admin/news/ai-news\"]", "board", "Y", 10)
        ));
        COAdminMenuService service = new COAdminMenuService(mapper, new ObjectMapper());

        var navigation = service.adminNavigation("/admin/survey-submissions/detail.do");

        assertThat(navigation.gnbItems())
                .extracting("menuCode")
                .containsExactly("admin_home", "survey_operation", "news");
        assertThat(navigation.activeRoot().menuCode()).isEqualTo("survey_operation");
        assertThat(navigation.lnbItems())
                .extracting("menuCode")
                .containsExactly("survey_manage", "survey_history");
        assertThat(navigation.lnbItems().get(1).active()).isTrue();

        var newsNavigation = service.adminNavigation("/admin/news/ai-news/detail.do");
        assertThat(newsNavigation.gnbItems().get(2).menuCode()).isEqualTo("news");
        assertThat(newsNavigation.gnbItems().get(2).active()).isTrue();
        assertThat(newsNavigation.gnbItems().get(2).children())
                .extracting("menuCode")
                .containsExactly("news_ai_news");
        assertThat(newsNavigation.gnbItems().get(2).children().get(0).active()).isTrue();
    }

    private COAdminMenuResponseDto menu(
            String menuCode,
            String parentMenuCode,
            Integer depthNo,
            String menuName,
            String menuUrl,
            String matchUrlsJson,
            String menuType,
            String useYn,
            Integer sortOrder
    ) {
        return new COAdminMenuResponseDto(
                1L,
                menuCode,
                parentMenuCode,
                depthNo,
                menuName,
                menuUrl,
                matchUrlsJson,
                null,
                menuType,
                "",
                useYn,
                "N",
                sortOrder,
                LocalDate.of(2026, 5, 27),
                LocalDate.of(2026, 5, 27),
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "system",
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "system"
        );
    }
}
