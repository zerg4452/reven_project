package com.reven.project.service.bd.mapper;

import com.reven.project.service.bd.dto.BDPhotoBoardDetailResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardFileResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardFileSaveCommand;
import com.reven.project.service.bd.dto.BDPhotoBoardListItemResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicListItemResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicSearchRequestDto;
import com.reven.project.service.bd.dto.BDPhotoBoardSaveCommand;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BDPhotoBoardMapper {

    /** 사진 게시판 목록을 조회한다. */
    List<BDPhotoBoardListItemResponseDto> selectPhotoBoardList();

    /** 사진 게시판 단건 상세를 조회한다. */
    BDPhotoBoardDetailResponseDto selectPhotoBoardDetail(@Param("photoSeq") Long photoSeq);

    /** 사진 게시판 첨부 파일 목록을 조회한다. */
    List<BDPhotoBoardFileResponseDto> selectPhotoBoardFiles(@Param("photoSeq") Long photoSeq);

    /** 사진 게시판 첨부 단건을 조회한다. */
    BDPhotoBoardFileResponseDto selectPhotoBoardFile(@Param("photoFileSeq") Long photoFileSeq);

    /** 사용자 포토 게시판 목록을 조회한다. */
    List<BDPhotoBoardPublicListItemResponseDto> selectPublicPhotoBoardList(BDPhotoBoardPublicSearchRequestDto search);

    /** 사용자 포토 게시판 목록 건수를 조회한다. */
    int selectPublicPhotoBoardCount(BDPhotoBoardPublicSearchRequestDto search);

    /** 사용자 포토 게시판 단건 상세를 조회한다. */
    BDPhotoBoardDetailResponseDto selectPublicPhotoBoardDetail(@Param("photoSeq") Long photoSeq);

    /** 사용자 포토 게시판 첨부 단건을 조회한다. */
    BDPhotoBoardFileResponseDto selectPublicPhotoBoardFile(@Param("photoFileSeq") Long photoFileSeq);

    /** 사진 게시판 원고를 등록한다. */
    int insertPhotoBoard(BDPhotoBoardSaveCommand requestDto);

    /** 사진 게시판 원고를 수정한다. */
    int updatePhotoBoard(BDPhotoBoardSaveCommand requestDto);

    /** 사진 게시판을 soft delete 처리한다. */
    int deletePhotoBoard(@Param("photoSeq") Long photoSeq, @Param("actorId") String actorId);

    /** 사진 게시판 첨부 파일 메타를 등록한다. */
    int insertPhotoBoardFile(BDPhotoBoardFileSaveCommand requestDto);

    /** 사진 게시판 첨부 파일을 soft delete 처리한다. */
    int deletePhotoBoardFiles(@Param("photoSeq") Long photoSeq, @Param("actorId") String actorId);

    /** 사진 게시판 첨부 파일 단건을 soft delete 처리한다. */
    int deletePhotoBoardFile(@Param("photoFileSeq") Long photoFileSeq, @Param("actorId") String actorId);

    /** 사진 게시판 조회수를 1 증가시킨다. */
    int increaseViewCount(@Param("photoSeq") Long photoSeq);
}
