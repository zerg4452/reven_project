package com.reven.project.service.sa;

// 설문 제출 저장 시 객관식/체크박스 응답을 정규화하는 서비스 테스트

import com.reven.project.service.sa.dto.SASurveyDto;
import com.reven.project.service.sa.mapper.SASurveySubmitMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SASurveySubmitServiceTest {

    @Test
    void submitStoresAnswersInSurveyFieldOrderWithNormalizedValues() {
        SASurveyService surveyService = mock(SASurveyService.class);
        SASurveySubmitMapper submitMapper = mock(SASurveySubmitMapper.class);
        SASurveyDto.SurveyDetail survey = survey(1L, "survey-uid");
        SASurveyDto.SurveyField selectField = field(10L, 1L, "objective", "select");
        selectField.options = List.of(option(100L, 10L, "Red", "red"), option(101L, 10L, "Blue", "blue"));
        SASurveyDto.SurveyField textField = field(11L, 1L, "subjective", "text");
        survey.fields = List.of(selectField, textField);
        when(surveyService.findSurvey("survey-uid")).thenReturn(survey);

        SASurveySubmitService service = new SASurveySubmitService(surveyService, submitMapper);
        SASurveyDto.SurveySubmitRequest request = new SASurveyDto.SurveySubmitRequest();

        SASurveyDto.AnswerRequest textAnswer = new SASurveyDto.AnswerRequest();
        textAnswer.fieldKey = "field-11";
        textAnswer.values = List.of("hello");

        SASurveyDto.AnswerRequest selectAnswer = new SASurveyDto.AnswerRequest();
        selectAnswer.fieldKey = "field-10";
        selectAnswer.values = List.of("blue");

        request.answers = List.of(textAnswer, selectAnswer);

        service.submit("survey-uid", request, "127.0.0.1");

        ArgumentCaptor<SASurveyDto.AnswerInsert> captor = ArgumentCaptor.forClass(SASurveyDto.AnswerInsert.class);
        verify(submitMapper, times(2)).insertAnswer(captor.capture());

        assertThat(captor.getAllValues()).hasSize(2);
        assertThat(captor.getAllValues().get(0).fieldKey).isEqualTo("field-10");
        assertThat(captor.getAllValues().get(0).answerValue).isEqualTo("Blue");
        assertThat(captor.getAllValues().get(0).answerJson).isNull();
        assertThat(captor.getAllValues().get(1).fieldKey).isEqualTo("field-11");
        assertThat(captor.getAllValues().get(1).answerValue).isEqualTo("hello");
        assertThat(captor.getAllValues().get(1).answerJson).isNull();
    }

    @Test
    void submitStoresCheckboxAnswerAsJoinedLabelAndJsonArray() {
        SASurveyService surveyService = mock(SASurveyService.class);
        SASurveySubmitMapper submitMapper = mock(SASurveySubmitMapper.class);
        SASurveyDto.SurveyDetail survey = survey(1L, "survey-uid");
        SASurveyDto.SurveyField checkboxField = field(10L, 1L, "objective", "checkbox");
        checkboxField.options = List.of(option(100L, 10L, "Red", "red"), option(101L, 10L, "Blue", "blue"));
        survey.fields = List.of(checkboxField);
        when(surveyService.findSurvey("survey-uid")).thenReturn(survey);

        SASurveySubmitService service = new SASurveySubmitService(surveyService, submitMapper);
        SASurveyDto.SurveySubmitRequest request = new SASurveyDto.SurveySubmitRequest();
        SASurveyDto.AnswerRequest answer = new SASurveyDto.AnswerRequest();
        answer.fieldKey = "field-10";
        answer.values = List.of("red", "blue");
        request.answers = List.of(answer);

        service.submit("survey-uid", request, "127.0.0.1");

        ArgumentCaptor<SASurveyDto.AnswerInsert> captor = ArgumentCaptor.forClass(SASurveyDto.AnswerInsert.class);
        verify(submitMapper).insertAnswer(captor.capture());

        assertThat(captor.getValue().answerValue).isEqualTo("Red, Blue");
        assertThat(captor.getValue().answerJson).isEqualTo("[\"red\",\"blue\"]");
    }

    @Test
    void submitRejectsRequiredObjectiveWhenNothingIsSelected() {
        SASurveyService surveyService = mock(SASurveyService.class);
        SASurveySubmitMapper submitMapper = mock(SASurveySubmitMapper.class);
        SASurveyDto.SurveyDetail survey = survey(1L, "survey-uid");
        SASurveyDto.SurveyField field = field(10L, 1L, "objective", "select");
        field.requiredYn = "Y";
        field.options = List.of(option(100L, 10L, "Red", "red"));
        survey.fields = List.of(field);
        when(surveyService.findSurvey("survey-uid")).thenReturn(survey);

        SASurveySubmitService service = new SASurveySubmitService(surveyService, submitMapper);
        SASurveyDto.SurveySubmitRequest request = new SASurveyDto.SurveySubmitRequest();
        SASurveyDto.AnswerRequest answer = new SASurveyDto.AnswerRequest();
        answer.fieldKey = "field-10";
        answer.values = List.of("   ");
        request.answers = List.of(answer);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.submit("survey-uid", request, "127.0.0.1"))
                .isInstanceOf(SASurveySubmitService.SubmissionValidationException.class);
        verify(submitMapper, never()).insertSubmission(any());
        verify(submitMapper, never()).insertAnswer(any());
    }

    @Test
    void submitRejectsRequiredSubjectiveWhenBlank() {
        SASurveyService surveyService = mock(SASurveyService.class);
        SASurveySubmitMapper submitMapper = mock(SASurveySubmitMapper.class);
        SASurveyDto.SurveyDetail survey = survey(1L, "survey-uid");
        SASurveyDto.SurveyField field = field(11L, 1L, "subjective", "text");
        field.requiredYn = "Y";
        survey.fields = List.of(field);
        when(surveyService.findSurvey("survey-uid")).thenReturn(survey);

        SASurveySubmitService service = new SASurveySubmitService(surveyService, submitMapper);
        SASurveyDto.SurveySubmitRequest request = new SASurveyDto.SurveySubmitRequest();
        SASurveyDto.AnswerRequest answer = new SASurveyDto.AnswerRequest();
        answer.fieldKey = "field-11";
        answer.values = List.of("   ");
        request.answers = List.of(answer);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.submit("survey-uid", request, "127.0.0.1"))
                .isInstanceOf(SASurveySubmitService.SubmissionValidationException.class);
        verify(submitMapper, never()).insertSubmission(any());
        verify(submitMapper, never()).insertAnswer(any());
    }

    @Test
    void submitRejectsRequiredCheckboxWhenNothingIsSelected() {
        SASurveyService surveyService = mock(SASurveyService.class);
        SASurveySubmitMapper submitMapper = mock(SASurveySubmitMapper.class);
        SASurveyDto.SurveyDetail survey = survey(1L, "survey-uid");
        SASurveyDto.SurveyField field = field(12L, 1L, "objective", "checkbox");
        field.requiredYn = "Y";
        field.options = List.of(option(120L, 12L, "Red", "red"));
        survey.fields = List.of(field);
        when(surveyService.findSurvey("survey-uid")).thenReturn(survey);

        SASurveySubmitService service = new SASurveySubmitService(surveyService, submitMapper);
        SASurveyDto.SurveySubmitRequest request = new SASurveyDto.SurveySubmitRequest();
        SASurveyDto.AnswerRequest answer = new SASurveyDto.AnswerRequest();
        answer.fieldKey = "field-12";
        answer.values = List.of("   ");
        request.answers = List.of(answer);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.submit("survey-uid", request, "127.0.0.1"))
                .isInstanceOf(SASurveySubmitService.SubmissionValidationException.class);
        verify(submitMapper, never()).insertSubmission(any());
        verify(submitMapper, never()).insertAnswer(any());
    }

    private SASurveyDto.SurveyDetail survey(Long surveySeq, String surveyUid) {
        SASurveyDto.SurveyDetail survey = new SASurveyDto.SurveyDetail();
        survey.surveySeq = surveySeq;
        survey.surveyUid = surveyUid;
        survey.title = "설문";
        survey.description = "설명";
        survey.useYn = "Y";
        return survey;
    }

    private SASurveyDto.SurveyField field(Long fieldSeq, Long surveySeq, String surveyType, String fieldType) {
        SASurveyDto.SurveyField field = new SASurveyDto.SurveyField();
        field.fieldSeq = fieldSeq;
        field.surveySeq = surveySeq;
        field.fieldKey = "field-" + fieldSeq;
        field.label = "질문";
        field.surveyType = surveyType;
        field.fieldType = fieldType;
        field.requiredYn = "N";
        field.sortOrd = 1;
        return field;
    }

    private SASurveyDto.SurveyOption option(Long optionSeq, Long fieldSeq, String label, String value) {
        SASurveyDto.SurveyOption option = new SASurveyDto.SurveyOption();
        option.optionSeq = optionSeq;
        option.fieldSeq = fieldSeq;
        option.optionLabel = label;
        option.optionValue = value;
        option.sortOrd = 1;
        return option;
    }
}
