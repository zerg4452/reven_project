package com.reven.project.service.co.mapper;

import com.reven.project.service.co.dto.COAdminMapperSearchRequestDto;
import com.reven.project.service.co.dto.COAdminListItemResponseDto;
import com.reven.project.service.co.dto.COAdminManagementSearchRequestDto;
import com.reven.project.service.co.dto.COAdminSessionDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface COAdminMapper {

    /** 로그인 ID로 활성 관리자 계정을 조회한다. */
    COAdminSessionDto selectAdminByLoginId(COAdminMapperSearchRequestDto requestDto);

    /** 관리자 관리 목록의 전체 건수를 조회한다. */
    long countAdmins(COAdminManagementSearchRequestDto requestDto);

    /** 관리자 관리 목록을 조회한다. */
    List<COAdminListItemResponseDto> selectAdmins(COAdminManagementSearchRequestDto requestDto);
}
