package com.reven.project.client;

import com.reven.project.client.bd.BDAiNewsPublicController;
import com.reven.project.client.bd.BDPhotoBoardPublicController;
import com.reven.project.client.main.COMainController;
import com.reven.project.client.sa.SAPublicSurveyController;
import com.reven.project.service.bd.BDAiNewsService;
import com.reven.project.service.bd.BDPhotoBoardService;
import com.reven.project.service.bd.dto.BDAiNewsDetailResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardDetailResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardFileResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicPageResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicSearchRequestDto;
import com.reven.project.service.sa.SASurveyService;
import com.reven.project.service.sa.SASurveySubmitService;
import com.reven.project.service.sa.dto.SASurveyDto;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class COMainControllerTest {

    @TempDir
    Path tempDir;

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

        mvc.perform(get("/board/ai-news/list.do").param("keyword", "AI"))
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
                "Y", "N", LocalDate.of(2026, 5, 26), null, null, null, null, null, null, null
        ));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDAiNewsPublicController(newsService)).build();

        mvc.perform(get("/board/ai-news/detail.do").param("newsSeq", "7"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/news/ai-detail"))
                .andExpect(model().attributeExists("news"));
    }

    @Test
    void publicPhotoListUsesSearchConditionAndRendersPhotoList() throws Exception {
        BDPhotoBoardService photoBoardService = mock(BDPhotoBoardService.class);
        BDPhotoBoardPublicPageResponseDto page = new BDPhotoBoardPublicPageResponseDto(
                new BDPhotoBoardPublicSearchRequestDto("봄", true, false, 2, 9),
                List.of(),
                0,
                0
        );
        when(photoBoardService.searchPublicPhotoBoards(any())).thenReturn(page);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDPhotoBoardPublicController(photoBoardService)).build();

        mvc.perform(get("/board/photo/list.do")
                        .param("keyword", "봄")
                        .param("imageOnly", "true")
                        .param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/photo/list"))
                .andExpect(model().attribute("page", page));

        verify(photoBoardService).searchPublicPhotoBoards(argThat(search ->
                "봄".equals(search.keyword())
                        && search.imageOnly()
                        && !search.videoOnly()
                        && search.page() == 2
                        && search.size() == 9
        ));
    }

    @Test
    void publicPhotoDetailInvalidAccessRendersAlertRedirectView() throws Exception {
        BDPhotoBoardService photoBoardService = mock(BDPhotoBoardService.class);
        when(photoBoardService.findPublicPhotoBoard(999L)).thenReturn(null);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDPhotoBoardPublicController(photoBoardService)).build();

        mvc.perform(get("/board/photo/detail.do").param("photoSeq", "999"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/photo/invalid-access"))
                .andExpect(model().attribute("message", "비정상적인 접근입니다."))
                .andExpect(model().attribute("redirectUrl", "/board/photo/list.do"));
    }

    @Test
    void publicPhotoDetailRendersPhotoAndFiles() throws Exception {
        BDPhotoBoardService photoBoardService = mock(BDPhotoBoardService.class);
        BDPhotoBoardDetailResponseDto photo = photo(1L, "공개 포토");
        List<BDPhotoBoardFileResponseDto> photoFiles = List.of(photoFile(11L, 1L, "sample.jpg"));
        when(photoBoardService.findPublicPhotoBoard(1L)).thenReturn(photo);
        when(photoBoardService.findPublicPhotoBoardFiles(1L)).thenReturn(photoFiles);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDPhotoBoardPublicController(photoBoardService)).build();

        mvc.perform(get("/board/photo/detail.do").param("photoSeq", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/photo/detail"))
                .andExpect(model().attribute("photo", photo))
                .andExpect(model().attribute("photoFiles", photoFiles));
    }

    @Test
    void publicPhotoFileReturnsNotFoundWhenMissing() throws Exception {
        BDPhotoBoardService photoBoardService = mock(BDPhotoBoardService.class);
        when(photoBoardService.findPublicPhotoBoardFile(404L)).thenReturn(null);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDPhotoBoardPublicController(photoBoardService)).build();

        mvc.perform(get("/board/photo/file.do").param("photoFileSeq", "404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void publicPhotoFileReturnsInlinePublicFile() throws Exception {
        BDPhotoBoardService photoBoardService = mock(BDPhotoBoardService.class);
        BDPhotoBoardFileResponseDto file = photoFile(11L, 1L, "sample.jpg");
        Path filePath = tempDir.resolve("sample.jpg");
        Files.write(filePath, new byte[] {1, 2, 3});
        when(photoBoardService.findPublicPhotoBoardFile(11L)).thenReturn(file);
        when(photoBoardService.resolvePublicPhotoBoardFilePath(11L)).thenReturn(filePath);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDPhotoBoardPublicController(photoBoardService)).build();

        mvc.perform(get("/board/photo/file.do").param("photoFileSeq", "11"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().string("Content-Disposition", containsString("inline")));
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

    private SASurveyDto.SurveyListItem survey(String uid, String title, String useYn) {
        SASurveyDto.SurveyListItem survey = new SASurveyDto.SurveyListItem();
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
                null,
                null
        );
    }

    private BDPhotoBoardDetailResponseDto photo(Long photoSeq, String title) {
        return new BDPhotoBoardDetailResponseDto(
                photoSeq,
                title,
                "Y",
                "N",
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "admin",
                LocalDateTime.of(2026, 5, 27, 0, 0),
                "admin",
                0L
        );
    }

    private BDPhotoBoardFileResponseDto photoFile(Long fileSeq, Long photoSeq, String fileName) {
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
                "/board/photo/file.do?photoFileSeq=11"
        );
    }
}
