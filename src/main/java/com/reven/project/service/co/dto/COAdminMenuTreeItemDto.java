package com.reven.project.service.co.dto;

public record COAdminMenuTreeItemDto(
        Long adminMenuSeq,
        Long parentAdminMenuSeq,
        String menuCode,
        String parentMenuCode,
        Integer depthNo,
        String menuName,
        String menuUrl,
        String menuType,
        String useYn,
        Integer sortOrder,
        boolean selected
) {
}
