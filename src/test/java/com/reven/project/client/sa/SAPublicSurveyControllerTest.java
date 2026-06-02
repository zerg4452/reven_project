package com.reven.project.client.sa;

// 사용자 설문 제출 요청을 문항별 값 목록으로 평탄화하는 컨트롤러 테스트

import com.reven.project.service.sa.SASurveyService;
import com.reven.project.service.sa.SASurveySubmitService;
import com.reven.project.service.sa.dto.SASurveyDto;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class SAPublicSurveyControllerTest {

    @Test
    void submitGroupsRepeatedCheckboxValuesIntoOneAnswerRequest() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        SASurveySubmitService submitService = mock(SASurveySubmitService.class);
        when(surveyService.findSurvey("survey-uid")).thenReturn(survey());

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAPublicSurveyController(surveyService, submitService)).build();

        mvc.perform(post("/surveys/submit.do")
                        .param("surveyUid", "survey-uid")
                        .param("submitterName", "홍길동")
                        .param("phone", "010-0000-0000")
                        .param("answers[field-10]", "red", "blue")
                        .param("answers[field-11]", "hello"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/surveys/thanks.do"));

        ArgumentCaptor<SASurveyDto.SurveySubmitRequest> captor = ArgumentCaptor.forClass(SASurveyDto.SurveySubmitRequest.class);
        verify(submitService).submit(eq("survey-uid"), captor.capture(), anyString());
        assertThat(captor.getValue().submitterName).isEqualTo("홍길동");
        assertThat(captor.getValue().phone).isEqualTo("010-0000-0000");
        assertThat(captor.getValue().answers).extracting(answer -> answer.fieldKey).containsExactly("field-10", "field-11");
        assertThat(captor.getValue().answers.get(0).values).containsExactly("red", "blue");
        assertThat(captor.getValue().answers.get(1).values).containsExactly("hello");
    }

    @Test
    void submitRedisplaysFormWithFieldErrorsWhenValidationFails() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        SASurveySubmitService submitService = mock(SASurveySubmitService.class);
        when(surveyService.findSurvey("survey-uid")).thenReturn(survey());
        when(submitService.submit(anyString(), any(), anyString()))
                .thenThrow(new SASurveySubmitService.SubmissionValidationException(
                        java.util.Map.of("field-10", "필수 문항에 응답해 주세요.")
                ));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAPublicSurveyController(surveyService, submitService)).build();

        mvc.perform(post("/surveys/submit.do")
                        .param("surveyUid", "survey-uid")
                        .param("submitterName", "홍길동")
                        .param("phone", "010-0000-0000"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/survey/form"))
                .andExpect(model().attributeExists("survey", "errors"));
    }

    private SASurveyDto.SurveyDetail survey() {
        SASurveyDto.SurveyDetail survey = new SASurveyDto.SurveyDetail();
        survey.surveySeq = 1L;
        survey.surveyUid = "survey-uid";
        survey.title = "설문";
        survey.description = "설명";
        survey.useYn = "Y";
        survey.fields = List.of();
        return survey;
    }
}
