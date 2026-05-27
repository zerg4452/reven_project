package com.reven.project.service.bd.dto;

public record BDPhotoBoardFileSaveRequestDto(
        Long photoSeq,
        String originalFileName,
        String storedFileName,
        String storedPath,
        String contentType,
        Long fileSize,
        Integer sortOrder,
        String actorId
) {
}
