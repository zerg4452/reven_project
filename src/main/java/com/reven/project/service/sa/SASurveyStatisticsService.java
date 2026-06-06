package com.reven.project.service.sa;

import com.reven.project.service.sa.dto.SASurveyDto;
import com.reven.project.service.sa.mapper.SASurveySubmitMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SASurveyStatisticsService {

    private final SASurveySubmitMapper submitMapper;

    public SASurveyStatisticsService(SASurveySubmitMapper submitMapper) {
        this.submitMapper = submitMapper;
    }

    public SASurveyDto.SurveyStatistics getStatistics(SASurveyDto.SurveyDetail survey) {
        SASurveyDto.SurveyStatistics stats = new SASurveyDto.SurveyStatistics();
        Long surveySeq = survey.surveySeq;

        stats.statusCounts = submitMapper.selectStatusCounts(surveySeq);
        stats.dailyCounts = submitMapper.selectDailyCounts(surveySeq);
        Map<String, SASurveyDto.FieldStatistics> fieldStatsByKey = new LinkedHashMap<>();
        Map<String, Map<String, String>> optionLabelByValueByFieldKey = new LinkedHashMap<>();

        for (SASurveyDto.SurveyField field : survey.fields) {
            fieldStatsByKey.put(field.fieldKey, fromCurrentField(field));
            optionLabelByValueByFieldKey.put(field.fieldKey, optionLabelByValue(field));
        }

        for (SASurveyDto.FieldStatistics snapshotField : submitMapper.selectStatisticFields(surveySeq)) {
            fieldStatsByKey.putIfAbsent(snapshotField.fieldKey, snapshotField);
        }

        for (SASurveyDto.FieldStatistics fieldStats : fieldStatsByKey.values()) {
            if ("objective".equalsIgnoreCase(fieldStats.surveyType)) {
                fieldStats.optionFrequencies = buildOptionFrequencies(
                        fieldStats,
                        submitMapper.selectObjectiveOptionFrequencies(surveySeq, fieldStats.fieldKey),
                        optionLabelByValueByFieldKey.getOrDefault(fieldStats.fieldKey, Map.of())
                );
            } else {
                fieldStats.recentTextAnswers = submitMapper.selectRecentTextAnswers(surveySeq, fieldStats.fieldKey);
            }
            stats.fieldStatistics.add(fieldStats);
        }
        stats.fieldStatistics.sort(Comparator.comparingInt(field -> field.sortOrd));
        return stats;
    }

    private SASurveyDto.FieldStatistics fromCurrentField(SASurveyDto.SurveyField field) {
        SASurveyDto.FieldStatistics fieldStats = new SASurveyDto.FieldStatistics();
        fieldStats.fieldSeq = field.fieldSeq;
        fieldStats.fieldKey = field.fieldKey;
        fieldStats.fieldLabel = field.label;
        fieldStats.surveyType = defaultSurveyType(field.surveyType, field.fieldType);
        fieldStats.fieldType = field.fieldType;
        fieldStats.sortOrd = field.sortOrd;
        for (SASurveyDto.SurveyOption option : field.options) {
            SASurveyDto.OptionFrequency frequency = new SASurveyDto.OptionFrequency();
            frequency.optionLabel = option.optionLabel;
            frequency.count = 0;
            fieldStats.optionFrequencies.add(frequency);
        }
        return fieldStats;
    }

    private Map<String, String> optionLabelByValue(SASurveyDto.SurveyField field) {
        Map<String, String> labelByValue = new LinkedHashMap<>();
        for (SASurveyDto.SurveyOption option : field.options) {
            labelByValue.put(option.optionValue, option.optionLabel);
        }
        return labelByValue;
    }

    private List<SASurveyDto.OptionFrequency> buildOptionFrequencies(SASurveyDto.FieldStatistics fieldStats, List<SASurveyDto.OptionFrequency> submittedFrequencies, Map<String, String> optionLabelByValue) {
        Map<String, Long> countsByLabel = new LinkedHashMap<>();
        for (SASurveyDto.OptionFrequency currentOption : fieldStats.optionFrequencies) {
            countsByLabel.putIfAbsent(currentOption.optionLabel, 0L);
        }
        for (SASurveyDto.OptionFrequency submittedFrequency : submittedFrequencies) {
            String label = optionLabelByValue.getOrDefault(submittedFrequency.optionLabel, submittedFrequency.optionLabel);
            countsByLabel.merge(label, submittedFrequency.count, Long::sum);
        }

        List<SASurveyDto.OptionFrequency> frequencies = new ArrayList<>();
        for (Map.Entry<String, Long> entry : countsByLabel.entrySet()) {
            SASurveyDto.OptionFrequency frequency = new SASurveyDto.OptionFrequency();
            frequency.optionLabel = entry.getKey();
            frequency.count = entry.getValue();
            frequencies.add(frequency);
        }
        return frequencies;
    }

    private String defaultSurveyType(String surveyType, String fieldType) {
        if (surveyType != null && !surveyType.isBlank()) {
            return surveyType;
        }
        String normalizedFieldType = fieldType == null ? "" : fieldType.toLowerCase();
        return switch (normalizedFieldType) {
            case "select", "radio", "checkbox" -> "objective";
            default -> "subjective";
        };
    }
}
