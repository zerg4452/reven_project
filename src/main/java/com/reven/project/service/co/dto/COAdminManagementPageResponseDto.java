package com.reven.project.service.co.dto;

import java.util.List;

public record COAdminManagementPageResponseDto(
        COAdminManagementSearchRequestDto search,
        long totalCount,
        List<COAdminListItemResponseDto> admins
) {
}
