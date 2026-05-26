package com.reven.project.service.co.dto;

import java.time.LocalDateTime;

public record COAdminDetailResponseDto(
        Long adminSeq,
        String loginId,
        String name,
        String role,
        String status,
        String passwordHash,
        LocalDateTime regDtm,
        String regId,
        LocalDateTime modDtm,
        String modId
) {
}
