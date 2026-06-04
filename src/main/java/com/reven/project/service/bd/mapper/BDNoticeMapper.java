package com.reven.project.service.bd.mapper;

import com.reven.project.service.bd.dto.BDNoticeDetailResponseDto;
import com.reven.project.service.bd.dto.BDNoticeFileResponseDto;
import com.reven.project.service.bd.dto.BDNoticeFileSaveCommand;
import com.reven.project.service.bd.dto.BDNoticeAdminSearchRequestDto;
import com.reven.project.service.bd.dto.BDNoticeListItemResponseDto;
import com.reven.project.service.bd.dto.BDNoticePublicListItemResponseDto;
import com.reven.project.service.bd.dto.BDNoticePublicSearchRequestDto;
import com.reven.project.service.bd.dto.BDNoticeSaveCommand;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BDNoticeMapper {

    /** 관리자 공지사항 목록을 조회한다. */
    List<BDNoticeListItemResponseDto> selectNoticeList(BDNoticeAdminSearchRequestDto search);

    /** 관리자 공지사항 목록 건수를 조회한다. */
    int selectNoticeCount(BDNoticeAdminSearchRequestDto search);

    /** 공지사항 단건 상세를 조회한다. */
    BDNoticeDetailResponseDto selectNoticeDetail(@Param("noticeSeq") Long noticeSeq);

    /** 공지사항 첨부 파일 목록을 조회한다. fileType이 null이면 전체를 조회한다. */
    List<BDNoticeFileResponseDto> selectNoticeFiles(@Param("noticeSeq") Long noticeSeq, @Param("fileType") String fileType);

    /** 공지사항 첨부 단건을 조회한다. */
    BDNoticeFileResponseDto selectNoticeFile(@Param("noticeFileSeq") Long noticeFileSeq);

    /** 사용자 공개 고정 공지 목록을 조회한다(검색·페이징 미적용). */
    List<BDNoticePublicListItemResponseDto> selectPublicPinnedNotices();

    /** 사용자 공개 일반 공지 목록을 조회한다. */
    List<BDNoticePublicListItemResponseDto> selectPublicNoticeList(BDNoticePublicSearchRequestDto search);

    /** 사용자 공개 일반 공지 건수를 조회한다. */
    int selectPublicNoticeCount(BDNoticePublicSearchRequestDto search);

    /** 사용자 공개 공지 단건 상세를 조회한다. */
    BDNoticeDetailResponseDto selectPublicNoticeDetail(@Param("noticeSeq") Long noticeSeq);

    /** 사용자 공개 공지 첨부 단건을 조회한다. */
    BDNoticeFileResponseDto selectPublicNoticeFile(@Param("noticeFileSeq") Long noticeFileSeq);

    /** 공지사항을 등록한다. */
    int insertNotice(BDNoticeSaveCommand command);

    /** 공지사항을 수정한다. */
    int updateNotice(BDNoticeSaveCommand command);

    /** 공지사항을 soft delete 처리한다. */
    int deleteNotice(@Param("noticeSeq") Long noticeSeq, @Param("actorId") String actorId);

    /** 공지사항 첨부 파일 메타를 등록한다. */
    int insertNoticeFile(BDNoticeFileSaveCommand command);

    /** 공지사항 첨부 단건을 soft delete 처리한다. */
    int deleteNoticeFile(@Param("noticeFileSeq") Long noticeFileSeq, @Param("actorId") String actorId);

    /** 공지사항 첨부 전체를 soft delete 처리한다. */
    int deleteNoticeFiles(@Param("noticeSeq") Long noticeSeq, @Param("actorId") String actorId);

    /** 공지사항 조회수를 1 증가시킨다. */
    int increaseViewCount(@Param("noticeSeq") Long noticeSeq);
}
