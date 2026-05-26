package com.reven.project.service.co.dto;

public record COAdminWriteRequestDto(
        Long adminSeq,
        String loginId,
        String name,
        String status,
        String password
) {
}
