// 워크로그를 월별로 묶어 타임라인 화면에 전달하는 응답 DTO
package com.reven.project.service.co.dto;

import java.util.List;

public record COWorklogMonthResponseDto(

        String monthLabel,

        List<COWorklogDayResponseDto> days

) {}
