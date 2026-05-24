package com.reven.project.service.co.dto;

import java.time.LocalDate;

public record COAdminHomeResponseDto(
        String screenName,
        String purpose,
        LocalDate baseDate,
        long todayAccessCount
) {
}
