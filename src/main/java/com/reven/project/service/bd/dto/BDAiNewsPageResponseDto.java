package com.reven.project.service.bd.dto;

import java.util.List;

public record BDAiNewsPageResponseDto(
        BDAiNewsSearchRequestDto search,
        long totalCount,
        List<BDAiNewsListItemResponseDto> news
) {
}
