package com.reven.project.service.sa;

// 설문 문항 저장 흐름을 검증하는 서비스 테스트

import com.reven.project.service.sa.dto.SASurveyDto;
import com.reven.project.service.sa.mapper.SASurveyMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SASurveyServiceTest {

    @Test
    void newSurveyFormUsesNewRecordState() {
        SASurveyService service = new SASurveyService(mock(SASurveyMapper.class));

        SASurveyDto.SurveyDetail survey = service.newSurveyForm();

        assertThat(survey.surveyUid).isNotBlank();
        assertThat(survey.surveySeq).isNull();
        assertThat(survey.fields).isEmpty();
        assertThat(survey.useYn).isEqualTo("Y");
    }

    @Test
    void saveSurveyInsertsNewSurveyWithNormalizedFieldType() {
        SASurveyMapper mapper = mock(SASurveyMapper.class);
        SASurveyDto.SurveyDetail persisted = survey(1L, "survey-uid");
        SASurveyDto.SurveyField persistedField = field(10L, 1L, "objective", "select");
        when(mapper.selectSurvey("survey-uid")).thenReturn(null, persisted);
        when(mapper.selectSurveyFields(1L)).thenReturn(List.of(persistedField));
        when(mapper.selectSurveyOptions(1L)).thenReturn(List.of());

        SASurveyService service = new SASurveyService(mapper);
        SASurveyDto.SurveySaveRequest request = new SASurveyDto.SurveySaveRequest();
        request.surveyUid = "survey-uid";
        request.title = "새 설문";
        request.description = "설명";
        request.useYn = "true";

        SASurveyDto.SurveyFieldSaveRequest field = new SASurveyDto.SurveyFieldSaveRequest();
        field.label = "질문 1";
        field.surveyType = "주관식";
        field.fieldType = "textarea";
        request.fields = List.of(field);

        SASurveyDto.SurveyDetail result = service.saveSurvey(null, request);

        ArgumentCaptor<SASurveyDto.SurveyField> fieldCaptor = ArgumentCaptor.forClass(SASurveyDto.SurveyField.class);
        verify(mapper).insertSurveyField(fieldCaptor.capture());

        assertThat(fieldCaptor.getValue().surveyType).isEqualTo("subjective");
        assertThat(fieldCaptor.getValue().fieldType).isEqualTo("textarea");
        assertThat(result.surveySeq).isEqualTo(1L);
        assertThat(result.fields).hasSize(1);
        assertThat(result.fields.get(0).surveyType).isEqualTo("objective");
    }

    @Test
    void saveSurveyUpdatesExistingSurveyWithNormalizedFieldType() {
        SASurveyMapper mapper = mock(SASurveyMapper.class);
        SASurveyDto.SurveyDetail existing = survey(7L, "survey-uid");
        SASurveyDto.SurveyDetail updated = survey(7L, "survey-uid");
        SASurveyDto.SurveyField updatedField = field(20L, 7L, "subjective", "textarea");
        when(mapper.selectSurvey("survey-uid")).thenReturn(existing, updated);
        when(mapper.selectSurveyFields(7L)).thenReturn(List.of(updatedField));
        when(mapper.selectSurveyOptions(7L)).thenReturn(List.of());

        SASurveyService service = new SASurveyService(mapper);
        SASurveyDto.SurveySaveRequest request = new SASurveyDto.SurveySaveRequest();
        request.surveyUid = "ignored";
        request.title = "수정 설문";
        request.description = "설명";
        request.useYn = "false";

        SASurveyDto.SurveyFieldSaveRequest field = new SASurveyDto.SurveyFieldSaveRequest();
        field.label = "질문 1";
        field.surveyType = "objective";
        field.fieldType = "select";
        request.fields = List.of(field);

        SASurveyDto.SurveyDetail result = service.saveSurvey("survey-uid", request);

        ArgumentCaptor<SASurveyDto.SurveyDetail> surveyCaptor = ArgumentCaptor.forClass(SASurveyDto.SurveyDetail.class);
        ArgumentCaptor<SASurveyDto.SurveyField> fieldCaptor = ArgumentCaptor.forClass(SASurveyDto.SurveyField.class);
        verify(mapper).updateSurvey(surveyCaptor.capture());
        verify(mapper).deleteSurveyOptions(7L);
        verify(mapper).deleteSurveyFields(7L);
        verify(mapper).insertSurveyField(fieldCaptor.capture());

        assertThat(surveyCaptor.getValue().surveySeq).isEqualTo(7L);
        assertThat(surveyCaptor.getValue().surveyUid).isEqualTo("survey-uid");
        assertThat(surveyCaptor.getValue().useYn).isEqualTo("N");
        assertThat(fieldCaptor.getValue().surveyType).isEqualTo("objective");
        assertThat(fieldCaptor.getValue().fieldType).isEqualTo("select");
        assertThat(result.surveySeq).isEqualTo(7L);
        assertThat(result.fields).hasSize(1);
        assertThat(result.fields.get(0).surveyType).isEqualTo("subjective");
    }

    private SASurveyDto.SurveyDetail survey(Long surveySeq, String surveyUid) {
        SASurveyDto.SurveyDetail survey = new SASurveyDto.SurveyDetail();
        survey.surveySeq = surveySeq;
        survey.surveyUid = surveyUid;
        survey.title = "설문";
        survey.description = "설명";
        survey.useYn = "Y";
        survey.regDate = LocalDate.of(2026, 6, 1);
        survey.modDate = LocalDate.of(2026, 6, 1);
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
}
