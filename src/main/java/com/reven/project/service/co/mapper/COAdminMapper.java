package com.reven.project.service.co.mapper;

import com.reven.project.service.co.dto.COAdminMapperSearchRequestDto;
import com.reven.project.service.co.dto.COAdminDetailResponseDto;
import com.reven.project.service.co.dto.COAdminListItemResponseDto;
import com.reven.project.service.co.dto.COAdminManagementSearchRequestDto;
import com.reven.project.service.co.dto.COAdminSessionDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface COAdminMapper {

    /** 로그인 ID로 활성 관리자 계정을 조회한다. */
    COAdminSessionDto selectAdminByLoginId(COAdminMapperSearchRequestDto requestDto);

    /** 로그인 ID로 관리자 계정을 상태와 무관하게 조회한다. */
    COAdminSessionDto selectAdminByLoginIdAnyStatus(COAdminMapperSearchRequestDto requestDto);

    /** 관리자 일련번호로 관리자 상세를 조회한다. */
    COAdminDetailResponseDto selectAdminBySeq(@Param("adminSeq") Long adminSeq);

    /** 관리자 관리 목록의 전체 건수를 조회한다. */
    long countAdmins(COAdminManagementSearchRequestDto requestDto);

    /** 관리자 관리 목록을 조회한다. */
    List<COAdminListItemResponseDto> selectAdmins(COAdminManagementSearchRequestDto requestDto);

    /** 관리자를 등록한다. */
    int insertAdmin(COAdminDetailResponseDto admin);

    /** 관리자를 수정한다. */
    int updateAdmin(COAdminDetailResponseDto admin);

    /** 관리자를 삭제한다. */
    int deleteAdmin(@Param("adminSeq") Long adminSeq);
}
