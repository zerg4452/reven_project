package com.reven.project.service.sa;

import com.reven.project.service.sa.dto.SASurveyDto;
import com.reven.project.service.sa.mapper.SASurveySubmitMapper;
import org.springframework.stereotype.Service;

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

        for (SASurveyDto.SurveyField field : survey.fields) {
            SASurveyDto.FieldStatistics fieldStats = new SASurveyDto.FieldStatistics();
            fieldStats.fieldSeq = field.fieldSeq;
            fieldStats.fieldKey = field.fieldKey;
            fieldStats.fieldLabel = field.label;
            fieldStats.surveyType = field.surveyType;
            fieldStats.fieldType = field.fieldType;

            if ("objective".equalsIgnoreCase(field.surveyType)) {
                fieldStats.optionFrequencies = submitMapper.selectOptionFrequencies(surveySeq, field.fieldSeq);
            } else {
                fieldStats.recentTextAnswers = submitMapper.selectRecentTextAnswers(surveySeq, field.fieldSeq);
            }
            stats.fieldStatistics.add(fieldStats);
        }
        return stats;
    }
}
