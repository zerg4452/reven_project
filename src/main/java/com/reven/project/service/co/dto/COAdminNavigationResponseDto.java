package com.reven.project.service.co.dto;

import java.util.List;

public record COAdminNavigationResponseDto(
        List<COAdminNavigationItemDto> gnbItems,
        COAdminNavigationItemDto activeRoot,
        List<COAdminNavigationItemDto> lnbItems
) {
}
