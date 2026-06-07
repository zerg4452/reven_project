package com.reven.project.service.bd;

import com.reven.project.service.bd.dto.BDPhotoBoardDetailResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardFileResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardFileSaveCommand;
import com.reven.project.service.bd.dto.BDPhotoBoardListItemResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicListItemResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicPageResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicSearchRequestDto;
import com.reven.project.service.bd.dto.BDPhotoBoardSaveCommand;
import com.reven.project.service.bd.dto.BDPhotoBoardSaveRequestDto;
import com.reven.project.service.bd.mapper.BDPhotoBoardMapper;
import com.reven.project.service.bd.support.BDFileStorageConstants;
import com.reven.project.service.bd.support.BDFileStorageSupport;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import static com.reven.project.common.util.TextUtils.firstText;

@Service
public class BDPhotoBoardService {

    private static final String MAX_UPLOAD_MESSAGE = "최대 업로드 갯수는 5개입니다.";
    private static final String MIN_UPLOAD_MESSAGE = "첨부 파일을 최소 1개 이상 업로드해 주세요.";
    private final BDPhotoBoardMapper photoBoardMapper;
    private final BDFileStorageSupport fileStorage;
    private final String fileBaseUrl;
    private final int maxFiles;
    private final int maxImageSizeMb;
    private final int maxVideoSizeMb;
    private final long maxImageBytes;
    private final long maxVideoBytes;

    public BDPhotoBoardService(
            BDPhotoBoardMapper photoBoardMapper,
            @Value("${reven.upload.photo.root-path}") String rootPath,
            @Value("${reven.upload.photo.base-url}") String fileBaseUrl,
            @Value("${reven.upload.photo.max-files:5}") int maxFiles,
            @Value("${reven.upload.photo.max-image-size-mb:20}") int maxImageSizeMb,
            @Value("${reven.upload.photo.max-video-size-mb:50}") int maxVideoSizeMb
    ) {
        this.photoBoardMapper = photoBoardMapper;
        this.fileStorage = new BDFileStorageSupport(rootPath);
        this.fileBaseUrl = fileBaseUrl;
        this.maxFiles = maxFiles;
        this.maxImageSizeMb = maxImageSizeMb;
        this.maxVideoSizeMb = maxVideoSizeMb;
        this.maxImageBytes = BDFileStorageSupport.megabytesToBytes(maxImageSizeMb);
        this.maxVideoBytes = BDFileStorageSupport.megabytesToBytes(maxVideoSizeMb);
    }

    /**
     * 사진 게시판 목록을 조회한다.
     */
    public List<BDPhotoBoardListItemResponseDto> findPhotoBoards() {
        return photoBoardMapper.selectPhotoBoardList();
    }

    /**
     * 사진 게시판 단건을 조회한다.
     */
    public BDPhotoBoardDetailResponseDto findPhotoBoard(Long photoSeq) {
        return photoBoardMapper.selectPhotoBoardDetail(photoSeq);
    }

    /**
     * 사진 게시판의 첨부 파일 목록을 조회한다.
     */
    public List<BDPhotoBoardFileResponseDto> findPhotoBoardFiles(Long photoSeq) {
        return photoBoardMapper.selectPhotoBoardFiles(photoSeq).stream()
                .map(this::withFileUrl)
                .toList();
    }

    /**
     * 사진 게시판 첨부 파일 단건을 조회한다.
     */
    public BDPhotoBoardFileResponseDto findPhotoBoardFile(Long photoFileSeq) {
        BDPhotoBoardFileResponseDto file = photoBoardMapper.selectPhotoBoardFile(photoFileSeq);
        return file == null ? null : withFileUrl(file);
    }

    /**
     * 사진 게시판 첨부 파일의 실제 저장 경로를 구한다.
     */
    public Path resolvePhotoBoardFilePath(Long photoFileSeq) {
        BDPhotoBoardFileResponseDto file = photoBoardMapper.selectPhotoBoardFile(photoFileSeq);
        if (file == null) {
            return null;
        }
        return fileStorage.resolveStoredFilePath(file.storedPath(), file.storedFileName());
    }

    /**
     * 사용자 포토 게시판 목록을 조회한다.
     */
    public BDPhotoBoardPublicPageResponseDto searchPublicPhotoBoards(BDPhotoBoardPublicSearchRequestDto search) {
        BDPhotoBoardPublicSearchRequestDto normalized = search == null
                ? new BDPhotoBoardPublicSearchRequestDto("", false, false, 1, 9)
                : search.normalized();
        int totalCount = photoBoardMapper.selectPublicPhotoBoardCount(normalized);
        int totalPages = totalCount == 0 ? 0 : (int) Math.ceil((double) totalCount / normalized.size());
        if (totalPages > 0 && normalized.page() > totalPages) {
            normalized = new BDPhotoBoardPublicSearchRequestDto(
                    normalized.keyword(),
                    normalized.imageOnly(),
                    normalized.videoOnly(),
                    totalPages,
                    normalized.size()
            );
        }
        List<BDPhotoBoardPublicListItemResponseDto> photos = photoBoardMapper.selectPublicPhotoBoardList(normalized)
                .stream()
                .map(this::withThumbnailUrl)
                .toList();
        return new BDPhotoBoardPublicPageResponseDto(normalized, photos, totalCount, totalPages);
    }

    /**
     * 사진 게시판 조회수를 1 증가시킨다.
     */
    @Transactional
    public void increaseViewCount(Long photoSeq) {
        if (photoSeq != null) {
            photoBoardMapper.increaseViewCount(photoSeq);
        }
    }

    /**
     * 사용자 포토 게시판 단건을 조회한다.
     */
    public BDPhotoBoardDetailResponseDto findPublicPhotoBoard(Long photoSeq) {
        if (photoSeq == null) {
            return null;
        }
        return photoBoardMapper.selectPublicPhotoBoardDetail(photoSeq);
    }

    /**
     * 사용자 포토 게시판의 첨부 파일 목록을 조회한다.
     */
    public List<BDPhotoBoardFileResponseDto> findPublicPhotoBoardFiles(Long photoSeq) {
        if (photoSeq == null) {
            return List.of();
        }
        BDPhotoBoardDetailResponseDto photo = photoBoardMapper.selectPublicPhotoBoardDetail(photoSeq);
        if (photo == null) {
            return List.of();
        }
        return photoBoardMapper.selectPhotoBoardFiles(photoSeq).stream()
                .map(this::withPublicFileUrl)
                .toList();
    }

    /**
     * 사용자 포토 게시판 첨부 파일 단건을 조회한다.
     */
    public BDPhotoBoardFileResponseDto findPublicPhotoBoardFile(Long photoFileSeq) {
        if (photoFileSeq == null) {
            return null;
        }
        BDPhotoBoardFileResponseDto file = photoBoardMapper.selectPublicPhotoBoardFile(photoFileSeq);
        return file == null ? null : withPublicFileUrl(file);
    }

    /**
     * 사용자 포토 게시판 첨부 파일의 실제 저장 경로를 구한다.
     */
    public Path resolvePublicPhotoBoardFilePath(Long photoFileSeq) {
        if (photoFileSeq == null) {
            return null;
        }
        BDPhotoBoardFileResponseDto file = photoBoardMapper.selectPublicPhotoBoardFile(photoFileSeq);
        if (file == null) {
            return null;
        }
        return fileStorage.resolveStoredFilePath(file.storedPath(), file.storedFileName());
    }

    /**
     * 사진 게시판을 등록하거나 수정한다.
     */
    @Transactional
    public Long savePhotoBoard(BDPhotoBoardSaveRequestDto requestDto, List<MultipartFile> uploadedFiles) {
        return savePhotoBoard(requestDto, uploadedFiles, List.of());
    }

    /**
     * 사진 게시판을 등록하거나 수정한다.
     */
    @Transactional
    public Long savePhotoBoard(
            BDPhotoBoardSaveRequestDto requestDto,
            List<MultipartFile> uploadedFiles,
            List<Long> keepPhotoFileSeqs
    ) {
        BDPhotoBoardSaveRequestDto normalized = normalize(requestDto);
        List<MultipartFile> newFiles = BDFileStorageSupport.normalizeFiles(uploadedFiles);
        validateNewFileSizes(newFiles);
        BDPhotoBoardDetailResponseDto existingPost = normalized.photoSeq() == null
                ? null
                : photoBoardMapper.selectPhotoBoardDetail(normalized.photoSeq());
        if (normalized.photoSeq() != null && existingPost == null) {
            throw new IllegalArgumentException("수정할 사진 게시글을 찾을 수 없습니다.");
        }

        List<BDPhotoBoardFileResponseDto> existingFiles = normalized.photoSeq() == null
                ? List.of()
                : photoBoardMapper.selectPhotoBoardFiles(normalized.photoSeq());
        Set<Long> keepSeqs = normalized.photoSeq() == null
                ? Set.of()
                : normalizeKeepSeqs(keepPhotoFileSeqs, normalized.photoSeq(), existingFiles);

        List<BDPhotoBoardFileResponseDto> retainedFiles = existingFiles.stream()
                .filter(file -> keepSeqs.contains(file.photoFileSeq()))
                .toList();
        List<BDPhotoBoardFileResponseDto> removedFiles = existingFiles.stream()
                .filter(file -> !keepSeqs.contains(file.photoFileSeq()))
                .toList();

        validateAttachmentCounts(retainedFiles.size(), newFiles.size());

        BDPhotoBoardSaveCommand saveCommand = toSaveCommand(normalized);
        List<Path> writtenFiles = new ArrayList<>();

        try {
            if (normalized.photoSeq() == null) {
                photoBoardMapper.insertPhotoBoard(saveCommand);
                normalized = new BDPhotoBoardSaveRequestDto(saveCommand.getPhotoSeq(), normalized.title(), normalized.publishYn(), normalized.actorId());
            } else {
                for (BDPhotoBoardFileResponseDto removedFile : removedFiles) {
                    photoBoardMapper.deletePhotoBoardFile(removedFile.photoFileSeq(), normalized.actorId());
                }
                if (!removedFiles.isEmpty()) {
                    fileStorage.scheduleCleanupAfterCommit(toFileRefs(removedFiles));
                }
                photoBoardMapper.updatePhotoBoard(saveCommand);
            }

            int sortOrder = retainedFiles.stream()
                    .mapToInt(file -> file.sortOrder() == null ? 0 : file.sortOrder())
                    .max()
                    .orElse(0);
            for (MultipartFile file : newFiles) {
                sortOrder++;
                storePhotoFile(normalized.photoSeq(), file, sortOrder, normalized.actorId(), writtenFiles);
            }
            return normalized.photoSeq();
        } catch (RuntimeException exception) {
            BDFileStorageSupport.cleanupWrittenFiles(writtenFiles);
            throw exception;
        } catch (IOException exception) {
            BDFileStorageSupport.cleanupWrittenFiles(writtenFiles);
            throw new IllegalStateException("사진 파일을 저장할 수 없습니다.", exception);
        }
    }

    /**
     * 사진 게시판을 soft delete 처리한다.
     */
    @Transactional
    public void deletePhotoBoard(Long photoSeq, String actorId) {
        BDPhotoBoardDetailResponseDto existing = photoBoardMapper.selectPhotoBoardDetail(photoSeq);
        if (existing == null) {
            throw new IllegalArgumentException("삭제할 사진 게시글을 찾을 수 없습니다.");
        }
        List<BDPhotoBoardFileResponseDto> files = photoBoardMapper.selectPhotoBoardFiles(photoSeq);
        photoBoardMapper.deletePhotoBoardFiles(photoSeq, firstText(actorId, "system"));
        photoBoardMapper.deletePhotoBoard(photoSeq, firstText(actorId, "system"));
        fileStorage.scheduleCleanupAfterCommit(toFileRefs(files));
    }

    private BDPhotoBoardSaveRequestDto normalize(BDPhotoBoardSaveRequestDto requestDto) {
        String title = firstText(requestDto.title()).trim();
        if (title.isBlank()) {
            throw new IllegalArgumentException("제목을 입력해 주세요.");
        }
        String publishYn = "N".equalsIgnoreCase(requestDto.publishYn()) ? "N" : "Y";
        return new BDPhotoBoardSaveRequestDto(
                requestDto.photoSeq(),
                title,
                publishYn,
                firstText(requestDto.actorId(), "system")
        );
    }

    private void validateAttachmentCounts(int retainedCount, int newCount) {
        int total = retainedCount + newCount;
        if (total > maxFiles) {
            throw new IllegalArgumentException(MAX_UPLOAD_MESSAGE);
        }
        if (total < 1) {
            throw new IllegalArgumentException(MIN_UPLOAD_MESSAGE);
        }
    }

    private Set<Long> normalizeKeepSeqs(
            List<Long> keepPhotoFileSeqs,
            Long photoSeq,
            List<BDPhotoBoardFileResponseDto> existingFiles
    ) {
        if (keepPhotoFileSeqs == null || keepPhotoFileSeqs.isEmpty()) {
            return Set.of();
        }
        Set<Long> validSeqs = new HashSet<>();
        for (BDPhotoBoardFileResponseDto file : existingFiles) {
            if (photoSeq.equals(file.photoSeq())) {
                validSeqs.add(file.photoFileSeq());
            }
        }
        Set<Long> keepSeqs = new HashSet<>();
        for (Long keepSeq : keepPhotoFileSeqs) {
            if (keepSeq != null && validSeqs.contains(keepSeq)) {
                keepSeqs.add(keepSeq);
            }
        }
        return keepSeqs;
    }

    private BDPhotoBoardSaveCommand toSaveCommand(BDPhotoBoardSaveRequestDto requestDto) {
        BDPhotoBoardSaveCommand command = new BDPhotoBoardSaveCommand();
        command.setPhotoSeq(requestDto.photoSeq());
        command.setTitle(requestDto.title());
        command.setPublishYn(requestDto.publishYn());
        command.setActorId(requestDto.actorId());
        return command;
    }

    private void storePhotoFile(Long photoSeq, MultipartFile file, int sortOrder, String actorId, List<Path> writtenFiles) throws IOException {
        String originalFileName = BDFileStorageSupport.sanitizeFileName(firstText(file.getOriginalFilename(), "photo"));
        String extension = BDFileStorageSupport.fileExtension(originalFileName);
        if (!BDFileStorageConstants.PHOTO_BOARD_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
        }
        String contentType = firstText(file.getContentType()).toLowerCase(Locale.ROOT);
        if (!isAllowedContentType(contentType, extension)) {
            throw new IllegalArgumentException("허용되지 않는 MIME 타입입니다.");
        }
        BDFileStorageSupport.StoredFile stored = fileStorage.writeToDisk(file, extension, writtenFiles);

        BDPhotoBoardFileSaveCommand fileCommand = new BDPhotoBoardFileSaveCommand();
        fileCommand.setPhotoSeq(photoSeq);
        fileCommand.setOriginalFileName(originalFileName);
        fileCommand.setStoredFileName(stored.storedFileName());
        fileCommand.setStoredPath(stored.storedPath());
        fileCommand.setContentType(contentType);
        fileCommand.setFileSize(file.getSize());
        fileCommand.setSortOrder(sortOrder);
        fileCommand.setActorId(actorId);
        photoBoardMapper.insertPhotoBoardFile(fileCommand);
    }

    private void validateNewFileSizes(List<MultipartFile> newFiles) {
        for (MultipartFile file : newFiles) {
            String originalFileName = BDFileStorageSupport.sanitizeFileName(firstText(file.getOriginalFilename(), "photo"));
            String extension = BDFileStorageSupport.fileExtension(originalFileName);
            validateFileSize(extension, file.getSize());
        }
    }

    private void validateFileSize(String extension, long fileSize) {
        if (fileSize <= 0) {
            throw new IllegalArgumentException(MIN_UPLOAD_MESSAGE);
        }
        if ("mp4".equals(extension)) {
            if (fileSize > maxVideoBytes) {
                throw new IllegalArgumentException(
                        "동영상 파일은 " + maxVideoSizeMb + "MB 이하만 업로드할 수 있습니다."
                );
            }
            return;
        }
        if (fileSize > maxImageBytes) {
            throw new IllegalArgumentException(
                    "이미지 파일은 " + maxImageSizeMb + "MB 이하만 업로드할 수 있습니다."
            );
        }
    }

    private boolean isAllowedContentType(String contentType, String extension) {
        if (contentType == null || contentType.isBlank()) {
            return false;
        }
        if (extension.equals("mp4")) {
            return "video/mp4".equals(contentType);
        }
        return contentType.startsWith("image/");
    }

    private List<BDFileStorageSupport.StoredFileRef> toFileRefs(List<BDPhotoBoardFileResponseDto> files) {
        return files.stream()
                .map(file -> new BDFileStorageSupport.StoredFileRef(file.storedPath(), file.storedFileName()))
                .toList();
    }

    private BDPhotoBoardFileResponseDto withFileUrl(BDPhotoBoardFileResponseDto file) {
        return new BDPhotoBoardFileResponseDto(
                file.photoFileSeq(),
                file.photoSeq(),
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
                buildFileUrl(file.photoFileSeq())
        );
    }

    private String buildFileUrl(Long photoFileSeq) {
        return fileBaseUrl + "?photoFileSeq=" + photoFileSeq;
    }

    private BDPhotoBoardPublicListItemResponseDto withThumbnailUrl(BDPhotoBoardPublicListItemResponseDto photo) {
        return new BDPhotoBoardPublicListItemResponseDto(
                photo.photoSeq(),
                photo.title(),
                photo.registeredDate(),
                photo.thumbnailFileSeq(),
                photo.thumbnailContentType(),
                buildPublicFileUrl(photo.thumbnailFileSeq()),
                photo.hasImage(),
                photo.hasVideo()
        );
    }

    private BDPhotoBoardFileResponseDto withPublicFileUrl(BDPhotoBoardFileResponseDto file) {
        return new BDPhotoBoardFileResponseDto(
                file.photoFileSeq(),
                file.photoSeq(),
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
                buildPublicFileUrl(file.photoFileSeq())
        );
    }

    private String buildPublicFileUrl(Long photoFileSeq) {
        return photoFileSeq == null ? null : "/board/photo/file.do?photoFileSeq=" + photoFileSeq;
    }
}
