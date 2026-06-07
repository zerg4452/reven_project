package com.reven.project.service.sa;

// 설문 마스터 MyBatis 공개 조회 쿼리 통합 테스트

import com.reven.project.service.sa.dto.SASurveyDto;
import com.reven.project.service.sa.mapper.SASurveyMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SASurveyMapperIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SASurveyMapper surveyMapper;

    @Test
    void publicSurveyCardsIncludePeriodStatusesAndOrderAcceptingFirst() {
        LocalDate today = LocalDate.of(2026, 6, 7);
        insertSurvey("p10-open", "접수중 설문", "Y", today.minusDays(1), today.plusDays(1), LocalDateTime.of(2026, 6, 1, 10, 0));
        insertSurvey("p10-unrestricted", "상시 설문", "Y", null, null, LocalDateTime.of(2026, 5, 31, 10, 0));
        insertSurvey("p10-future", "예정 설문", "Y", today.plusDays(1), today.plusDays(5), LocalDateTime.of(2026, 6, 2, 10, 0));
        insertSurvey("p10-closed", "마감 설문", "Y", today.minusDays(5), today.minusDays(1), LocalDateTime.of(2026, 6, 3, 10, 0));
        insertSurvey("p10-disabled", "미사용 설문", "N", null, null, LocalDateTime.of(2026, 6, 4, 10, 0));

        List<SASurveyDto.SurveyListItem> surveys = surveyMapper.selectPublicSurveyCardList(today);

        assertThat(surveys)
                .extracting(survey -> survey.surveyUid)
                .containsSubsequence("p10-open", "p10-unrestricted", "p10-future", "p10-disabled", "p10-closed");
        assertThat(surveys)
                .filteredOn(survey -> survey.surveyUid.startsWith("p10-"))
                .extracting(survey -> periodStatusText(survey, today))
                .containsExactly("접수중", "접수중", "예정", "마감", "마감");
    }

    @Test
    void publicSurveySummaryReturnsOnlyAcceptingSurveysForGivenToday() {
        LocalDate today = LocalDate.of(2026, 6, 7);
        insertSurvey("p10-summary-open", "접수중 요약", "Y", today, today, LocalDateTime.of(2026, 6, 7, 10, 0));
        insertSurvey("p10-summary-unrestricted", "상시 요약", "Y", null, null, LocalDateTime.of(2026, 6, 7, 13, 0));
        insertSurvey("p10-summary-future", "예정 요약", "Y", today.plusDays(1), null, LocalDateTime.of(2026, 6, 7, 11, 0));
        insertSurvey("p10-summary-closed", "마감 요약", "Y", null, today.minusDays(1), LocalDateTime.of(2026, 6, 7, 12, 0));

        List<SASurveyDto.SurveyListItem> surveys = surveyMapper.selectPublicSurveySummaryList(10, today);

        assertThat(surveys)
                .extracting(survey -> survey.surveyUid)
                .contains("p10-summary-open", "p10-summary-unrestricted")
                .doesNotContain("p10-summary-future", "p10-summary-closed");
    }

    private String periodStatusText(SASurveyDto.SurveyListItem survey, LocalDate today) {
        if (!survey.isEnabled()) {
            return "마감";
        }
        return SASurveyDto.periodStatusText(survey.startDate, survey.endDate, today);
    }

    private void insertSurvey(String surveyUid, String title, String useYn, LocalDate startDate, LocalDate endDate, LocalDateTime regDtm) {
        jdbcTemplate.update("""
                insert into sa_survey_mst
                    (survey_uid, title, description, start_date, end_date, use_yn, delete_flg, reg_dtm, reg_id, mod_dtm, mod_id)
                values
                    (?, ?, ?, ?, ?, ?, 'N', ?, 'test', ?, 'test')
                """,
                surveyUid,
                title,
                "설명",
                startDate,
                endDate,
                useYn,
                regDtm,
                regDtm
        );
    }
}
