package com.reven.project.service.co.mapper;

import com.reven.project.service.co.dto.COAccessLogCreateRequestDto;
import com.reven.project.service.co.dto.COAccessLogResponseDto;
import com.reven.project.service.co.dto.COAccessLogSearchRequestDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface COAccessLogMapper {

    /** 관리자 접속 이력을 등록한다. */
    int insertAccessLog(COAccessLogCreateRequestDto requestDto);

    /** 접속 이력 검색 결과의 전체 건수를 조회한다. */
    long countAccessLogs(COAccessLogSearchRequestDto requestDto);

    /** 접속 이력 목록을 검색 조건으로 조회한다. */
    List<COAccessLogResponseDto> selectAccessLogs(COAccessLogSearchRequestDto requestDto);
}
