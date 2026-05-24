package com.reven.project.service.co.dto;

import java.time.LocalDate;

public record COAdminListItemResponseDto(
        Long adminSeq,
        String loginId,
        String name,
        String role,
        String status,
        LocalDate registeredDate,
        LocalDate updatedDate
) {
}
