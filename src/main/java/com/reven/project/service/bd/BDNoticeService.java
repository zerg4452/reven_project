// 공지사항 등록/조회/파일/조회수 처리를 담당하는 서비스
package com.reven.project.service.bd;

import com.reven.project.service.bd.dto.BDNoticeAdminSearchRequestDto;
import com.reven.project.service.bd.dto.BDNoticeDetailResponseDto;
import com.reven.project.service.bd.dto.BDNoticeFileResponseDto;
import com.reven.project.service.bd.dto.BDNoticeFileSaveCommand;
import com.reven.project.service.bd.dto.BDNoticeListItemResponseDto;
import com.reven.project.service.bd.dto.BDNoticePublicListItemResponseDto;
import com.reven.project.service.bd.dto.BDNoticePublicPageResponseDto;
import com.reven.project.service.bd.dto.BDNoticePublicSearchRequestDto;
import com.reven.project.service.bd.dto.BDNoticeSaveCommand;
import com.reven.project.service.bd.dto.BDNoticeSaveRequestDto;
import com.reven.project.service.bd.mapper.BDNoticeMapper;
import com.reven.project.service.bd.support.BDFileStorageConstants;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BDNoticeService {

    private final BDNoticeMapper noticeMapper;
    private final Path rootPath;
    private final String fileBaseUrl;
    private final int maxAttachments;
    private final int maxThumbnailSizeMb;
    private final int maxAttachmentSizeMb;
    private final long maxThumbnailBytes;
    private final long maxAttachmentBytes;

    public BDNoticeService(
            BDNoticeMapper noticeMapper,
            @Value("${reven.upload.notice.root-path}") String rootPath,
            @Value("${reven.upload.notice.base-url}") String fileBaseUrl,
            @Value("${reven.upload.notice.max-attachments:10}") int maxAttachments,
            @Value("${reven.upload.notice.max-thumbnail-size-mb:5}") int maxThumbnailSizeMb,
            @Value("${reven.upload.notice.max-attachment-size-mb:20}") int maxAttachmentSizeMb
    ) {
        this.noticeMapper = noticeMapper;
        this.rootPath = Paths.get(rootPath).toAbsolutePath().normalize();
        this.fileBaseUrl = fileBaseUrl;
        this.maxAttachments = maxAttachments;
        this.maxThumbnailSizeMb = maxThumbnailSizeMb;
        this.maxAttachmentSizeMb = maxAttachmentSizeMb;
        this.maxThumbnailBytes = megabytesToBytes(maxThumbnailSizeMb);
        this.maxAttachmentBytes = megabytesToBytes(maxAttachmentSizeMb);
    }

    /**
     * 관리자 공지사항 목록을 조회한다.
     */
    public List<BDNoticeListItemResponseDto> findNotices(BDNoticeAdminSearchRequestDto search) {
        return noticeMapper.selectNoticeList(normalizeAdminSearch(search));
    }

    /**
     * 관리자 목록 검색 조건을 기본값까지 보정한다.
     */
    public BDNoticeAdminSearchRequestDto normalizedAdminSearch(BDNoticeAdminSearchRequestDto search) {
        return normalizeAdminSearch(search);
    }

    /**
     * 공지사항 단건을 조회한다.
     */
    public BDNoticeDetailResponseDto findNotice(Long noticeSeq) {
        return noticeMapper.selectNoticeDetail(noticeSeq);
    }

    /**
     * 공지사항 첨부 파일 목록을 조회한다(관리자, fileType이 null이면 전체).
     */
    public List<BDNoticeFileResponseDto> findNoticeFiles(Long noticeSeq, String fileType) {
        return noticeMapper.selectNoticeFiles(noticeSeq, fileType).stream()
                .map(this::withFileUrl)
                .toList();
    }

    /**
     * 공지사항 썸네일 단건을 조회한다(관리자).
     */
    public BDNoticeFileResponseDto findNoticeThumbnail(Long noticeSeq) {
        List<BDNoticeFileResponseDto> thumbs = noticeMapper.selectNoticeFiles(noticeSeq, BDFileStorageConstants.FILE_TYPE_THUMB);
        return thumbs.isEmpty() ? null : withFileUrl(thumbs.get(0));
    }

    /**
     * 공지사항 첨부 파일 단건을 조회한다(관리자).
     */
    public BDNoticeFileResponseDto findNoticeFile(Long noticeFileSeq) {
        BDNoticeFileResponseDto file = noticeMapper.selectNoticeFile(noticeFileSeq);
        return file == null ? null : withFileUrl(file);
    }

    /**
     * 공지사항 첨부 파일의 실제 저장 경로를 구한다(관리자).
     */
    public Path resolveNoticeFilePath(Long noticeFileSeq) {
        BDNoticeFileResponseDto file = noticeMapper.selectNoticeFile(noticeFileSeq);
        if (file == null) {
            return null;
        }
        return resolveStoredFilePath(file.storedPath(), file.storedFileName());
    }

    /**
     * 사용자 공개 공지 목록을 조립한다(고정 목록 + 일반 페이지 목록).
     */
    public BDNoticePublicPageResponseDto searchPublicNotices(BDNoticePublicSearchRequestDto search) {
        BDNoticePublicSearchRequestDto normalized = search == null
                ? new BDNoticePublicSearchRequestDto("", 1, 10)
                : search.normalized();
        List<BDNoticePublicListItemResponseDto> pinned = noticeMapper.selectPublicPinnedNotices().stream()
                .map(this::withPublicThumbnailUrl)
                .toList();
        int totalCount = noticeMapper.selectPublicNoticeCount(normalized);
        int totalPages = totalCount == 0 ? 0 : (int) Math.ceil((double) totalCount / normalized.size());
        List<BDNoticePublicListItemResponseDto> notices = noticeMapper.selectPublicNoticeList(normalized).stream()
                .map(this::withPublicThumbnailUrl)
                .toList();
        return new BDNoticePublicPageResponseDto(normalized, pinned, notices, totalCount, totalPages);
    }

    /**
     * 사용자 공개 공지 단건을 조회한다(미래 게시일/노출N은 null).
     */
    public BDNoticeDetailResponseDto findPublicNotice(Long noticeSeq) {
        if (noticeSeq == null) {
            return null;
        }
        return noticeMapper.selectPublicNoticeDetail(noticeSeq);
    }

    /**
     * 사용자 공개 공지의 첨부 파일 목록을 조회한다(fileType이 null이면 전체).
     */
    public List<BDNoticeFileResponseDto> findPublicNoticeFiles(Long noticeSeq, String fileType) {
        if (noticeSeq == null || findPublicNotice(noticeSeq) == null) {
            return List.of();
        }
        return findPublicNoticeFilesForDetail(noticeSeq, fileType);
    }

    /**
     * 공개 상세에서 노출 검증이 끝난 뒤 첨부 파일 목록을 조회한다(fileType이 null이면 전체).
     */
    public List<BDNoticeFileResponseDto> findPublicNoticeFilesForDetail(Long noticeSeq, String fileType) {
        if (noticeSeq == null) {
            return List.of();
        }
        return noticeMapper.selectNoticeFiles(noticeSeq, fileType).stream()
                .map(this::withPublicFileUrl)
                .toList();
    }

    /**
     * 사용자 공개 공지 첨부 파일 단건을 조회한다.
     */
    public BDNoticeFileResponseDto findPublicNoticeFile(Long noticeFileSeq) {
        if (noticeFileSeq == null) {
            return null;
        }
        BDNoticeFileResponseDto file = noticeMapper.selectPublicNoticeFile(noticeFileSeq);
        return file == null ? null : withPublicFileUrl(file);
    }

    /**
     * 사용자 공개 공지 첨부 파일의 실제 저장 경로를 구한다.
     */
    public Path resolvePublicNoticeFilePath(Long noticeFileSeq) {
        if (noticeFileSeq == null) {
            return null;
        }
        BDNoticeFileResponseDto file = noticeMapper.selectPublicNoticeFile(noticeFileSeq);
        if (file == null) {
            return null;
        }
        return resolveStoredFilePath(file.storedPath(), file.storedFileName());
    }

    /**
     * 공지사항 조회수를 1 증가시킨다.
     */
    @Transactional
    public void increaseViewCount(Long noticeSeq) {
        if (noticeSeq != null) {
            noticeMapper.increaseViewCount(noticeSeq);
        }
    }

    /**
     * 공지사항을 등록하거나 수정한다.
     */
    @Transactional
    public Long saveNotice(
            BDNoticeSaveRequestDto requestDto,
            MultipartFile thumbnailFile,
            List<MultipartFile> attachFiles,
            List<Long> keepAttachFileSeqs
    ) {
        BDNoticeSaveRequestDto normalized = normalize(requestDto);
        MultipartFile newThumbnail = isEmptyFile(thumbnailFile) ? null : thumbnailFile;
        List<MultipartFile> newAttachments = normalizeFiles(attachFiles);
        validateThumbnailSize(newThumbnail);
        validateAttachmentSizes(newAttachments);

        BDNoticeDetailResponseDto existing = normalized.noticeSeq() == null
                ? null
                : noticeMapper.selectNoticeDetail(normalized.noticeSeq());
        if (normalized.noticeSeq() != null && existing == null) {
            throw new IllegalArgumentException("수정할 공지사항을 찾을 수 없습니다.");
        }

        List<BDNoticeFileResponseDto> existingAttachments = normalized.noticeSeq() == null
                ? List.of()
                : noticeMapper.selectNoticeFiles(normalized.noticeSeq(), BDFileStorageConstants.FILE_TYPE_ATTACH);
        Set<Long> keepSeqs = normalizeKeepSeqs(keepAttachFileSeqs, existingAttachments);
        List<BDNoticeFileResponseDto> removedAttachments = existingAttachments.stream()
                .filter(file -> !keepSeqs.contains(file.noticeFileSeq()))
                .toList();
        int retainedCount = existingAttachments.size() - removedAttachments.size();
        validateAttachmentCount(retainedCount, newAttachments.size());

        BDNoticeSaveCommand saveCommand = toSaveCommand(normalized);
        List<Path> writtenFiles = new ArrayList<>();
        try {
            Long noticeSeq;
            if (normalized.noticeSeq() == null) {
                noticeMapper.insertNotice(saveCommand);
                noticeSeq = saveCommand.getNoticeSeq();
            } else {
                noticeSeq = normalized.noticeSeq();
                for (BDNoticeFileResponseDto removed : removedAttachments) {
                    noticeMapper.deleteNoticeFile(removed.noticeFileSeq(), normalized.actorId());
                }
                if (!removedAttachments.isEmpty()) {
                    schedulePhysicalCleanupAfterCommit(removedAttachments);
                }
                noticeMapper.updateNotice(saveCommand);
            }

            if (newThumbnail != null) {
                replaceThumbnail(noticeSeq, newThumbnail, normalized.actorId(), writtenFiles);
            }

            int sortOrder = existingAttachments.stream()
                    .filter(file -> keepSeqs.contains(file.noticeFileSeq()))
                    .mapToInt(file -> file.sortOrder() == null ? 0 : file.sortOrder())
                    .max()
                    .orElse(0);
            for (MultipartFile file : newAttachments) {
                sortOrder++;
                storeNoticeFile(noticeSeq, file, BDFileStorageConstants.FILE_TYPE_ATTACH, sortOrder, normalized.actorId(), writtenFiles);
            }
            return noticeSeq;
        } catch (RuntimeException exception) {
            cleanupWrittenFiles(writtenFiles);
            throw exception;
        } catch (IOException exception) {
            cleanupWrittenFiles(writtenFiles);
            throw new IllegalStateException("공지사항 파일을 저장할 수 없습니다.", exception);
        }
    }

    /**
     * 공지사항을 soft delete 처리한다.
     */
    @Transactional
    public void deleteNotice(Long noticeSeq, String actorId) {
        BDNoticeDetailResponseDto existing = noticeMapper.selectNoticeDetail(noticeSeq);
        if (existing == null) {
            throw new IllegalArgumentException("삭제할 공지사항을 찾을 수 없습니다.");
        }
        List<BDNoticeFileResponseDto> files = noticeMapper.selectNoticeFiles(noticeSeq, null);
        noticeMapper.deleteNoticeFiles(noticeSeq, firstText(actorId, "system"));
        noticeMapper.deleteNotice(noticeSeq, firstText(actorId, "system"));
        schedulePhysicalCleanupAfterCommit(files);
    }

    private BDNoticeAdminSearchRequestDto normalizeAdminSearch(BDNoticeAdminSearchRequestDto search) {
        BDNoticeAdminSearchRequestDto request = search == null
                ? new BDNoticeAdminSearchRequestDto(null, null)
                : search;
        LocalDate endDate = request.endDate() == null ? LocalDate.now().plusDays(1) : request.endDate();
        LocalDate startDate = request.startDate() == null ? LocalDate.now().minusDays(60) : request.startDate();
        return new BDNoticeAdminSearchRequestDto(startDate, endDate);
    }

    private BDNoticeSaveRequestDto normalize(BDNoticeSaveRequestDto requestDto) {
        String title = firstText(requestDto.title()).trim();
        if (title.isBlank()) {
            throw new IllegalArgumentException("제목을 입력해 주세요.");
        }
        String publishYn = "Y".equalsIgnoreCase(requestDto.publishYn()) ? "Y" : "N";
        String pinYn = "Y".equalsIgnoreCase(requestDto.pinYn()) ? "Y" : "N";
        LocalDateTime publishDtm = requestDto.publishDtm() == null ? LocalDateTime.now() : requestDto.publishDtm();
        return new BDNoticeSaveRequestDto(
                requestDto.noticeSeq(),
                title,
                requestDto.content(),
                publishDtm,
                publishYn,
                pinYn,
                firstText(requestDto.actorId(), "system")
        );
    }

    private void replaceThumbnail(Long noticeSeq, MultipartFile thumbnailFile, String actorId, List<Path> writtenFiles) throws IOException {
        List<BDNoticeFileResponseDto> existingThumbs = noticeMapper.selectNoticeFiles(noticeSeq, BDFileStorageConstants.FILE_TYPE_THUMB);
        for (BDNoticeFileResponseDto thumb : existingThumbs) {
            noticeMapper.deleteNoticeFile(thumb.noticeFileSeq(), actorId);
        }
        if (!existingThumbs.isEmpty()) {
            schedulePhysicalCleanupAfterCommit(existingThumbs);
        }
        storeNoticeFile(noticeSeq, thumbnailFile, BDFileStorageConstants.FILE_TYPE_THUMB, 0, actorId, writtenFiles);
    }

    private void validateAttachmentCount(int retainedCount, int newCount) {
        if (retainedCount + newCount > maxAttachments) {
            throw new IllegalArgumentException("첨부 파일은 최대 " + maxAttachments + "개까지 등록할 수 있습니다.");
        }
    }

    private Set<Long> normalizeKeepSeqs(List<Long> keepAttachFileSeqs, List<BDNoticeFileResponseDto> existingAttachments) {
        if (keepAttachFileSeqs == null || keepAttachFileSeqs.isEmpty()) {
            return Set.of();
        }
        Set<Long> validSeqs = new HashSet<>();
        for (BDNoticeFileResponseDto file : existingAttachments) {
            validSeqs.add(file.noticeFileSeq());
        }
        Set<Long> keepSeqs = new HashSet<>();
        for (Long keepSeq : keepAttachFileSeqs) {
            if (keepSeq != null && validSeqs.contains(keepSeq)) {
                keepSeqs.add(keepSeq);
            }
        }
        return keepSeqs;
    }

    private List<MultipartFile> normalizeFiles(List<MultipartFile> uploadedFiles) {
        if (uploadedFiles == null || uploadedFiles.isEmpty()) {
            return List.of();
        }
        return uploadedFiles.stream()
                .filter(file -> !isEmptyFile(file))
                .toList();
    }

    private boolean isEmptyFile(MultipartFile file) {
        return file == null || file.isEmpty();
    }

    private BDNoticeSaveCommand toSaveCommand(BDNoticeSaveRequestDto requestDto) {
        BDNoticeSaveCommand command = new BDNoticeSaveCommand();
        command.setNoticeSeq(requestDto.noticeSeq());
        command.setTitle(requestDto.title());
        command.setContent(requestDto.content());
        command.setPublishYn(requestDto.publishYn());
        command.setPinYn(requestDto.pinYn());
        command.setPublishDtm(requestDto.publishDtm());
        command.setActorId(requestDto.actorId());
        return command;
    }

    private void storeNoticeFile(Long noticeSeq, MultipartFile file, String fileType, int sortOrder, String actorId, List<Path> writtenFiles) throws IOException {
        String originalFileName = sanitizeFileName(firstText(file.getOriginalFilename(), "notice"));
        String extension = fileExtension(originalFileName);
        Set<String> allowed = BDFileStorageConstants.FILE_TYPE_THUMB.equals(fileType)
                ? BDFileStorageConstants.IMAGE_EXTENSIONS
                : BDFileStorageConstants.NOTICE_ATTACHMENT_EXTENSIONS;
        if (!allowed.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
        }
        String contentType = firstText(file.getContentType()).toLowerCase(Locale.ROOT);
        if (BDFileStorageConstants.FILE_TYPE_THUMB.equals(fileType) && !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("썸네일은 이미지 파일만 등록할 수 있습니다.");
        }
        String storedFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String storedPath = BDFileStorageConstants.STORAGE_PATH_FORMATTER.format(LocalDate.now());
        Path directory = rootPath.resolve(storedPath).normalize();
        Files.createDirectories(directory);
        Path target = directory.resolve(storedFileName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target);
        }
        writtenFiles.add(target);

        BDNoticeFileSaveCommand fileCommand = new BDNoticeFileSaveCommand();
        fileCommand.setNoticeSeq(noticeSeq);
        fileCommand.setFileType(fileType);
        fileCommand.setOriginalFileName(originalFileName);
        fileCommand.setStoredFileName(storedFileName);
        fileCommand.setStoredPath(storedPath);
        fileCommand.setContentType(contentType);
        fileCommand.setFileSize(file.getSize());
        fileCommand.setSortOrder(sortOrder);
        fileCommand.setActorId(actorId);
        noticeMapper.insertNoticeFile(fileCommand);
    }

    private void validateThumbnailSize(MultipartFile thumbnailFile) {
        if (thumbnailFile != null && thumbnailFile.getSize() > maxThumbnailBytes) {
            throw new IllegalArgumentException("썸네일은 " + maxThumbnailSizeMb + "MB 이하만 업로드할 수 있습니다.");
        }
    }

    private void validateAttachmentSizes(List<MultipartFile> attachFiles) {
        for (MultipartFile file : attachFiles) {
            if (file.getSize() > maxAttachmentBytes) {
                throw new IllegalArgumentException("첨부 파일은 " + maxAttachmentSizeMb + "MB 이하만 업로드할 수 있습니다.");
            }
        }
    }

    private long megabytesToBytes(int megabytes) {
        if (megabytes <= 0) {
            throw new IllegalArgumentException("업로드 용량 제한은 1MB 이상이어야 합니다.");
        }
        return megabytes * 1024L * 1024L;
    }

    private String fileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("파일 확장자를 확인할 수 없습니다.");
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String sanitizeFileName(String fileName) {
        Path path = Path.of(fileName);
        Path name = path.getFileName();
        return name == null ? fileName : name.toString();
    }

    private void cleanupWrittenFiles(List<Path> writtenFiles) {
        for (Path path : writtenFiles) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // best effort cleanup
            }
        }
    }

    private Path resolveStoredFilePath(String storedPath, String storedFileName) {
        Path directory = storedPath == null || storedPath.isBlank()
                ? rootPath
                : rootPath.resolve(storedPath);
        Path resolved = directory.resolve(storedFileName).normalize();
        if (!resolved.startsWith(rootPath)) {
            return null;
        }
        return resolved;
    }

    private void schedulePhysicalCleanupAfterCommit(List<BDNoticeFileResponseDto> files) {
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

    private void cleanupStoredFiles(List<BDNoticeFileResponseDto> files) {
        for (BDNoticeFileResponseDto file : files) {
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

    private BDNoticeFileResponseDto withFileUrl(BDNoticeFileResponseDto file) {
        return rebuildFileUrl(file, fileBaseUrl + "?noticeFileSeq=" + file.noticeFileSeq());
    }

    private BDNoticeFileResponseDto withPublicFileUrl(BDNoticeFileResponseDto file) {
        return rebuildFileUrl(file, "/board/notice/file.do?noticeFileSeq=" + file.noticeFileSeq());
    }

    private BDNoticeFileResponseDto rebuildFileUrl(BDNoticeFileResponseDto file, String fileUrl) {
        return new BDNoticeFileResponseDto(
                file.noticeFileSeq(),
                file.noticeSeq(),
                file.fileType(),
                file.originalFileName(),
                file.storedFileName(),
                file.storedPath(),
                file.contentType(),
                file.fileSize(),
                file.sortOrder(),
                file.deleteFlg(),
                file.registeredAt(),
                file.registeredBy(),
                file.modifiedAt(),
                file.modifiedBy(),
                fileUrl
        );
    }

    private BDNoticePublicListItemResponseDto withPublicThumbnailUrl(BDNoticePublicListItemResponseDto notice) {
        String thumbnailUrl = notice.thumbnailFileSeq() == null
                ? null
                : "/board/notice/file.do?noticeFileSeq=" + notice.thumbnailFileSeq();
        return new BDNoticePublicListItemResponseDto(
                notice.noticeSeq(),
                notice.title(),
                notice.pinYn(),
                notice.viewCnt(),
                notice.publishDate(),
                notice.thumbnailFileSeq(),
                thumbnailUrl
        );
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
