# BD 파일저장 헬퍼 & firstText 중복 제거 리팩토링 Implementation Plan

> **For agentic workers:** 동작 보존(behavior-preserving) 리팩토링이다. 새 실패 테스트를 만들지 않는다. 각 태스크는 **기존 테스트 green 확인 → 리팩토링 → 다시 green 확인** 루프로 진행한다. 스텝은 `- [ ]` 체크박스로 추적한다.

**Goal:** 두 BD 게시판 서비스의 중복된 파일저장 헬퍼와 4개 서비스에 복붙된 `firstText`를 공통 헬퍼로 추출해 중복을 제거한다.

**Architecture:** (1) `service/bd/support`에 `BDFileStorageSupport`를 새로 만들어 디스크 저장·경로 해석·삭제 스케줄링을 모은다. 각 서비스는 자신의 `rootPath`로 인스턴스를 1개 생성해 위임한다(서비스마다 root-path가 다르므로 Spring 싱글톤 빈이 아니라 서비스별 인스턴스). 검증 규칙(허용 확장자·MIME)은 서비스마다 달라 그대로 둔다. (2) `common/util`에 `TextUtils.firstText`를 만들고 static import로 갈아끼운다.

**Tech Stack:** Java 17 records, Spring `@Service`, MyBatis, JUnit (`./gradlew test`).

**Plan 파일 위치:** 현재는 하네스 플랜 파일에 작성. 실행 시작 시 프로젝트 컨벤션대로 `docs/planned/2026-06-07-bd-refactor-dedup-plan.md`로 복사한다(진행중→`docs/planned`, 완료→`docs/clear`).

---

## Context

리팩토링 후보 조사에서 두 가지 중복을 확인했다.

- `BDNoticeService`(575줄)와 `BDPhotoBoardService`(559줄)가 파일 저장 관련 private 헬퍼 ~10개를 거의 동일하게 복붙 보유한다. `megabytesToBytes`·`fileExtension`·`sanitizeFileName`·`cleanupWrittenFiles`·`resolveStoredFilePath`는 **완전 동일**, `schedulePhysicalCleanupAfterCommit`·`cleanupStoredFiles`는 DTO 타입만 다르고 로직 동일, `normalizeFiles`와 `store*File`의 디스크 기록 블록도 동일하다.
- `firstText(String...)`는 `BDNoticeService`·`BDPhotoBoardService`·`BDAiNewsService`·`COAdminMenuService` **4개 파일에 바이트 단위로 동일**하게 존재한다(호출 40곳). 팀 정책이 rule-of-three(`docs/context-notes.md` 2026-06-07 P9 노트)인데 4곳이라 임계를 넘는다.

기대 결과는 동작 변화 없이 서비스당 ~80줄 감소, 단일 출처(single source of truth) 확보다. 기존 회귀 테스트(`BDNoticeServiceTest`, `BDPhotoBoardServiceTest` 19개, `BDAiNewsServiceTest`, `COAdminMenuServiceTest`)가 안전망이다.

## File Structure

- **신규** `src/main/java/com/reven/project/service/bd/support/BDFileStorageSupport.java` — bd 파일의 디스크 저장·경로 해석·커밋 후 삭제. `rootPath`를 보유하는 인스턴스 + 순수 static 헬퍼 + 두 record(`StoredFile`, `StoredFileRef`).
- **신규** `src/main/java/com/reven/project/common/util/TextUtils.java` — `firstText` static 유틸.
- **수정** `service/bd/BDNoticeService.java`, `service/bd/BDPhotoBoardService.java` — 헬퍼로 위임, 중복 private 메서드 제거.
- **수정** `service/bd/BDAiNewsService.java`, `service/co/COAdminMenuService.java` — `firstText` static import로 교체.
- 기존 `service/bd/support/BDFileStorageConstants.java`는 그대로 사용(`STORAGE_PATH_FORMATTER`).

---

## Task R1: BDFileStorageSupport 추출 (무거움)

**Files:**
- Create: `src/main/java/com/reven/project/service/bd/support/BDFileStorageSupport.java`
- Modify: `src/main/java/com/reven/project/service/bd/BDNoticeService.java`
- Modify: `src/main/java/com/reven/project/service/bd/BDPhotoBoardService.java`
- Test(기존): `src/test/java/com/reven/project/service/bd/BDNoticeServiceTest.java`, `BDPhotoBoardServiceTest.java`

- [ ] **Step 1: 기준선 green 확인**

Run: `./gradlew test --tests "*BDNoticeServiceTest" --tests "*BDPhotoBoardServiceTest"`
Expected: PASS (리팩토링 전 안전망 확인)

- [ ] **Step 2: `BDFileStorageSupport` 생성**

`src/main/java/com/reven/project/service/bd/support/BDFileStorageSupport.java`:

```java
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
```

- [ ] **Step 3: `BDNoticeService` 위임으로 전환**

필드/생성자: `private final Path rootPath;` 삭제, `private final BDFileStorageSupport fileStorage;` 추가. 생성자 본문에서
`this.rootPath = Paths.get(rootPath)...` → `this.fileStorage = new BDFileStorageSupport(rootPath);`,
`megabytesToBytes(...)` 호출 2곳 → `BDFileStorageSupport.megabytesToBytes(...)`.

`storeNoticeFile`를 디스크 기록만 위임하도록 교체(검증은 유지):

```java
    private void storeNoticeFile(Long noticeSeq, MultipartFile file, String fileType, int sortOrder, String actorId, List<Path> writtenFiles) throws IOException {
        String originalFileName = BDFileStorageSupport.sanitizeFileName(firstText(file.getOriginalFilename(), "notice"));
        String extension = BDFileStorageSupport.fileExtension(originalFileName);
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
        BDFileStorageSupport.StoredFile stored = fileStorage.writeToDisk(file, extension, writtenFiles);

        BDNoticeFileSaveCommand fileCommand = new BDNoticeFileSaveCommand();
        fileCommand.setNoticeSeq(noticeSeq);
        fileCommand.setFileType(fileType);
        fileCommand.setOriginalFileName(originalFileName);
        fileCommand.setStoredFileName(stored.storedFileName());
        fileCommand.setStoredPath(stored.storedPath());
        fileCommand.setContentType(contentType);
        fileCommand.setFileSize(file.getSize());
        fileCommand.setSortOrder(sortOrder);
        fileCommand.setActorId(actorId);
        noticeMapper.insertNoticeFile(fileCommand);
    }
```

DTO→ref 매핑 헬퍼 추가(삭제 스케줄용):

```java
    private List<BDFileStorageSupport.StoredFileRef> toFileRefs(List<BDNoticeFileResponseDto> files) {
        return files.stream()
                .map(file -> new BDFileStorageSupport.StoredFileRef(file.storedPath(), file.storedFileName()))
                .toList();
    }
```

호출처 치환(기능 동일):
- `resolveStoredFilePath(file.storedPath(), file.storedFileName())` → `fileStorage.resolveStoredFilePath(...)` (2곳: `resolveNoticeFilePath`, `resolvePublicNoticeFilePath`)
- `normalizeFiles(attachFiles)` → `BDFileStorageSupport.normalizeFiles(attachFiles)`
- `cleanupWrittenFiles(writtenFiles)` → `BDFileStorageSupport.cleanupWrittenFiles(writtenFiles)` (2곳)
- `schedulePhysicalCleanupAfterCommit(<dtoList>)` → `fileStorage.scheduleCleanupAfterCommit(toFileRefs(<dtoList>))` (3곳: `saveNotice` removed, `replaceThumbnail`, `deleteNotice`)

제거할 private 메서드: `megabytesToBytes`, `fileExtension`, `sanitizeFileName`, `normalizeFiles`, `cleanupWrittenFiles`, `resolveStoredFilePath`, `schedulePhysicalCleanupAfterCommit`, `cleanupStoredFiles`. 컴파일러가 지적하는 미사용 import 제거(`InputStream`, `Files`, `Paths`, `UUID`, `TransactionSynchronization`, `TransactionSynchronizationManager`). `Locale`·`LocalDate`·`Set`·`Path`는 남는 사용처가 있어 유지.

- [ ] **Step 4: `BDPhotoBoardService` 위임으로 전환**

R1 Step 3과 동일 패턴. 필드 `rootPath` → `fileStorage`, 생성자 `megabytesToBytes` 2곳 → static 호출. `storePhotoFile`를 교체:

```java
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
```

`validateNewFileSizes`는 `fileExtension`/`sanitizeFileName`를 쓰므로 static 호출로 바꾼다. `toFileRefs(List<BDPhotoBoardFileResponseDto>)` 매핑 헬퍼 추가, `schedulePhysicalCleanupAfterCommit` 호출 2곳(`savePhotoBoard`, `deletePhotoBoard`)을 `fileStorage.scheduleCleanupAfterCommit(toFileRefs(...))`로 치환. `normalizeFiles`·`cleanupWrittenFiles`·`resolveStoredFilePath` 호출 치환. 동일 private 메서드 8개 제거 + 미사용 import 제거. `MAX_UPLOAD_MESSAGE`/`MIN_UPLOAD_MESSAGE`와 `isAllowedContentType`·`validateFileSize`는 포토 전용이라 유지.

- [ ] **Step 5: 컴파일 + 회귀 확인**

Run: `./gradlew test --tests "*BDNoticeServiceTest" --tests "*BDPhotoBoardServiceTest"`
Expected: PASS (Step 1과 동일 결과). 실패 시 위임 치환 누락·import 문제를 먼저 읽고 고친다.

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/reven/project/service/bd/support/BDFileStorageSupport.java \
        src/main/java/com/reven/project/service/bd/BDNoticeService.java \
        src/main/java/com/reven/project/service/bd/BDPhotoBoardService.java
git commit -m "refactor(bd): 파일저장 헬퍼 BDFileStorageSupport로 공통화"
```

---

## Task R2: firstText 공용 헬퍼 통합 (중간)

**Files:**
- Create: `src/main/java/com/reven/project/common/util/TextUtils.java`
- Modify: `BDNoticeService.java`, `BDPhotoBoardService.java`, `BDAiNewsService.java`(`service/bd/`), `COAdminMenuService.java`(`service/co/`)

- [ ] **Step 1: `TextUtils` 생성**

`src/main/java/com/reven/project/common/util/TextUtils.java`:

```java
// 여러 문자열 중 비어 있지 않은 첫 값을 고르는 문자열 유틸
package com.reven.project.common.util;

public final class TextUtils {

    private TextUtils() {
    }

    public static String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
```

- [ ] **Step 2: 4개 서비스에서 private firstText 제거 + static import 추가**

각 파일에서 private `firstText(String...)` 메서드(7줄)를 삭제하고 import 영역에 추가:

```java
import static com.reven.project.common.util.TextUtils.firstText;
```

static import이므로 호출처 40곳(`firstText(...)`)은 **수정하지 않는다**. R1을 먼저 했으면 `BDNoticeService`/`BDPhotoBoardService`에는 이미 R1 변경이 반영돼 있고 여기서는 firstText만 손댄다.

- [ ] **Step 3: 회귀 확인**

Run: `./gradlew test`
Expected: PASS (전체 그린)

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/reven/project/common/util/TextUtils.java \
        src/main/java/com/reven/project/service/bd/BDNoticeService.java \
        src/main/java/com/reven/project/service/bd/BDPhotoBoardService.java \
        src/main/java/com/reven/project/service/bd/BDAiNewsService.java \
        src/main/java/com/reven/project/service/co/COAdminMenuService.java
git commit -m "refactor: 중복 firstText를 TextUtils로 통합"
```

---

## Task R3: SASurveyDto 분리 검토 (낮음 — 결정 태스크, 기본 보류)

`service/sa/dto/SASurveyDto.java`(531줄)는 nested static class ~20개를 담은 DTO 컨테이너다. 분리는 다음 이유로 **기본 보류**를 권장한다.

- 단순 DTO 묶음이라 분리해도 로직 이득이 없고, import·참조 경로 변경으로 churn만 크다.
- 한 도메인(설문)의 요청/응답 DTO가 한 파일에 모여 있어 탐색은 오히려 쉽다.

- [ ] **Step 1: 결정 기록** — 분리하지 않기로 하면 `docs/context-notes.md`에 사유 1줄 추가. 사용자가 분리를 원하면 별도 플랜으로 분리(파일당 1 DTO, 동작 변화 없음, `./gradlew test`로 확인).

---

## R4 참고(액션 없음)

`BDNoticeService.normalizedAdminSearch`(public)는 dead가 아니다. `BDNoticeAdminController.java:74`가 호출하고 `BDNoticeAdminControllerTest`가 mock한다. public(컨트롤러 노출) ↔ private `normalizeAdminSearch`(내부 공유 impl) 쌍은 의도적이다. 정리 가치가 낮아 이번 범위에서 제외한다.

---

## Verification

- 태스크별 focused 테스트는 각 Step에 명시.
- 최종 전체 회귀: `./gradlew test` — 전부 PASS여야 한다.
- 동작 보존 확인 포인트: 파일 업로드 저장 경로(`yyyy/MM/dd`)·UUID 파일명·허용 확장자/MIME 거부·트랜잭션 커밋 후 물리 삭제·경로 탈출 방지(`resolveStoredFilePath`의 `startsWith(rootPath)` 가드)가 리팩토링 전후 동일해야 한다. 이는 기존 `BDPhotoBoardServiceTest`(19개)·`BDNoticeServiceTest`가 커버한다.
- 추가 테스트 작성 불필요(behavior-preserving, 기존 커버리지로 충분).

## Self-Review 메모

- 타입 일관성: `StoredFile(storedFileName, storedPath)`와 `StoredFileRef(storedPath, storedFileName)`의 인자 순서가 다르다(StoredFile은 디스크 기록 결과, StoredFileRef는 삭제 입력). 호출 시 record 컴포넌트명으로 접근하므로 혼동 없음. 구현 시 순서 주의.
- `firstText`는 R1 단계의 `storeNoticeFile`/`storePhotoFile`에도 등장한다. R1에서는 기존 private `firstText`를 그대로 쓰고, R2에서 static import로 일괄 전환한다(순서 의존성 명시).
- 누락 점검: 파일저장 5개 동일 + 2개 DTO형 + `normalizeFiles` + 디스크 기록 블록 = R1에서 전부 커버. `firstText` 4파일 = R2에서 커버.

## Execution Handoff

플랜 승인 후 실행 방식 2가지.

1. **Subagent-Driven (추천)** — 태스크마다 새 subagent 디스패치, 사이에 리뷰. (`superpowers:subagent-driven-development`)
2. **Inline 실행** — 이 세션에서 체크포인트 두고 순차 실행. (`superpowers:executing-plans`)

R1 → R2 순서(무거운 것 먼저). R3는 결정만, R4는 액션 없음.
