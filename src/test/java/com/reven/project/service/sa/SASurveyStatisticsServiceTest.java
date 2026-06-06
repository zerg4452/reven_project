package com.reven.project.service.sa;

import com.reven.project.service.sa.dto.SASurveyDto;
import com.reven.project.service.sa.mapper.SASurveySubmitMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SASurveyStatisticsServiceTest {

    @Test
    void getStatisticsCallsStatusAndDailyCountsOnce() {
        SASurveySubmitMapper mapper = mock(SASurveySubmitMapper.class);
        when(mapper.selectStatusCounts(1L)).thenReturn(List.of());
        when(mapper.selectDailyCounts(1L)).thenReturn(List.of());

        SASurveyDto.SurveyDetail survey = surveyWithFields(List.of());
        new SASurveyStatisticsService(mapper).getStatistics(survey);

        verify(mapper, times(1)).selectStatusCounts(1L);
        verify(mapper, times(1)).selectDailyCounts(1L);
    }

    @Test
    void getStatisticsCallsOptionFrequenciesForObjectiveField() {
        SASurveySubmitMapper mapper = mock(SASurveySubmitMapper.class);
        when(mapper.selectStatusCounts(any())).thenReturn(List.of());
        when(mapper.selectDailyCounts(any())).thenReturn(List.of());
        when(mapper.selectOptionFrequencies(1L, 10L)).thenReturn(List.of());

        SASurveyDto.SurveyField objectiveField = new SASurveyDto.SurveyField();
        objectiveField.fieldSeq = 10L;
        objectiveField.surveyType = "objective";

        SASurveyDto.SurveyDetail survey = surveyWithFields(List.of(objectiveField));
        SASurveyDto.SurveyStatistics stats = new SASurveyStatisticsService(mapper).getStatistics(survey);

        verify(mapper).selectOptionFrequencies(1L, 10L);
        verify(mapper, never()).selectRecentTextAnswers(any(), any());
        assertThat(stats.fieldStatistics).hasSize(1);
        assertThat(stats.fieldStatistics.get(0).surveyType).isEqualTo("objective");
    }

    @Test
    void getStatisticsCallsRecentTextAnswersForSubjectiveField() {
        SASurveySubmitMapper mapper = mock(SASurveySubmitMapper.class);
        when(mapper.selectStatusCounts(any())).thenReturn(List.of());
        when(mapper.selectDailyCounts(any())).thenReturn(List.of());
        when(mapper.selectRecentTextAnswers(1L, 20L)).thenReturn(List.of("답변1", "답변2"));

        SASurveyDto.SurveyField subjectiveField = new SASurveyDto.SurveyField();
        subjectiveField.fieldSeq = 20L;
        subjectiveField.surveyType = "subjective";

        SASurveyDto.SurveyDetail survey = surveyWithFields(List.of(subjectiveField));
        SASurveyDto.SurveyStatistics stats = new SASurveyStatisticsService(mapper).getStatistics(survey);

        verify(mapper).selectRecentTextAnswers(1L, 20L);
        verify(mapper, never()).selectOptionFrequencies(any(), any());
        assertThat(stats.fieldStatistics.get(0).recentTextAnswers).containsExactly("답변1", "답변2");
    }

    @Test
    void getStatisticsHandlesMixedFields() {
        SASurveySubmitMapper mapper = mock(SASurveySubmitMapper.class);
        when(mapper.selectStatusCounts(any())).thenReturn(List.of());
        when(mapper.selectDailyCounts(any())).thenReturn(List.of());
        when(mapper.selectOptionFrequencies(any(), any())).thenReturn(List.of());
        when(mapper.selectRecentTextAnswers(any(), any())).thenReturn(List.of());

        SASurveyDto.SurveyField obj = new SASurveyDto.SurveyField();
        obj.fieldSeq = 1L;
        obj.surveyType = "objective";

        SASurveyDto.SurveyField subj = new SASurveyDto.SurveyField();
        subj.fieldSeq = 2L;
        subj.surveyType = "subjective";

        SASurveyDto.SurveyDetail survey = surveyWithFields(List.of(obj, subj));
        SASurveyDto.SurveyStatistics stats = new SASurveyStatisticsService(mapper).getStatistics(survey);

        assertThat(stats.fieldStatistics).hasSize(2);
        verify(mapper, times(1)).selectOptionFrequencies(any(), any());
        verify(mapper, times(1)).selectRecentTextAnswers(any(), any());
    }

    private SASurveyDto.SurveyDetail surveyWithFields(List<SASurveyDto.SurveyField> fields) {
        SASurveyDto.SurveyDetail survey = new SASurveyDto.SurveyDetail();
        survey.surveySeq = 1L;
        survey.surveyUid = "uid-1";
        survey.fields = fields;
        return survey;
    }
}
