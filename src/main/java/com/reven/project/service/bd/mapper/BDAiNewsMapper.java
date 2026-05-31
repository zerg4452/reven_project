package com.reven.project.service.bd.mapper;

import com.reven.project.service.bd.dto.BDAiNewsDetailResponseDto;
import com.reven.project.service.bd.dto.BDAiNewsListItemResponseDto;
import com.reven.project.service.bd.dto.BDAiNewsSaveRequestDto;
import com.reven.project.service.bd.dto.BDAiNewsSearchRequestDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BDAiNewsMapper {

    /** 검색 조건에 맞는 AI News 전체 건수를 조회한다. */
    long countAiNews(BDAiNewsSearchRequestDto requestDto);

    /** 검색 조건에 맞는 AI News 목록을 조회한다. */
    List<BDAiNewsListItemResponseDto> selectAiNewsList(BDAiNewsSearchRequestDto requestDto);

    /** 사용자 화면에 게시 상태 AI News 최신 목록을 조회한다. */
    List<BDAiNewsDetailResponseDto> selectPublishedAiNewsList(@Param("keyword") String keyword, @Param("limit") int limit);

    /** AI News 일련번호 기준 상세를 조회한다. */
    BDAiNewsDetailResponseDto selectAiNewsDetail(@Param("newsSeq") Long newsSeq);

    /** 사용자 화면에서 게시 상태 AI News 상세를 조회한다. */
    BDAiNewsDetailResponseDto selectPublishedAiNewsDetail(@Param("newsSeq") Long newsSeq);

    /** slug 중복 확인과 등록 후 재조회에 사용할 상세를 조회한다. */
    BDAiNewsDetailResponseDto selectAiNewsBySlug(@Param("slug") String slug);

    /** AI News 원고를 등록한다. */
    int insertAiNews(BDAiNewsSaveRequestDto requestDto);

    /** AI News 원고를 수정한다. */
    int updateAiNews(BDAiNewsSaveRequestDto requestDto);

    /** AI News 원고를 soft delete 처리한다. */
    int deleteAiNews(@Param("newsSeq") Long newsSeq);

    /** AI News 조회수를 1 증가시킨다. */
    int increaseViewCount(@Param("newsSeq") Long newsSeq);
}
