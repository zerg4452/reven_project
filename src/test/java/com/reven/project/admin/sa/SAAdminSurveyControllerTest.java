package com.reven.project.admin.sa;

// 관리자 설문 저장 시 옵션 검증과 오류 재표시를 검증하는 테스트

import com.reven.project.service.sa.SASurveyService;
import com.reven.project.service.sa.dto.SASurveyDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class SAAdminSurveyControllerTest {

    @Test
    void previewSurveyRendersPublicFormInPreviewMode() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        when(surveyService.findSurvey("survey-uid")).thenReturn(survey());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(get("/admin/surveys/preview.do").param("surveyUid", "survey-uid"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/survey/form"))
                .andExpect(model().attributeExists("survey"))
                .andExpect(model().attribute("previewMode", true));

        verify(surveyService).findSurvey("survey-uid");
    }

    @Test
    void previewSurveyRedirectsToListWhenSurveyUidIsInvalid() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        when(surveyService.findSurvey("missing-uid")).thenThrow(new IllegalArgumentException("missing"));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(get("/admin/surveys/preview.do").param("surveyUid", "missing-uid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/surveys/list.do"))
                .andExpect(flash().attribute("surveySavedMessage", "비정상적인 접근입니다."));

        verify(surveyService).findSurvey("missing-uid");
    }

    @Test
    void saveSurveyRejectsObjectiveFieldWithoutOptions() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        when(surveyService.newSurveyForm()).thenReturn(survey());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(post("/admin/surveys/insert.do")
                        .param("surveyUid", "survey-uid")
                        .param("title", "설문")
                        .param("useYn", "Y")
                        .param("fields[0].label", "질문 1")
                        .param("fields[0].surveyType", "objective")
                        .param("fields[0].fieldType", "select")
                        .param("fields[0].options[0].optionLabel", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/survey/detail"))
                .andExpect(model().attributeExists("survey", "errors"));

        verify(surveyService, never()).saveSurvey(any(), any());
    }

    @Test
    void saveSurveyRedirectsToListWhenObjectiveFieldHasBlankOptionRow() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        SASurveyDto.SurveyDetail saved = survey();
        saved.surveySeq = 1L;
        when(surveyService.saveSurvey(isNull(), any())).thenReturn(saved);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(post("/admin/surveys/insert.do")
                        .param("surveyUid", "survey-uid")
                        .param("title", "설문")
                        .param("useYn", "Y")
                        .param("fields[0].label", "질문 1")
                        .param("fields[0].surveyType", "objective")
                        .param("fields[0].fieldType", "radio")
                        .param("fields[0].options[0].optionLabel", "보기1")
                        .param("fields[0].options[1].optionLabel", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/surveys/list.do"));

        verify(surveyService).saveSurvey(isNull(), any());
    }

    @Test
    void saveSurveyPreservesPostedFieldOrder() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        SASurveyDto.SurveyDetail saved = survey();
        saved.surveyUid = "saved-uid";
        when(surveyService.saveSurvey(isNull(), any())).thenReturn(saved);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(post("/admin/surveys/insert.do")
                        .param("surveyUid", "survey-uid")
                        .param("title", "순서 테스트")
                        .param("useYn", "Y")
                        .param("fields[0].label", "두 번째")
                        .param("fields[0].surveyType", "subjective")
                        .param("fields[0].fieldType", "text")
                        .param("fields[1].label", "첫 번째")
                        .param("fields[1].surveyType", "subjective")
                        .param("fields[1].fieldType", "text"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/surveys/list.do"));

        ArgumentCaptor<SASurveyDto.SurveySaveRequest> captor =
                ArgumentCaptor.forClass(SASurveyDto.SurveySaveRequest.class);
        verify(surveyService).saveSurvey(isNull(), captor.capture());
        assertThat(captor.getValue().fields)
                .extracting(field -> field.label)
                .containsExactly("두 번째", "첫 번째");
    }

    @Test
    void updateSurveyRedirectsToList() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        SASurveyDto.SurveyDetail saved = survey();
        saved.surveySeq = 1L;
        when(surveyService.saveSurvey(eq("survey-uid"), any())).thenReturn(saved);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(post("/admin/surveys/update.do")
                        .param("surveyUid", "survey-uid")
                        .param("title", "수정 설문")
                        .param("useYn", "Y")
                        .param("fields[0].label", "질문 1")
                        .param("fields[0].surveyType", "objective")
                        .param("fields[0].fieldType", "radio")
                        .param("fields[0].options[0].optionLabel", "A")
                        .param("fields[0].options[1].optionLabel", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/surveys/list.do"));

        verify(surveyService).saveSurvey(eq("survey-uid"), any());
    }

    @Test
    void deleteSurveyRedirectsToList() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(post("/admin/surveys/delete.do").param("surveyUid", "survey-uid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/surveys/list.do"));

        verify(surveyService).deleteSurvey("survey-uid");
    }

    @Test
    void listKeepsDefaultsOnFirstEntry() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        when(surveyService.findAdminSurveys(any())).thenReturn(java.util.List.of());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(get("/admin/surveys/list.do"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/survey/list"))
                .andExpect(model().attribute("keywordType", "전체"))
                .andExpect(model().attribute("useYn", ""));
    }

    @Test
    void listPassesUseYnYToService() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        when(surveyService.findAdminSurveys(any())).thenReturn(java.util.List.of());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(get("/admin/surveys/list.do").param("useYn", "Y"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("useYn", "Y"));

        ArgumentCaptor<SASurveyDto.SurveySearchRequest> captor =
                ArgumentCaptor.forClass(SASurveyDto.SurveySearchRequest.class);
        verify(surveyService).findAdminSurveys(captor.capture());
        assertThat(captor.getValue().useYn).isEqualTo("Y");
    }

    @Test
    void listClearsInvalidUseYnAndKeywordType() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        when(surveyService.findAdminSurveys(any())).thenReturn(java.util.List.of());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(get("/admin/surveys/list.do")
                        .param("useYn", "X")
                        .param("keywordType", "이상한값"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("useYn", ""))
                .andExpect(model().attribute("keywordType", "전체"));

        ArgumentCaptor<SASurveyDto.SurveySearchRequest> captor =
                ArgumentCaptor.forClass(SASurveyDto.SurveySearchRequest.class);
        verify(surveyService).findAdminSurveys(captor.capture());
        assertThat(captor.getValue().useYn).isNull();
        assertThat(captor.getValue().keywordType).isEqualTo("전체");
    }

    @Test
    void listFallsBackToDefaultDatesOnInvalidDate() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        when(surveyService.findAdminSurveys(any())).thenReturn(java.util.List.of());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(get("/admin/surveys/list.do").param("startDate", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/survey/list"));

        ArgumentCaptor<SASurveyDto.SurveySearchRequest> captor =
                ArgumentCaptor.forClass(SASurveyDto.SurveySearchRequest.class);
        verify(surveyService).findAdminSurveys(captor.capture());
        assertThat(captor.getValue().startDate).isNotNull();
    }

    @Test
    void saveSurveyRejectsDuplicateObjectiveOptionLabels() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        when(surveyService.newSurveyForm()).thenReturn(survey());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(post("/admin/surveys/insert.do")
                        .param("surveyUid", "survey-uid")
                        .param("title", "설문")
                        .param("useYn", "Y")
                        .param("fields[0].label", "질문 1")
                        .param("fields[0].surveyType", "objective")
                        .param("fields[0].fieldType", "select")
                        .param("fields[0].options[0].optionLabel", "Red")
                        .param("fields[0].options[1].optionLabel", "Red"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/survey/detail"))
                .andExpect(model().attributeExists("survey", "errors"));

        verify(surveyService, never()).saveSurvey(any(), any());
    }

    private SASurveyDto.SurveyDetail survey() {
        SASurveyDto.SurveyDetail survey = new SASurveyDto.SurveyDetail();
        survey.surveyUid = "survey-uid";
        survey.title = "설문";
        survey.description = "설명";
        survey.useYn = "Y";
        return survey;
    }
}
