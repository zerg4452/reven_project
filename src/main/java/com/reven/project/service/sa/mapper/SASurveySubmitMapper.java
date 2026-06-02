package com.reven.project.service.sa.mapper;

import com.reven.project.service.sa.dto.SASurveyDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SASurveySubmitMapper {
    /** 설문 제출 마스터를 등록한다. */
    void insertSubmission(SASurveyDto.SubmitInsert submission);

    /** 설문 제출 답변 snapshot을 등록한다. */
    void insertAnswer(SASurveyDto.AnswerInsert answer);

    /** 설문 이력 목록을 검색 조건으로 조회한다. */
    List<SASurveyDto.SubmissionListItem> selectSubmissionList(SASurveyDto.SubmissionSearchRequest request);

    /** 제출 UID 기준으로 설문 이력 마스터를 조회한다. */
    SASurveyDto.SubmissionDetail selectSubmission(@Param("submitUid") String submitUid);

    /** 설문 이력 상세 답변 snapshot 목록을 조회한다. */
    List<SASurveyDto.AnswerSnapshot> selectSubmissionAnswers(@Param("submitSeq") Long submitSeq);

    /** CSV 출력용 설문 이력/답변 row를 조회한다. */
    List<SASurveyDto.CsvRow> selectCsvRows(SASurveyDto.SubmissionSearchRequest request);

    /** 설문 이력의 상태와 관리자 메모를 변경한다. */
    void updateSubmission(@Param("submitUid") String submitUid,
                          @Param("status") String status,
                          @Param("adminMemo") String adminMemo);
}
