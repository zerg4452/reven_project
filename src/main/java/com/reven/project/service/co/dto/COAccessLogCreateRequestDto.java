package com.reven.project.service.co.dto;

import java.time.LocalDateTime;

public record COAccessLogCreateRequestDto(
        Long adminSeq,
        String loginId,
        String requestUri,
        String method,
        String remoteAddr,
        String userAgent,
        LocalDateTime accessAt
) {
}
