package com.reven.project.service.co.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record COAdminMenuResponseDto(
        Long adminMenuSeq,
        String menuCode,
        String parentMenuCode,
        Integer depthNo,
        String menuName,
        String menuUrl,
        String matchUrlsJson,
        String matchUrlsText,
        String menuType,
        String boardKey,
        String useYn,
        String deleteFlg,
        Integer sortOrder,
        LocalDate registeredDate,
        LocalDate updatedDate,
        LocalDateTime registeredAt,
        String registeredBy,
        LocalDateTime modifiedAt,
        String modifiedBy
) {
}
