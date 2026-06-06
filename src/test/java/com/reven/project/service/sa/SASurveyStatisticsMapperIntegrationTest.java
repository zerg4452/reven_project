package com.reven.project.service.sa;

// 설문 통계 MyBatis 스냅샷 집계 쿼리 통합 테스트

import com.reven.project.service.sa.dto.SASurveyDto;
import com.reven.project.service.sa.mapper.SASurveySubmitMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SASurveyStatisticsMapperIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SASurveySubmitMapper submitMapper;

    @Test
    void statisticQueriesReadAnswersBySnapshotFieldKeyAfterFieldSeqChanges() {
        long surveySeq = 990001L;
        insertSubmission(surveySeq, "stats-submit-990001", "new");
        Long submitSeq = jdbcTemplate.queryForObject(
                "select submit_seq from sa_survey_submit_mst where submit_uid = ?",
                Long.class,
                "stats-submit-990001"
        );
        insertAnswer(submitSeq, 1001L, "q1", "수정 전 문항", "radio", "수정 전 보기", null, 1);

        List<SASurveyDto.FieldStatistics> fields = submitMapper.selectStatisticFields(surveySeq);
        List<SASurveyDto.OptionFrequency> frequencies = submitMapper.selectObjectiveOptionFrequencies(surveySeq, "q1");

        assertThat(fields)
                .extracting(field -> field.fieldKey + ":" + field.fieldLabel + ":" + field.surveyType)
                .containsExactly("q1:수정 전 문항:objective");
        assertThat(frequencies)
                .extracting(frequency -> frequency.optionLabel + ":" + frequency.count)
                .containsExactly("수정 전 보기:1");
    }

    @Test
    void objectiveFrequencyExpandsCheckboxAnswerJsonWithoutSplittingCommas() {
        long surveySeq = 990002L;
        insertSubmission(surveySeq, "stats-submit-990002", "new");
        Long submitSeq = jdbcTemplate.queryForObject(
                "select submit_seq from sa_survey_submit_mst where submit_uid = ?",
                Long.class,
                "stats-submit-990002"
        );
        insertAnswer(submitSeq, 1002L, "checks", "체크 문항", "checkbox", "예, 동의", "[\"예, 동의\"]", 1);

        List<SASurveyDto.OptionFrequency> frequencies = submitMapper.selectObjectiveOptionFrequencies(surveySeq, "checks");

        assertThat(frequencies)
                .extracting(frequency -> frequency.optionLabel + ":" + frequency.count)
                .containsExactly("예, 동의:1");
    }

    @Test
    void recentTextAnswersAreLimitedInSql() {
        long surveySeq = 990003L;
        insertSubmission(surveySeq, "stats-submit-990003", "new");
        Long submitSeq = jdbcTemplate.queryForObject(
                "select submit_seq from sa_survey_submit_mst where submit_uid = ?",
                Long.class,
                "stats-submit-990003"
        );
        for (int index = 1; index <= 25; index++) {
            insertAnswer(submitSeq, 2000L + index, "memo", "메모", "text", "답변" + index, null, 1);
        }

        List<String> answers = submitMapper.selectRecentTextAnswers(surveySeq, "memo");

        assertThat(answers)
                .hasSize(20)
                .first()
                .isEqualTo("답변25");
    }

    private void insertSubmission(long surveySeq, String submitUid, String status) {
        jdbcTemplate.update("""
                insert into sa_survey_submit_mst
                    (survey_seq, survey_uid, submit_uid, survey_title_snapshot, submitter_name, phone, email, status, submitted_dtm, ip, reg_dtm, reg_id, mod_dtm, mod_id)
                values
                    (?, ?, ?, ?, ?, ?, ?, ?, now(), ?, now(), 'test', now(), 'test')
                """,
                surveySeq,
                "survey-stats-uid",
                submitUid,
                "통계 테스트 설문",
                "제출자",
                "010-0000-0000",
                "tester@example.com",
                status,
                "127.0.0.1"
        );
    }

    private void insertAnswer(Long submitSeq, Long fieldSeq, String fieldKey, String fieldLabel, String fieldType, String answerValue, String answerJson, int sortOrd) {
        jdbcTemplate.update("""
                insert into sa_survey_answer_dtl
                    (submit_seq, field_seq, field_key_snapshot, field_label_snapshot, field_type_snapshot, required_yn_snapshot, answer_value, answer_json, sort_ord, reg_dtm, reg_id, mod_dtm, mod_id)
                values
                    (?, ?, ?, ?, ?, 'N', ?, ?, ?, now(), 'test', now(), 'test')
                """,
                submitSeq,
                fieldSeq,
                fieldKey,
                fieldLabel,
                fieldType,
                answerValue,
                answerJson,
                sortOrd
        );
    }
}
