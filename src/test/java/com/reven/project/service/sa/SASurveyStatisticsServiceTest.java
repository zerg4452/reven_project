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
        when(mapper.selectStatisticFields(1L)).thenReturn(List.of());
        when(mapper.selectRecentTextAnswers(1L, "memo")).thenReturn(List.of("답변"));

        SASurveyDto.SurveyDetail survey = surveyWithFields(List.of(subjectiveField(1L, "memo", "메모", "text", 1)));
        new SASurveyStatisticsService(mapper).getStatistics(survey);

        verify(mapper, times(1)).selectStatusCounts(1L);
        verify(mapper, times(1)).selectDailyCounts(1L);
        verify(mapper, times(1)).selectStatisticFields(1L);
    }

    @Test
    void getStatisticsCountsObjectiveAnswersBySnapshotFieldKeyAfterFieldSeqChanges() {
        SASurveySubmitMapper mapper = mock(SASurveySubmitMapper.class);
        when(mapper.selectStatusCounts(any())).thenReturn(List.of());
        when(mapper.selectDailyCounts(any())).thenReturn(List.of());
        when(mapper.selectStatisticFields(1L)).thenReturn(List.of());
        when(mapper.selectObjectiveOptionFrequencies(1L, "q1")).thenReturn(List.of(frequency("기존 보기", 1)));

        SASurveyDto.SurveyField objectiveField = objectiveField(200L, "q1", "현재 문항", "radio", 1, List.of("현재 보기"));

        SASurveyDto.SurveyDetail survey = surveyWithFields(List.of(objectiveField));
        SASurveyDto.SurveyStatistics stats = new SASurveyStatisticsService(mapper).getStatistics(survey);

        verify(mapper).selectObjectiveOptionFrequencies(1L, "q1");
        assertThat(stats.fieldStatistics.get(0).optionFrequencies)
                .extracting(frequency -> frequency.optionLabel + ":" + frequency.count)
                .containsExactly("현재 보기:0", "기존 보기:1");
    }

    @Test
    void getStatisticsKeepsSnapshotOnlyDeletedFields() {
        SASurveySubmitMapper mapper = mock(SASurveySubmitMapper.class);
        when(mapper.selectStatusCounts(any())).thenReturn(List.of());
        when(mapper.selectDailyCounts(any())).thenReturn(List.of());
        when(mapper.selectStatisticFields(1L)).thenReturn(List.of(snapshotField("old-q", "삭제된 문항", "subjective", "text", 3)));
        when(mapper.selectRecentTextAnswers(1L, "old-q")).thenReturn(List.of("남아있는 답변"));

        SASurveyDto.SurveyDetail survey = surveyWithFields(List.of());
        SASurveyDto.SurveyStatistics stats = new SASurveyStatisticsService(mapper).getStatistics(survey);

        assertThat(stats.fieldStatistics).hasSize(1);
        assertThat(stats.fieldStatistics.get(0).fieldKey).isEqualTo("old-q");
        assertThat(stats.fieldStatistics.get(0).recentTextAnswers).containsExactly("남아있는 답변");
    }

    @Test
    void getStatisticsMapsCheckboxJsonValueToCurrentLabelAndKeepsCurrentZeroOptions() {
        SASurveySubmitMapper mapper = mock(SASurveySubmitMapper.class);
        when(mapper.selectStatusCounts(any())).thenReturn(List.of());
        when(mapper.selectDailyCounts(any())).thenReturn(List.of());
        when(mapper.selectStatisticFields(1L)).thenReturn(List.of());
        when(mapper.selectObjectiveOptionFrequencies(1L, "checks")).thenReturn(List.of(frequency("agree", 1), frequency("C", 1)));

        SASurveyDto.SurveyField checkboxField = objectiveField(10L, "checks", "체크 문항", "checkbox", 1, List.of(option("예, 동의", "agree"), option("B", "B")));

        SASurveyDto.SurveyDetail survey = surveyWithFields(List.of(checkboxField));
        SASurveyDto.SurveyStatistics stats = new SASurveyStatisticsService(mapper).getStatistics(survey);

        assertThat(stats.fieldStatistics.get(0).optionFrequencies)
                .extracting(frequency -> frequency.optionLabel + ":" + frequency.count)
                .containsExactly("예, 동의:1", "B:0", "C:1");
    }

    @Test
    void getStatisticsLimitsSubjectiveRecentAnswersToTwenty() {
        SASurveySubmitMapper mapper = mock(SASurveySubmitMapper.class);
        when(mapper.selectStatusCounts(any())).thenReturn(List.of());
        when(mapper.selectDailyCounts(any())).thenReturn(List.of());
        when(mapper.selectStatisticFields(1L)).thenReturn(List.of());
        when(mapper.selectRecentTextAnswers(1L, "memo")).thenReturn(java.util.stream.IntStream.rangeClosed(1, 20)
                .mapToObj(index -> "답변" + index)
                .toList());

        SASurveyDto.SurveyField memoField = subjectiveField(20L, "memo", "메모", "text", 1);
        SASurveyDto.SurveyDetail survey = surveyWithFields(List.of(memoField));
        SASurveyDto.SurveyStatistics stats = new SASurveyStatisticsService(mapper).getStatistics(survey);

        assertThat(stats.fieldStatistics.get(0).recentTextAnswers)
                .hasSize(20)
                .first()
                .isEqualTo("답변1");
    }

    private SASurveyDto.SurveyDetail surveyWithFields(List<SASurveyDto.SurveyField> fields) {
        SASurveyDto.SurveyDetail survey = new SASurveyDto.SurveyDetail();
        survey.surveySeq = 1L;
        survey.surveyUid = "uid-1";
        survey.fields = fields;
        return survey;
    }

    private SASurveyDto.SurveyField objectiveField(Long fieldSeq, String fieldKey, String label, String fieldType, int sortOrd, List<?> options) {
        SASurveyDto.SurveyField field = new SASurveyDto.SurveyField();
        field.fieldSeq = fieldSeq;
        field.fieldKey = fieldKey;
        field.label = label;
        field.surveyType = "objective";
        field.fieldType = fieldType;
        field.sortOrd = sortOrd;
        field.options = options.stream()
                .map(option -> option instanceof SASurveyDto.SurveyOption surveyOption ? surveyOption : option(option.toString()))
                .toList();
        return field;
    }

    private SASurveyDto.SurveyField subjectiveField(Long fieldSeq, String fieldKey, String label, String fieldType, int sortOrd) {
        SASurveyDto.SurveyField field = new SASurveyDto.SurveyField();
        field.fieldSeq = fieldSeq;
        field.fieldKey = fieldKey;
        field.label = label;
        field.surveyType = "subjective";
        field.fieldType = fieldType;
        field.sortOrd = sortOrd;
        return field;
    }

    private SASurveyDto.SurveyOption option(String label) {
        return option(label, label);
    }

    private SASurveyDto.SurveyOption option(String label, String value) {
        SASurveyDto.SurveyOption option = new SASurveyDto.SurveyOption();
        option.optionLabel = label;
        option.optionValue = value;
        return option;
    }

    private SASurveyDto.FieldStatistics snapshotField(String fieldKey, String label, String surveyType, String fieldType, int sortOrd) {
        SASurveyDto.FieldStatistics field = new SASurveyDto.FieldStatistics();
        field.fieldKey = fieldKey;
        field.fieldLabel = label;
        field.surveyType = surveyType;
        field.fieldType = fieldType;
        field.sortOrd = sortOrd;
        return field;
    }

    private SASurveyDto.OptionFrequency frequency(String optionLabel, long count) {
        SASurveyDto.OptionFrequency frequency = new SASurveyDto.OptionFrequency();
        frequency.optionLabel = optionLabel;
        frequency.count = count;
        return frequency;
    }
}
