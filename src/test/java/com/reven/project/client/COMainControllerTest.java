package com.reven.project.client;

import com.reven.project.client.bd.BDAiNewsPublicController;
import com.reven.project.client.main.COMainController;
import com.reven.project.client.sa.SAPublicSurveyController;
import com.reven.project.service.bd.BDAiNewsService;
import com.reven.project.service.bd.dto.BDAiNewsDetailResponseDto;
import com.reven.project.service.sa.SASurveyService;
import com.reven.project.service.sa.SASurveySubmitService;
import com.reven.project.service.sa.dto.SADto;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class COMainControllerTest {

    @Test
    void mainRoutesRenderPublicMainPageWithSurveyAndNewsCards() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        BDAiNewsService newsService = mock(BDAiNewsService.class);
        when(surveyService.findPublicSurveySummaries(3)).thenReturn(List.of(survey("survey-1", "진행 설문", "Y")));
        when(newsService.findPublishedAiNews(3)).thenReturn(List.of(news(1L, "AI 뉴스")));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new COMainController(surveyService, newsService)).build();

        for (String path : List.of("/", "/main.do", "/index.do")) {
            mvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(view().name("client/main/index"))
                    .andExpect(model().attributeExists("surveys", "newsList"));
        }
    }

    @Test
    void publicNewsListUsesPublishedNewsAndSearchKeyword() throws Exception {
        BDAiNewsService newsService = mock(BDAiNewsService.class);
        when(newsService.searchPublishedAiNews(any())).thenReturn(List.of(news(2L, "검색 뉴스")));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDAiNewsPublicController(newsService)).build();

        mvc.perform(get("/news/ai/list.do").param("keyword", "AI"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/news/ai-list"))
                .andExpect(model().attributeExists("newsList", "keyword"));

        verify(newsService).searchPublishedAiNews("AI");
    }

    @Test
    void publicNewsDetailRendersOnlyPublishedNews() throws Exception {
        BDAiNewsService newsService = mock(BDAiNewsService.class);
        when(newsService.findPublishedAiNewsDetail(7L)).thenReturn(new BDAiNewsDetailResponseDto(
                7L, "slug", "상세 뉴스", "AI News", "요약", "본문", "[]", "",
                "Y", "N", LocalDate.of(2026, 5, 26), null, null, null, null, null, null
        ));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDAiNewsPublicController(newsService)).build();

        mvc.perform(get("/news/ai/detail.do").param("newsSeq", "7"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/news/ai-detail"))
                .andExpect(model().attributeExists("news"));
    }

    @Test
    void publicSurveyListIncludesClosedSurveysForDimmedCards() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        SASurveySubmitService submitService = mock(SASurveySubmitService.class);
        when(surveyService.findPublicSurveyCards()).thenReturn(List.of(
                survey("open", "진행 설문", "Y"),
                survey("closed", "마감 설문", "N")
        ));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAPublicSurveyController(surveyService, submitService)).build();

        mvc.perform(get("/surveys/list.do"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/survey/list"))
                .andExpect(model().attributeExists("surveys"));
    }

    private SADto.SurveyListItem survey(String uid, String title, String useYn) {
        SADto.SurveyListItem survey = new SADto.SurveyListItem();
        survey.surveyUid = uid;
        survey.title = title;
        survey.useYn = useYn;
        survey.fieldCount = 3;
        survey.regDate = LocalDate.of(2026, 5, 26);
        return survey;
    }

    private BDAiNewsDetailResponseDto news(Long newsSeq, String title) {
        return new BDAiNewsDetailResponseDto(
                newsSeq,
                "slug-" + newsSeq,
                title,
                "AI News",
                "요약",
                "본문",
                "[]",
                "",
                "Y",
                "N",
                LocalDate.of(2026, 5, 26),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
