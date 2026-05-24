package com.reven.project.service.sa.mapper;

import com.reven.project.service.sa.dto.SADto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SASurveySubmitMapper {
    /** 설문 제출 마스터를 등록한다. */
    void insertSubmission(SADto.SubmitInsert submission);

    /** 설문 제출 답변 snapshot을 등록한다. */
    void insertAnswer(SADto.AnswerInsert answer);

    /** 설문 이력 목록을 검색 조건으로 조회한다. */
    List<SADto.SubmissionListItem> selectSubmissionList(SADto.SubmissionSearchRequest request);

    /** 제출 UID 기준으로 설문 이력 마스터를 조회한다. */
    SADto.SubmissionDetail selectSubmission(@Param("submitUid") String submitUid);

    /** 설문 이력 상세 답변 snapshot 목록을 조회한다. */
    List<SADto.AnswerSnapshot> selectSubmissionAnswers(@Param("submitSeq") Long submitSeq);

    /** CSV 출력용 설문 이력/답변 row를 조회한다. */
    List<SADto.CsvRow> selectCsvRows(SADto.SubmissionSearchRequest request);
}
