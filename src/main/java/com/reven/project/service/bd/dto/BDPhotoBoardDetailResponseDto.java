package com.reven.project.service.bd.dto;

import java.time.LocalDateTime;

public record BDPhotoBoardDetailResponseDto(
        Long photoSeq,
        String title,
        String publishYn,
        String deleteFlg,
        LocalDateTime registeredAt,
        String registeredBy,
        LocalDateTime modifiedAt,
        String modifiedBy
) {
    public Long id() {
        return photoSeq;
    }

    public String publishText() {
        return "Y".equals(publishYn) ? "게시" : "대기";
    }
}
