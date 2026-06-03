package com.reven.project.admin.sa;

// 설문 이력 상태/메모 저장의 검증과 오류 재표시를 검증하는 테스트

import com.reven.project.service.sa.SASurveyCsvService;
import com.reven.project.service.sa.SASurveySubmitService;
import com.reven.project.service.sa.dto.SASurveyDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class SAAdminSurveySubmissionControllerTest {

    private MockMvc mvc(SASurveySubmitService submitService) {
        SASurveyCsvService csvService = mock(SASurveyCsvService.class);
        return MockMvcBuilders.standaloneSetup(new SAAdminSurveySubmissionController(submitService, csvService)).build();
    }

    @Test
    void listAppliesAllStatusesByDefault() throws Exception {
        SASurveySubmitService submitService = mock(SASurveySubmitService.class);
        when(submitService.findSubmissions(any())).thenReturn(java.util.List.of());

        mvc(submitService).perform(get("/admin/survey-submissions/list.do"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/survey/history-list"));

        ArgumentCaptor<SASurveyDto.SubmissionSearchRequest> captor =
                ArgumentCaptor.forClass(SASurveyDto.SubmissionSearchRequest.class);
        verify(submitService).findSubmissions(captor.capture());
        assertThat(captor.getValue().statuses)
                .containsExactly("new", "reviewing", "contacted", "done", "hold");
    }

    @Test
    void listKeepsSelectedStatusesOnly() throws Exception {
        SASurveySubmitService submitService = mock(SASurveySubmitService.class);
        when(submitService.findSubmissions(any())).thenReturn(java.util.List.of());

        mvc(submitService).perform(get("/admin/survey-submissions/list.do")
                        .param("statuses", "done")
                        .param("statuses", "hold"))
                .andExpect(status().isOk());

        ArgumentCaptor<SASurveyDto.SubmissionSearchRequest> captor =
                ArgumentCaptor.forClass(SASurveyDto.SubmissionSearchRequest.class);
        verify(submitService).findSubmissions(captor.capture());
        assertThat(captor.getValue().statuses).containsExactly("done", "hold");
    }

    @Test
    void listClearsInvalidKeywordTypeAndInvalidDate() throws Exception {
        SASurveySubmitService submitService = mock(SASurveySubmitService.class);
        when(submitService.findSubmissions(any())).thenReturn(java.util.List.of());

        mvc(submitService).perform(get("/admin/survey-submissions/list.do")
                        .param("keywordType", "이상한값")
                        .param("startDate", "abc"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("keywordType", "전체"));

        ArgumentCaptor<SASurveyDto.SubmissionSearchRequest> captor =
                ArgumentCaptor.forClass(SASurveyDto.SubmissionSearchRequest.class);
        verify(submitService).findSubmissions(captor.capture());
        assertThat(captor.getValue().keywordType).isEqualTo("전체");
        assertThat(captor.getValue().startDate).isNotNull();
    }

    @Test
    void updateSavesValidStatusAndMemo() throws Exception {
        SASurveySubmitService submitService = mock(SASurveySubmitService.class);

        mvc(submitService).perform(post("/admin/survey-submissions/update.do")
                        .param("submitUid", "submit-uid")
                        .param("status", "done")
                        .param("adminMemo", "처리 완료"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/survey-submissions/detail.do?submitUid=submit-uid"));

        verify(submitService).updateSubmission(eq("submit-uid"), any());
    }

    @Test
    void updateRejectsBlankStatus() throws Exception {
        SASurveySubmitService submitService = mock(SASurveySubmitService.class);
        when(submitService.findSubmission("submit-uid")).thenReturn(submission());

        mvc(submitService).perform(post("/admin/survey-submissions/update.do")
                        .param("submitUid", "submit-uid")
                        .param("status", "")
                        .param("adminMemo", "메모"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/survey/history-detail"))
                .andExpect(model().attributeExists("submission", "updateErrors"));

        verify(submitService, never()).updateSubmission(any(), any());
    }

    @Test
    void updateRejectsMemoOverMaxLength() throws Exception {
        SASurveySubmitService submitService = mock(SASurveySubmitService.class);
        when(submitService.findSubmission("submit-uid")).thenReturn(submission());

        mvc(submitService).perform(post("/admin/survey-submissions/update.do")
                        .param("submitUid", "submit-uid")
                        .param("status", "done")
                        .param("adminMemo", "a".repeat(2001)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/survey/history-detail"))
                .andExpect(model().attributeExists("submission", "updateErrors"));

        verify(submitService, never()).updateSubmission(any(), any());
    }

    @Test
    void updateRejectsUnknownStatusGracefully() throws Exception {
        SASurveySubmitService submitService = mock(SASurveySubmitService.class);
        when(submitService.findSubmission("submit-uid")).thenReturn(submission());

        mvc(submitService).perform(post("/admin/survey-submissions/update.do")
                        .param("submitUid", "submit-uid")
                        .param("status", "garbage")
                        .param("adminMemo", "메모"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/survey/history-detail"))
                .andExpect(model().attributeExists("submission", "updateErrors"));

        verify(submitService, never()).updateSubmission(any(), any());
    }

    private SASurveyDto.SubmissionDetail submission() {
        SASurveyDto.SubmissionDetail detail = new SASurveyDto.SubmissionDetail();
        detail.submitUid = "submit-uid";
        detail.surveyTitle = "설문";
        detail.submitterName = "홍길동";
        detail.status = "new";
        return detail;
    }
}
