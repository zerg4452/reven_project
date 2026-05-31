// 워크로그 하루치 항목을 타임라인 카드 단위로 담는 DTO
package com.reven.project.service.co.dto;

import java.util.List;

public record COWorklogDayResponseDto(

        String dateLabel,

        List<String> entries

) {}
