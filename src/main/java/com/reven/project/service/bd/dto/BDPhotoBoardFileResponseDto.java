package com.reven.project.service.bd.dto;

import java.time.LocalDateTime;

public record BDPhotoBoardFileResponseDto(
        Long photoFileSeq,
        Long photoSeq,
        String originalFileName,
        String storedFileName,
        String storedPath,
        String contentType,
        Long fileSize,
        Integer sortOrder,
        String deleteFlg,
        LocalDateTime registeredAt,
        String registeredBy,
        LocalDateTime modifiedAt,
        String modifiedBy,
        String fileUrl
) {
    public String displayFileSizeKb() {
        long size = fileSize == null ? 0L : fileSize;
        long kb = Math.max(1L, (size + 1023L) / 1024L);
        return kb + " KB";
    }
}
