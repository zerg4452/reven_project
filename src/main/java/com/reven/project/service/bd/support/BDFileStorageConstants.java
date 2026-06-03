// bd 게시판 파일 저장 경로·유형·확장자 공통 상수
package com.reven.project.service.bd.support;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public final class BDFileStorageConstants {

    public static final DateTimeFormatter STORAGE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public static final String FILE_TYPE_THUMB = "THUMB";
    public static final String FILE_TYPE_ATTACH = "ATTACH";

    public static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp", "avif");

    public static final Set<String> NOTICE_ATTACHMENT_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "avif",
            "pdf", "hwp", "hwpx", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "zip", "txt", "csv");

    public static final Set<String> PHOTO_BOARD_EXTENSIONS;

    static {
        Set<String> extensions = new HashSet<>(IMAGE_EXTENSIONS);
        extensions.add("mp4");
        PHOTO_BOARD_EXTENSIONS = Set.copyOf(extensions);
    }

    private BDFileStorageConstants() {
    }
}
