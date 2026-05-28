package com.reven.project.service.co;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reven.project.service.co.dto.COAdminMenuResponseDto;
import com.reven.project.service.co.dto.COAdminMenuSaveRequestDto;
import com.reven.project.service.co.mapper.COAdminMenuMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
                menu("news", "", 1, "게시판", "/admin/board", "[\"/admin/board\"]", "group", "Y", 40),
                menu("news_ai_news", "news", 2, "AI News", "/admin/board/ai-news/list.do", "[\"/admin/board/ai-news\"]", "board", "Y", 10),
                menu("news_photo_board", "news", 2, "포토게시판", "/admin/board/photo/list.do", "[\"/admin/board/photo\"]", "board", "Y", 20)
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

        var aiNewsNavigation = service.adminNavigation("/admin/board/ai-news/detail.do");
        assertThat(aiNewsNavigation.gnbItems().get(2).menuCode()).isEqualTo("news");
        assertThat(aiNewsNavigation.gnbItems().get(2).active()).isTrue();
        assertThat(aiNewsNavigation.gnbItems().get(2).children())
                .extracting("menuCode")
                .containsExactly("news_ai_news", "news_photo_board");
        assertThat(aiNewsNavigation.gnbItems().get(2).children().get(0).active()).isTrue();

        var photoNavigation = service.adminNavigation("/admin/board/photo/list.do");
        assertThat(photoNavigation.gnbItems().get(2).active()).isTrue();
        assertThat(photoNavigation.gnbItems().get(2).children().get(1).menuCode()).isEqualTo("news_photo_board");
        assertThat(photoNavigation.gnbItems().get(2).children().get(1).active()).isTrue();
    }

    @Test
    void adminNavigationKeepsNestedTreeNodesInTheReturnedStructure() {
        COAdminMenuMapper mapper = mock(COAdminMenuMapper.class);
        when(mapper.selectAdminMenus()).thenReturn(List.of(
                menu("survey_operation", "", 1, "설문 운영", "/admin/surveys/list.do", "[\"/admin/surveys\"]", "group", "Y", 30),
                menu("survey_manage", "survey_operation", 2, "설문 관리", "/admin/surveys/list.do", "[\"/admin/surveys\"]", "page", "Y", 10),
                menu("survey_manage_detail", "survey_manage", 3, "설문 상세", "/admin/surveys/detail.do", "[\"/admin/surveys/detail.do\"]", "page", "Y", 10)
        ));
        COAdminMenuService service = new COAdminMenuService(mapper, new ObjectMapper());

        var navigation = service.adminNavigation("/admin/surveys/detail.do");

        assertThat(navigation.gnbItems())
                .extracting("menuCode")
                .containsExactly("survey_operation");
        assertThat(navigation.gnbItems().get(0).children())
                .extracting("menuCode")
                .containsExactly("survey_manage");
        assertThat(navigation.gnbItems().get(0).children().get(0).children())
                .extracting("menuCode")
                .containsExactly("survey_manage_detail");
    }

    @Test
    void saveMenuRejectsMenuCodeChangesForExistingMenus() {
        COAdminMenuMapper mapper = mock(COAdminMenuMapper.class);
        when(mapper.selectAdminMenuBySeq(10L)).thenReturn(menu(
                "survey_manage",
                "survey_operation",
                2,
                "설문 관리",
                "/admin/surveys/list.do",
                "[\"/admin/surveys\"]",
                "page",
                "Y",
                10
        ));
        COAdminMenuService service = new COAdminMenuService(mapper, new ObjectMapper());

        COAdminMenuSaveRequestDto request = new COAdminMenuSaveRequestDto(
                10L,
                "survey_manage_v2",
                "survey_operation",
                "설문 관리",
                "/admin/surveys/list.do",
                "[\"/admin/surveys\"]",
                "page",
                "",
                "Y",
                10,
                "system"
        );

        assertThatThrownBy(() -> service.saveMenu(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("메뉴 코드는 수정할 수 없습니다.");
        verify(mapper, never()).updateAdminMenu(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyString());
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
