package com.reven.project.service.sa.mapper;

import com.reven.project.service.sa.dto.SASurveyDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SASurveyMapper {
    /** 관리자 설문 관리 목록을 조회한다. */
    List<SASurveyDto.SurveyListItem> selectSurveyList(SASurveyDto.SurveySearchRequest request);

    /** 사용자 화면에 노출할 사용 중 설문 목록을 조회한다. */
    List<SASurveyDto.SurveyListItem> selectPublicSurveyList(@Param("today") LocalDate today);

    /** 사용자 설문 카드 화면에 노출할 삭제되지 않은 설문 목록을 조회한다. */
    List<SASurveyDto.SurveyListItem> selectPublicSurveyCardList(@Param("today") LocalDate today);

    /** 사용자 메인 화면에 노출할 사용 중 설문 최신 목록을 조회한다. */
    List<SASurveyDto.SurveyListItem> selectPublicSurveySummaryList(@Param("limit") int limit, @Param("today") LocalDate today);

    /** 공개 UID 기준으로 설문 마스터를 조회한다. */
    SASurveyDto.SurveyDetail selectSurvey(@Param("surveyUid") String surveyUid);

    /** 설문 문항 목록을 정렬 순서대로 조회한다. */
    List<SASurveyDto.SurveyField> selectSurveyFields(@Param("surveySeq") Long surveySeq);

    /** 설문 문항의 선택형 보기 목록을 조회한다. */
    List<SASurveyDto.SurveyOption> selectSurveyOptions(@Param("surveySeq") Long surveySeq);

    /** 설문 마스터를 등록한다. */
    void insertSurvey(SASurveyDto.SurveyDetail survey);

    /** 설문 마스터를 수정한다. */
    void updateSurvey(SASurveyDto.SurveyDetail survey);

    /** 설문에 속한 문항을 삭제한다. */
    void deleteSurveyFields(@Param("surveySeq") Long surveySeq);

    /** 설문에 속한 보기 목록을 삭제한다. */
    void deleteSurveyOptions(@Param("surveySeq") Long surveySeq);

    /** 설문 마스터를 삭제 상태로 전환한다. */
    void deleteSurvey(@Param("surveySeq") Long surveySeq);

    /** 설문 문항을 등록한다. */
    void insertSurveyField(SASurveyDto.SurveyField field);

    /** 설문 문항 보기를 등록한다. */
    void insertSurveyOption(SASurveyDto.SurveyOption option);
}
