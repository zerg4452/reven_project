package com.reven.project.service.co.mapper;

import com.reven.project.service.co.dto.COAdminMenuResponseDto;
import com.reven.project.service.co.dto.COAdminMenuSaveRequestDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface COAdminMenuMapper {

    /** 삭제되지 않은 관리자 메뉴 전체를 계층 정렬 순서로 조회한다. */
    List<COAdminMenuResponseDto> selectAdminMenus();

    /** 일련번호 기준 관리자 메뉴를 조회한다. */
    COAdminMenuResponseDto selectAdminMenuBySeq(@Param("adminMenuSeq") Long adminMenuSeq);

    /** 메뉴 코드 기준 관리자 메뉴를 조회한다. */
    COAdminMenuResponseDto selectAdminMenuByCode(@Param("menuCode") String menuCode);

    /** 하위 메뉴 개수를 조회한다. */
    long countChildren(@Param("parentMenuCode") String parentMenuCode);

    /** 관리자 메뉴를 등록한다. */
    int insertAdminMenu(@Param("menu") COAdminMenuSaveRequestDto menu, @Param("depthNo") int depthNo, @Param("matchUrlsJson") String matchUrlsJson);

    /** 관리자 메뉴를 수정한다. */
    int updateAdminMenu(@Param("menu") COAdminMenuSaveRequestDto menu, @Param("depthNo") int depthNo, @Param("matchUrlsJson") String matchUrlsJson);

    /** 관리자 메뉴를 soft delete 처리한다. */
    int deleteAdminMenu(@Param("adminMenuSeq") Long adminMenuSeq, @Param("actorId") String actorId);
}
