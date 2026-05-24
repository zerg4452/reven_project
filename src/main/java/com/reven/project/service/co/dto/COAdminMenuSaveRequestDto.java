package com.reven.project.service.co.dto;

public record COAdminMenuSaveRequestDto(
        Long adminMenuSeq,
        String menuCode,
        String parentMenuCode,
        String menuName,
        String menuUrl,
        String matchUrlsText,
        String menuType,
        String boardKey,
        String useYn,
        Integer sortOrder,
        String actorId
) {
}
