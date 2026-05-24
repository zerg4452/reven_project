package com.reven.project.service.co.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public record COAdminSessionDto(
        Long adminSeq,
        String loginId,
        String passwordHash,
        String adminName,
        String roleCsv,
        LocalDateTime lastLoginAt
) {
    public List<String> roles() {
        if (roleCsv == null || roleCsv.isBlank()) {
            return List.of("ADMIN");
        }
        return Arrays.stream(roleCsv.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .toList();
    }
}
