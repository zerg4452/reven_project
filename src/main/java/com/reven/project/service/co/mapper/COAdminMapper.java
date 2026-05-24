package com.reven.project.service.co.mapper;

import com.reven.project.service.co.dto.COAdminMapperSearchRequestDto;
import com.reven.project.service.co.dto.COAdminSessionDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface COAdminMapper {

    /** 로그인 ID로 활성 관리자 계정을 조회한다. */
    COAdminSessionDto selectAdminByLoginId(COAdminMapperSearchRequestDto requestDto);
}
