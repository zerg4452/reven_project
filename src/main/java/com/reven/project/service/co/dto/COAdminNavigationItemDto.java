package com.reven.project.service.co.dto;

import java.util.List;

public record COAdminNavigationItemDto(
        String menuCode,
        String parentMenuCode,
        Integer depthNo,
        String menuName,
        String href,
        boolean active,
        List<COAdminNavigationItemDto> children
) {
}
