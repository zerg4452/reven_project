package com.reven.project.admin.sa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reven.project.service.sa.SASurveyService;
import com.reven.project.service.sa.SASurveyStatisticsService;
import com.reven.project.service.sa.dto.SASurveyDto;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SAAdminSurveyStatisticsControllerTest {

    private MockMvc mvc(SASurveyService surveyService, SASurveyStatisticsService statsService) {
        return MockMvcBuilders.standaloneSetup(
                new SAAdminSurveyStatisticsController(surveyService, statsService, new ObjectMapper())
        ).build();
    }

    @Test
    void statisticsReturnsViewWithModelAttributes() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        SASurveyStatisticsService statsService = mock(SASurveyStatisticsService.class);

        SASurveyDto.SurveyDetail survey = new SASurveyDto.SurveyDetail();
        survey.surveySeq = 1L;
        survey.surveyUid = "test-uid";
        survey.title = "테스트 설문";
        survey.fields = List.of();

        SASurveyDto.SurveyStatistics stats = new SASurveyDto.SurveyStatistics();

        when(surveyService.findSurvey("test-uid")).thenReturn(survey);
        when(statsService.getStatistics(survey)).thenReturn(stats);

        mvc(surveyService, statsService)
                .perform(get("/admin/surveys/test-uid/statistics.do"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/survey/statistics"))
                .andExpect(model().attributeExists("survey", "stats", "statsJson"));
    }

    @Test
    void statisticsRedirectsToListWhenSurveyNotFound() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        SASurveyStatisticsService statsService = mock(SASurveyStatisticsService.class);

        when(surveyService.findSurvey("missing")).thenThrow(new IllegalArgumentException("not found"));

        mvc(surveyService, statsService)
                .perform(get("/admin/surveys/missing/statistics.do"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/surveys/list.do"))
                .andExpect(flash().attribute("surveySavedMessage", "존재하지 않는 설문입니다."));
    }

    @Test
    void statisticsDoesNotCallStatsServiceWhenSurveyNotFound() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        SASurveyStatisticsService statsService = mock(SASurveyStatisticsService.class);

        when(surveyService.findSurvey("bad-uid")).thenThrow(new IllegalArgumentException("not found"));

        mvc(surveyService, statsService)
                .perform(get("/admin/surveys/bad-uid/statistics.do"))
                .andExpect(status().is3xxRedirection());

        verifyNoInteractions(statsService);
    }

    @Test
    void statisticsTemplateHasEmptyFieldStatisticsMessage() throws Exception {
        String template = Files.readString(Path.of("src/main/resources/templates/admin/survey/statistics.html"));

        assertThat(template)
                .contains("th:if=\"${#lists.isEmpty(stats.fieldStatistics)}\"")
                .contains("통계를 표시할 문항이 없습니다.");
    }
}
