package com.reven.project.service.bd.dto;

public record BDPhotoBoardSaveRequestDto(
        Long photoSeq,
        String title,
        String publishYn,
        String actorId
) {
}
