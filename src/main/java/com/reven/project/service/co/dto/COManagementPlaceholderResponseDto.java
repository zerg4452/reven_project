package com.reven.project.service.co.dto;

import java.time.LocalDate;
import java.util.List;

public record COManagementPlaceholderResponseDto(
        String screenName,
        String purpose,
        LocalDate baseDate,
        List<String> pendingSections
) {
}
