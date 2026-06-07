// bd 게시판 파일의 디스크 저장·경로 해석·커밋 후 삭제를 담당하는 공통 헬퍼
package com.reven.project.service.bd.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

public class BDFileStorageSupport {

    private final Path rootPath;

    public BDFileStorageSupport(String rootPath) {
        this.rootPath = Paths.get(rootPath).toAbsolutePath().normalize();
    }

    public record StoredFile(String storedFileName, String storedPath) {
    }

    public record StoredFileRef(String storedPath, String storedFileName) {
    }

    public static long megabytesToBytes(int megabytes) {
        if (megabytes <= 0) {
            throw new IllegalArgumentException("업로드 용량 제한은 1MB 이상이어야 합니다.");
        }
        return megabytes * 1024L * 1024L;
    }

    public static String fileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("파일 확장자를 확인할 수 없습니다.");
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    public static String sanitizeFileName(String fileName) {
        Path path = Path.of(fileName);
        Path name = path.getFileName();
        return name == null ? fileName : name.toString();
    }

    public static List<MultipartFile> normalizeFiles(List<MultipartFile> uploadedFiles) {
        if (uploadedFiles == null || uploadedFiles.isEmpty()) {
            return List.of();
        }
        return uploadedFiles.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
    }

    public static void cleanupWrittenFiles(List<Path> writtenFiles) {
        for (Path path : writtenFiles) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // best effort cleanup
            }
        }
    }

    public StoredFile writeToDisk(MultipartFile file, String extension, List<Path> writtenFiles) throws IOException {
        String storedFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String storedPath = BDFileStorageConstants.STORAGE_PATH_FORMATTER.format(LocalDate.now());
        Path directory = rootPath.resolve(storedPath).normalize();
        Files.createDirectories(directory);
        Path target = directory.resolve(storedFileName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target);
        }
        writtenFiles.add(target);
        return new StoredFile(storedFileName, storedPath);
    }

    public Path resolveStoredFilePath(String storedPath, String storedFileName) {
        Path directory = storedPath == null || storedPath.isBlank()
                ? rootPath
                : rootPath.resolve(storedPath);
        Path resolved = directory.resolve(storedFileName).normalize();
        if (!resolved.startsWith(rootPath)) {
            return null;
        }
        return resolved;
    }

    public void scheduleCleanupAfterCommit(List<StoredFileRef> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            cleanupStoredFiles(files);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cleanupStoredFiles(files);
            }
        });
    }

    private void cleanupStoredFiles(List<StoredFileRef> files) {
        for (StoredFileRef file : files) {
            Path path = resolveStoredFilePath(file.storedPath(), file.storedFileName());
            if (path == null) {
                continue;
            }
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // best effort cleanup
            }
        }
    }
}
