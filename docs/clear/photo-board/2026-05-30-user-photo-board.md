# User Photo Board Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the public photo board under the existing public board menu with searchable 9-item thumbnail cards, a media detail page, and protected public file access.

**Architecture:** Reuse the existing admin photo board tables and upload metadata, and add public-only service and mapper methods that filter to published, non-deleted posts. Keep admin write behavior unchanged, derive image/video flags from `bd_photo_board_file_dtl.content_type`, and expose public media through a separate `/board/photo/file.do` endpoint.

**Tech Stack:** Spring Boot MVC, Thymeleaf, MyBatis, Java records, JUnit 5, Mockito, MockMvc, static CSS and JavaScript.

---

## File Structure

- Create `src/main/java/com/reven/project/client/bd/BDPhotoBoardPublicController.java`: public list, detail, and file endpoints.
- Create `src/main/java/com/reven/project/service/bd/dto/BDPhotoBoardPublicSearchRequestDto.java`: normalized public search and paging request.
- Create `src/main/java/com/reven/project/service/bd/dto/BDPhotoBoardPublicListItemResponseDto.java`: public card row including representative media and flags.
- Create `src/main/java/com/reven/project/service/bd/dto/BDPhotoBoardPublicPageResponseDto.java`: list page wrapper for search, rows, and pagination.
- Modify `src/main/java/com/reven/project/service/bd/dto/BDPhotoBoardFileResponseDto.java`: add media helper methods used by templates.
- Modify `src/main/java/com/reven/project/service/bd/BDPhotoBoardService.java`: add public list, detail, files, public file lookup, and public path resolution methods.
- Modify `src/main/java/com/reven/project/service/bd/mapper/BDPhotoBoardMapper.java`: add public query method declarations.
- Modify `src/main/resources/mapper/bd/BDPhotoBoardMapper.xml`: add public SQL queries using `content_type` for image/video flags.
- Modify `src/main/resources/templates/fragments/layout.html`: add `포토 게시판` to public GNB under `게시판`.
- Create `src/main/resources/templates/client/photo/list.html`: public card list and search form.
- Create `src/main/resources/templates/client/photo/detail.html`: public detail page and media modal.
- Create `src/main/resources/static/common/js/photo-public.js`: public image/video modal behavior.
- Modify `src/main/resources/static/common/css/app.css`: public photo card grid, search flags, detail grid, video modal styles.
- Modify `src/test/java/com/reven/project/service/bd/BDPhotoBoardServiceTest.java`: service tests for public search, OR media flags, and public file restrictions.
- Modify `src/test/java/com/reven/project/client/COMainControllerTest.java`: add public photo controller route tests.
- Modify `docs/worklog.md`: note public navigation, layout, data flow, and behavior changes.
- Modify `docs/checklist.md`: tick implementation plan and later implementation steps as work completes.

---

### Task 1: Public DTOs And Template Helpers

**Files:**
- Create: `src/main/java/com/reven/project/service/bd/dto/BDPhotoBoardPublicSearchRequestDto.java`
- Create: `src/main/java/com/reven/project/service/bd/dto/BDPhotoBoardPublicListItemResponseDto.java`
- Create: `src/main/java/com/reven/project/service/bd/dto/BDPhotoBoardPublicPageResponseDto.java`
- Modify: `src/main/java/com/reven/project/service/bd/dto/BDPhotoBoardFileResponseDto.java`

- [ ] **Step 1: Create public search request DTO**

Create `src/main/java/com/reven/project/service/bd/dto/BDPhotoBoardPublicSearchRequestDto.java` with this content.

```java
// 사용자 포토 게시판 검색 요청 DTO
package com.reven.project.service.bd.dto;

public record BDPhotoBoardPublicSearchRequestDto(
        String keyword,
        boolean imageOnly,
        boolean videoOnly,
        int page,
        int size
) {
    public BDPhotoBoardPublicSearchRequestDto normalized() {
        int normalizedPage = page < 1 ? 1 : page;
        int normalizedSize = size < 1 ? 9 : size;
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        return new BDPhotoBoardPublicSearchRequestDto(
                normalizedKeyword,
                imageOnly,
                videoOnly,
                normalizedPage,
                normalizedSize
        );
    }

    public int offset() {
        return (page - 1) * size;
    }

    public boolean hasMediaFilter() {
        return imageOnly || videoOnly;
    }
}
```

- [ ] **Step 2: Create public list item DTO**

Create `src/main/java/com/reven/project/service/bd/dto/BDPhotoBoardPublicListItemResponseDto.java` with this content.

```java
// 사용자 포토 게시판 카드 응답 DTO
package com.reven.project.service.bd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record BDPhotoBoardPublicListItemResponseDto(

        @Schema(description = "포토 게시판 일련번호", example = "1")
        Long photoSeq,

        @Schema(description = "제목", example = "봄 사진 전시")
        String title,

        @Schema(description = "등록 일자", example = "2026-05-30")
        LocalDate registeredDate,

        @Schema(description = "대표 첨부 일련번호", example = "10")
        Long thumbnailFileSeq,

        @Schema(description = "대표 첨부 MIME 타입", example = "image/jpeg")
        String thumbnailContentType,

        @Schema(description = "대표 첨부 URL", example = "/board/photo/file.do?photoFileSeq=10")
        String thumbnailFileUrl,

        @Schema(description = "이미지 포함 여부", example = "true")
        Boolean hasImage,

        @Schema(description = "동영상 포함 여부", example = "false")
        Boolean hasVideo
) {
    public boolean thumbnailImage() {
        return thumbnailContentType != null && thumbnailContentType.startsWith("image/");
    }

    public boolean thumbnailVideo() {
        return thumbnailContentType != null && thumbnailContentType.startsWith("video/");
    }
}
```

- [ ] **Step 3: Create public page DTO**

Create `src/main/java/com/reven/project/service/bd/dto/BDPhotoBoardPublicPageResponseDto.java` with this content.

```java
// 사용자 포토 게시판 목록 페이지 응답 DTO
package com.reven.project.service.bd.dto;

import java.util.List;

public record BDPhotoBoardPublicPageResponseDto(
        BDPhotoBoardPublicSearchRequestDto search,
        List<BDPhotoBoardPublicListItemResponseDto> photos,
        int totalCount,
        int totalPages
) {
    public boolean hasPrevious() {
        return search.page() > 1;
    }

    public boolean hasNext() {
        return search.page() < totalPages;
    }

    public int previousPage() {
        return Math.max(1, search.page() - 1);
    }

    public int nextPage() {
        return Math.min(totalPages, search.page() + 1);
    }
}
```

- [ ] **Step 4: Add media helper methods to file DTO**

Modify `src/main/java/com/reven/project/service/bd/dto/BDPhotoBoardFileResponseDto.java` inside the record body.

```java
    public boolean image() {
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean video() {
        return contentType != null && contentType.startsWith("video/");
    }
```

- [ ] **Step 5: Run DTO compile check**

Run this command.

```bash
./gradlew test --tests 'com.reven.project.service.bd.BDPhotoBoardServiceTest'
```

Expected result at this point may be compile failure only if a syntax error exists. Fix syntax before moving to Task 2.

---

### Task 2: Public Mapper Queries

**Files:**
- Modify: `src/main/java/com/reven/project/service/bd/mapper/BDPhotoBoardMapper.java`
- Modify: `src/main/resources/mapper/bd/BDPhotoBoardMapper.xml`

- [ ] **Step 1: Add mapper method declarations**

Add these imports and methods to `BDPhotoBoardMapper`.

```java
import com.reven.project.service.bd.dto.BDPhotoBoardPublicListItemResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicSearchRequestDto;
```

```java
    /** 사용자 포토 게시판 목록을 조회한다. */
    List<BDPhotoBoardPublicListItemResponseDto> selectPublicPhotoBoardList(BDPhotoBoardPublicSearchRequestDto search);

    /** 사용자 포토 게시판 목록 총 건수를 조회한다. */
    int selectPublicPhotoBoardCount(BDPhotoBoardPublicSearchRequestDto search);

    /** 사용자 포토 게시판 상세를 조회한다. */
    BDPhotoBoardDetailResponseDto selectPublicPhotoBoardDetail(@Param("photoSeq") Long photoSeq);

    /** 사용자 포토 게시판 공개 파일을 조회한다. */
    BDPhotoBoardFileResponseDto selectPublicPhotoBoardFile(@Param("photoFileSeq") Long photoFileSeq);
```

- [ ] **Step 2: Add public list SQL**

Add this SQL to `src/main/resources/mapper/bd/BDPhotoBoardMapper.xml`.

```xml
    <select id="selectPublicPhotoBoardList" resultType="com.reven.project.service.bd.dto.BDPhotoBoardPublicListItemResponseDto">
        select
            m.photo_seq as photoSeq,
            m.title,
            date(m.reg_dtm) as registeredDate,
            thumb.photo_file_seq as thumbnailFileSeq,
            thumb.content_type as thumbnailContentType,
            null as thumbnailFileUrl,
            exists (
                select 1
                from bd_photo_board_file_dtl image_file
                where image_file.photo_seq = m.photo_seq
                  and image_file.delete_flg = 'N'
                  and image_file.content_type like 'image/%'
            ) as hasImage,
            exists (
                select 1
                from bd_photo_board_file_dtl video_file
                where video_file.photo_seq = m.photo_seq
                  and video_file.delete_flg = 'N'
                  and video_file.content_type like 'video/%'
            ) as hasVideo
        from bd_photo_board_mst m
        left join bd_photo_board_file_dtl thumb
          on thumb.photo_file_seq = (
              select f.photo_file_seq
              from bd_photo_board_file_dtl f
              where f.photo_seq = m.photo_seq
                and f.delete_flg = 'N'
              order by f.sort_ord asc, f.photo_file_seq asc
              limit 1
          )
        where m.delete_flg = 'N'
          and m.publish_yn = 'Y'
          <if test="keyword != null and keyword != ''">
            and m.title like concat('%', #{keyword}, '%')
          </if>
          <if test="hasMediaFilter()">
            and (
              <trim prefixOverrides="or">
                <if test="imageOnly">
                  or exists (
                      select 1
                      from bd_photo_board_file_dtl image_filter
                      where image_filter.photo_seq = m.photo_seq
                        and image_filter.delete_flg = 'N'
                        and image_filter.content_type like 'image/%'
                  )
                </if>
                <if test="videoOnly">
                  or exists (
                      select 1
                      from bd_photo_board_file_dtl video_filter
                      where video_filter.photo_seq = m.photo_seq
                        and video_filter.delete_flg = 'N'
                        and video_filter.content_type like 'video/%'
                  )
                </if>
              </trim>
            )
          </if>
        order by m.photo_seq desc
        limit #{size}
        offset #{offset}
    </select>
```

- [ ] **Step 3: Add count, detail, and public file SQL**

Add these queries below the public list query.

```xml
    <select id="selectPublicPhotoBoardCount" resultType="int">
        select count(*)
        from bd_photo_board_mst m
        where m.delete_flg = 'N'
          and m.publish_yn = 'Y'
          <if test="keyword != null and keyword != ''">
            and m.title like concat('%', #{keyword}, '%')
          </if>
          <if test="hasMediaFilter()">
            and (
              <trim prefixOverrides="or">
                <if test="imageOnly">
                  or exists (
                      select 1
                      from bd_photo_board_file_dtl image_filter
                      where image_filter.photo_seq = m.photo_seq
                        and image_filter.delete_flg = 'N'
                        and image_filter.content_type like 'image/%'
                  )
                </if>
                <if test="videoOnly">
                  or exists (
                      select 1
                      from bd_photo_board_file_dtl video_filter
                      where video_filter.photo_seq = m.photo_seq
                        and video_filter.delete_flg = 'N'
                        and video_filter.content_type like 'video/%'
                  )
                </if>
              </trim>
            )
          </if>
    </select>

    <select id="selectPublicPhotoBoardDetail" resultType="com.reven.project.service.bd.dto.BDPhotoBoardDetailResponseDto">
        select
            photo_seq as photoSeq,
            title,
            publish_yn as publishYn,
            delete_flg as deleteFlg,
            reg_dtm as registeredAt,
            reg_id as registeredBy,
            mod_dtm as modifiedAt,
            mod_id as modifiedBy
        from bd_photo_board_mst
        where photo_seq = #{photoSeq}
          and publish_yn = 'Y'
          and delete_flg = 'N'
    </select>

    <select id="selectPublicPhotoBoardFile" resultType="com.reven.project.service.bd.dto.BDPhotoBoardFileResponseDto">
        select
            f.photo_file_seq as photoFileSeq,
            f.photo_seq as photoSeq,
            f.original_file_name as originalFileName,
            f.stored_file_name as storedFileName,
            f.stored_path as storedPath,
            f.content_type as contentType,
            f.file_size as fileSize,
            f.sort_ord as sortOrder,
            f.delete_flg as deleteFlg,
            f.reg_dtm as registeredAt,
            f.reg_id as registeredBy,
            f.mod_dtm as modifiedAt,
            f.mod_id as modifiedBy,
            null as fileUrl
        from bd_photo_board_file_dtl f
        join bd_photo_board_mst m on m.photo_seq = f.photo_seq
        where f.photo_file_seq = #{photoFileSeq}
          and f.delete_flg = 'N'
          and m.publish_yn = 'Y'
          and m.delete_flg = 'N'
    </select>
```

- [ ] **Step 4: Run mapper compile check**

Run this command.

```bash
./gradlew test --tests 'com.reven.project.service.bd.BDPhotoBoardServiceTest'
```

Expected result: compile should pass or fail only because service methods are not implemented yet in tests added later. Fix XML syntax immediately if MyBatis parsing fails.

---

### Task 3: Public Service Methods And Tests

**Files:**
- Modify: `src/test/java/com/reven/project/service/bd/BDPhotoBoardServiceTest.java`
- Modify: `src/main/java/com/reven/project/service/bd/BDPhotoBoardService.java`

- [ ] **Step 1: Write failing public list test**

Add this test to `BDPhotoBoardServiceTest`.

```java
    @Test
    void publicPhotoBoardListNormalizesSearchAndBuildsThumbnailUrls() {
        BDPhotoBoardMapper mapper = mock(BDPhotoBoardMapper.class);
        when(mapper.selectPublicPhotoBoardCount(any())).thenReturn(1);
        when(mapper.selectPublicPhotoBoardList(any())).thenReturn(List.of(new BDPhotoBoardPublicListItemResponseDto(
                1L,
                "공개 포토",
                LocalDate.of(2026, 5, 30),
                11L,
                "image/jpeg",
                null,
                true,
                false
        )));

        BDPhotoBoardService service = newService(mapper);

        BDPhotoBoardPublicPageResponseDto page = service.searchPublicPhotoBoards(
                new BDPhotoBoardPublicSearchRequestDto(" 포토 ", true, true, 0, 0)
        );

        assertThat(page.search().keyword()).isEqualTo("포토");
        assertThat(page.search().page()).isEqualTo(1);
        assertThat(page.search().size()).isEqualTo(9);
        assertThat(page.totalCount()).isEqualTo(1);
        assertThat(page.totalPages()).isEqualTo(1);
        assertThat(page.photos().get(0).thumbnailFileUrl()).isEqualTo("/board/photo/file.do?photoFileSeq=11");
    }
```

Add these imports.

```java
import com.reven.project.service.bd.dto.BDPhotoBoardPublicListItemResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicPageResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicSearchRequestDto;
```

- [ ] **Step 2: Run the failing public list test**

Run this command.

```bash
./gradlew test --tests 'com.reven.project.service.bd.BDPhotoBoardServiceTest.publicPhotoBoardListNormalizesSearchAndBuildsThumbnailUrls'
```

Expected result: FAIL because `searchPublicPhotoBoards` does not exist.

- [ ] **Step 3: Implement public list service**

Add this method to `BDPhotoBoardService`.

```java
    /**
     * 사용자 포토 게시판 목록을 검색한다.
     */
    public BDPhotoBoardPublicPageResponseDto searchPublicPhotoBoards(BDPhotoBoardPublicSearchRequestDto search) {
        BDPhotoBoardPublicSearchRequestDto normalized = search == null
                ? new BDPhotoBoardPublicSearchRequestDto("", false, false, 1, 9)
                : search.normalized();
        int totalCount = photoBoardMapper.selectPublicPhotoBoardCount(normalized);
        int totalPages = totalCount == 0 ? 1 : (int) Math.ceil((double) totalCount / normalized.size());
        List<BDPhotoBoardPublicListItemResponseDto> photos = photoBoardMapper.selectPublicPhotoBoardList(normalized).stream()
                .map(this::withThumbnailUrl)
                .toList();
        return new BDPhotoBoardPublicPageResponseDto(normalized, photos, totalCount, totalPages);
    }

    private BDPhotoBoardPublicListItemResponseDto withThumbnailUrl(BDPhotoBoardPublicListItemResponseDto photo) {
        return new BDPhotoBoardPublicListItemResponseDto(
                photo.photoSeq(),
                photo.title(),
                photo.registeredDate(),
                photo.thumbnailFileSeq(),
                photo.thumbnailContentType(),
                photo.thumbnailFileSeq() == null ? "" : buildPublicFileUrl(photo.thumbnailFileSeq()),
                photo.hasImage(),
                photo.hasVideo()
        );
    }

    private String buildPublicFileUrl(Long photoFileSeq) {
        return "/board/photo/file.do?photoFileSeq=" + photoFileSeq;
    }
```

Add imports.

```java
import com.reven.project.service.bd.dto.BDPhotoBoardPublicListItemResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicPageResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicSearchRequestDto;
```

- [ ] **Step 4: Run public list test and verify pass**

Run this command.

```bash
./gradlew test --tests 'com.reven.project.service.bd.BDPhotoBoardServiceTest.publicPhotoBoardListNormalizesSearchAndBuildsThumbnailUrls'
```

Expected result: PASS.

- [ ] **Step 5: Write failing public detail and file tests**

Add these tests to `BDPhotoBoardServiceTest`.

```java
    @Test
    void publicPhotoBoardDetailReturnsPublishedPostAndPublicFileUrls() {
        BDPhotoBoardMapper mapper = mock(BDPhotoBoardMapper.class);
        when(mapper.selectPublicPhotoBoardDetail(1L)).thenReturn(detail(1L, "공개 포토"));
        when(mapper.selectPhotoBoardFiles(1L)).thenReturn(List.of(file(11L, 1L, "sample.jpg")));

        BDPhotoBoardService service = newService(mapper);

        assertThat(service.findPublicPhotoBoard(1L)).isNotNull();
        assertThat(service.findPublicPhotoBoardFiles(1L).get(0).fileUrl()).isEqualTo("/board/photo/file.do?photoFileSeq=11");
    }

    @Test
    void publicPhotoBoardFileOnlyReturnsFilesFromPublishedPosts() {
        BDPhotoBoardMapper mapper = mock(BDPhotoBoardMapper.class);
        when(mapper.selectPublicPhotoBoardFile(11L)).thenReturn(file(11L, 1L, "sample.jpg"));

        BDPhotoBoardService service = newService(mapper);

        assertThat(service.findPublicPhotoBoardFile(11L)).isNotNull();
        assertThat(service.findPublicPhotoBoardFile(999L)).isNull();
    }
```

- [ ] **Step 6: Run failing public detail and file tests**

Run this command.

```bash
./gradlew test --tests 'com.reven.project.service.bd.BDPhotoBoardServiceTest.publicPhotoBoardDetailReturnsPublishedPostAndPublicFileUrls' --tests 'com.reven.project.service.bd.BDPhotoBoardServiceTest.publicPhotoBoardFileOnlyReturnsFilesFromPublishedPosts'
```

Expected result: FAIL because public detail and file service methods do not exist.

- [ ] **Step 7: Implement public detail and file service**

Add these methods to `BDPhotoBoardService`.

```java
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
     * 사용자 포토 게시판 첨부 파일 목록을 조회한다.
     */
    public List<BDPhotoBoardFileResponseDto> findPublicPhotoBoardFiles(Long photoSeq) {
        if (photoSeq == null) {
            return List.of();
        }
        return photoBoardMapper.selectPhotoBoardFiles(photoSeq).stream()
                .map(this::withPublicFileUrl)
                .toList();
    }

    /**
     * 사용자 포토 게시판 공개 첨부 파일 단건을 조회한다.
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
        BDPhotoBoardFileResponseDto file = photoBoardMapper.selectPublicPhotoBoardFile(photoFileSeq);
        if (file == null) {
            return null;
        }
        return resolveStoredFilePath(file.storedPath(), file.storedFileName());
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
```

- [ ] **Step 8: Run service tests**

Run this command.

```bash
./gradlew test --tests 'com.reven.project.service.bd.BDPhotoBoardServiceTest'
```

Expected result: PASS.

---

### Task 4: Public Controller And Route Tests

**Files:**
- Create: `src/main/java/com/reven/project/client/bd/BDPhotoBoardPublicController.java`
- Modify: `src/test/java/com/reven/project/client/COMainControllerTest.java`

- [ ] **Step 1: Write failing controller route tests**

Add imports to `COMainControllerTest`.

```java
import com.reven.project.client.bd.BDPhotoBoardPublicController;
import com.reven.project.service.bd.BDPhotoBoardService;
import com.reven.project.service.bd.dto.BDPhotoBoardDetailResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardFileResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicListItemResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicPageResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicSearchRequestDto;
```

Add these tests to `COMainControllerTest`.

```java
    @Test
    void publicPhotoBoardListUsesSearchAndRendersPhotoList() throws Exception {
        BDPhotoBoardService photoService = mock(BDPhotoBoardService.class);
        when(photoService.searchPublicPhotoBoards(any())).thenReturn(new BDPhotoBoardPublicPageResponseDto(
                new BDPhotoBoardPublicSearchRequestDto("봄", true, false, 1, 9),
                List.of(new BDPhotoBoardPublicListItemResponseDto(
                        1L, "봄 사진", LocalDate.of(2026, 5, 30), 11L, "image/jpeg",
                        "/board/photo/file.do?photoFileSeq=11", true, false
                )),
                1,
                1
        ));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDPhotoBoardPublicController(photoService)).build();

        mvc.perform(get("/board/photo/list.do")
                        .param("keyword", "봄")
                        .param("imageOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/photo/list"))
                .andExpect(model().attributeExists("page"));
    }

    @Test
    void publicPhotoBoardDetailRedirectsAbnormalAccessWithAlertView() throws Exception {
        BDPhotoBoardService photoService = mock(BDPhotoBoardService.class);
        when(photoService.findPublicPhotoBoard(999L)).thenReturn(null);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new BDPhotoBoardPublicController(photoService)).build();

        mvc.perform(get("/board/photo/detail.do").param("photoSeq", "999"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/photo/invalid-access"))
                .andExpect(model().attribute("message", "비정상적인 접근입니다."))
                .andExpect(model().attribute("redirectUrl", "/board/photo/list.do"));
    }
```

- [ ] **Step 2: Run failing controller tests**

Run this command.

```bash
./gradlew test --tests 'com.reven.project.client.COMainControllerTest.publicPhotoBoardListUsesSearchAndRendersPhotoList' --tests 'com.reven.project.client.COMainControllerTest.publicPhotoBoardDetailRedirectsAbnormalAccessWithAlertView'
```

Expected result: FAIL because `BDPhotoBoardPublicController` does not exist.

- [ ] **Step 3: Create public controller**

Create `src/main/java/com/reven/project/client/bd/BDPhotoBoardPublicController.java` with this content.

```java
// 사용자 포토 게시판 공개 컨트롤러
package com.reven.project.client.bd;

import com.reven.project.service.bd.BDPhotoBoardService;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicSearchRequestDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BDPhotoBoardPublicController {

    private final BDPhotoBoardService photoBoardService;

    public BDPhotoBoardPublicController(BDPhotoBoardService photoBoardService) {
        this.photoBoardService = photoBoardService;
    }

    @GetMapping("/board/photo/list.do")
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean imageOnly,
            @RequestParam(defaultValue = "false") boolean videoOnly,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        model.addAttribute("page", photoBoardService.searchPublicPhotoBoards(
                new BDPhotoBoardPublicSearchRequestDto(keyword, imageOnly, videoOnly, page, 9)
        ));
        return "client/photo/list";
    }

    @GetMapping("/board/photo/detail.do")
    public String detail(@RequestParam(required = false) Long photoSeq, Model model) {
        var photo = photoBoardService.findPublicPhotoBoard(photoSeq);
        if (photo == null) {
            model.addAttribute("message", "비정상적인 접근입니다.");
            model.addAttribute("redirectUrl", "/board/photo/list.do");
            return "client/photo/invalid-access";
        }
        model.addAttribute("photo", photo);
        model.addAttribute("photoFiles", photoBoardService.findPublicPhotoBoardFiles(photoSeq));
        return "client/photo/detail";
    }

    @GetMapping("/board/photo/file.do")
    public ResponseEntity<Resource> file(@RequestParam Long photoFileSeq) throws IOException {
        var file = photoBoardService.findPublicPhotoBoardFile(photoFileSeq);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Path path = photoBoardService.resolvePublicPhotoBoardFilePath(photoFileSeq);
        if (path == null || !Files.exists(path)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        MediaType mediaType = parseMediaType(file.contentType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(Files.size(path))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(file.originalFileName(), java.nio.charset.StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(new FileSystemResource(path));
    }

    private MediaType parseMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
```

- [ ] **Step 4: Run controller tests**

Run this command.

```bash
./gradlew test --tests 'com.reven.project.client.COMainControllerTest'
```

Expected result: PASS.

---

### Task 5: Public Templates And Navigation

**Files:**
- Modify: `src/main/resources/templates/fragments/layout.html`
- Create: `src/main/resources/templates/client/photo/list.html`
- Create: `src/main/resources/templates/client/photo/detail.html`
- Create: `src/main/resources/templates/client/photo/invalid-access.html`

- [ ] **Step 1: Update public GNB**

Modify the public GNB board dropdown in `fragments/layout.html` so it contains both links.

```html
            <a th:href="@{/board/ai-news/list.do}">게시판</a>
            <div class="gnb-dropdown">
                <a th:href="@{/board/ai-news/list.do}">AI News</a>
                <a th:href="@{/board/photo/list.do}">포토 게시판</a>
            </div>
```

- [ ] **Step 2: Create public photo list template**

Create `src/main/resources/templates/client/photo/list.html` with this content.

```html
<!doctype html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/layout :: head('포토 게시판')}"></head>
<body>
<!-- screen: 포토 게시판 / purpose: 게시 상태 포토 게시글을 카드 목록으로 조회하는 화면 / created: 2026-05-30 -->
<header th:replace="~{fragments/layout :: publicGnb('board')}"></header>
<main class="public-content">
    <section class="page-head"><h1>포토 게시판</h1><p>사진과 동영상 게시물을 확인합니다.</p></section>

    <form class="search-panel public-photo-search" method="get" th:action="@{/board/photo/list.do}">
        <label class="search-label" for="keyword">제목</label>
        <input class="form-control" id="keyword" name="keyword" th:value="${page.search.keyword}" placeholder="제목 검색">
        <label class="search-check"><input type="checkbox" name="imageOnly" value="true" th:checked="${page.search.imageOnly}"> 이미지 포함</label>
        <label class="search-check"><input type="checkbox" name="videoOnly" value="true" th:checked="${page.search.videoOnly}"> 동영상 포함</label>
        <button class="btn btn-register btn-sm" type="submit">검색</button>
        <a class="btn btn-neutral btn-sm" th:href="@{/board/photo/list.do}">초기화</a>
    </form>

    <section class="content-panel">
        <div class="panel-head"><h2>포토 게시판 목록</h2><span th:text="|${page.totalCount}개|">0개</span></div>
        <div class="public-photo-grid">
            <a class="public-photo-card" th:each="photo : ${page.photos}" th:href="@{/board/photo/detail.do(photoSeq=${photo.photoSeq})}">
                <span class="public-photo-thumb">
                    <img th:if="${photo.thumbnailImage()}" th:src="${photo.thumbnailFileUrl}" th:alt="${photo.title}">
                    <video th:if="${photo.thumbnailVideo()}" th:src="${photo.thumbnailFileUrl}" muted preload="metadata"></video>
                    <span th:if="${!photo.thumbnailImage() and !photo.thumbnailVideo()}">미리보기 없음</span>
                </span>
                <strong th:text="${photo.title}">제목</strong>
                <small th:text="${photo.registeredDate}">2026-05-30</small>
            </a>
        </div>
        <p class="empty" th:if="${#lists.isEmpty(page.photos)}">현재 등록된 내용이 없습니다.</p>
        <nav class="public-pagination" th:if="${page.totalPages > 1}">
            <a class="btn btn-neutral btn-sm" th:if="${page.hasPrevious()}" th:href="@{/board/photo/list.do(keyword=${page.search.keyword},imageOnly=${page.search.imageOnly},videoOnly=${page.search.videoOnly},page=${page.previousPage()})}">이전</a>
            <span th:text="|${page.search.page} / ${page.totalPages}|">1 / 1</span>
            <a class="btn btn-neutral btn-sm" th:if="${page.hasNext()}" th:href="@{/board/photo/list.do(keyword=${page.search.keyword},imageOnly=${page.search.imageOnly},videoOnly=${page.search.videoOnly},page=${page.nextPage()})}">다음</a>
        </nav>
    </section>
</main>
<footer th:replace="~{fragments/layout :: footer}"></footer>
<th:block th:replace="~{fragments/layout :: scripts}"></th:block>
</body>
</html>
```

- [ ] **Step 3: Create public photo detail template**

Create `src/main/resources/templates/client/photo/detail.html` with this content.

```html
<!doctype html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/layout :: head(${photo?.title} ?: '포토 게시판 상세')}"></head>
<body>
<!-- screen: 포토 게시판 상세 / purpose: 게시 상태 포토 게시글 첨부를 확대 조회하는 화면 / created: 2026-05-30 -->
<header th:replace="~{fragments/layout :: publicGnb('board')}"></header>
<main class="public-content">
    <section class="page-head">
        <h1 th:text="${photo?.title} ?: '포토 게시판 상세'">포토 게시판 상세</h1>
        <p th:text="${photo?.registeredAt != null ? #temporals.format(photo.registeredAt, 'yyyy-MM-dd') : '2026-05-30'}">2026-05-30</p>
    </section>
    <section class="content-panel public-photo-detail">
        <div class="panel-head"><h2>첨부 파일</h2><span th:text="|${#lists.size(photoFiles)}개|">0개</span></div>
        <div class="public-photo-file-grid">
            <button class="public-photo-file-card"
                    type="button"
                    th:each="file : ${photoFiles}"
                    th:attr="data-public-photo-preview='true',data-media-src=${file.fileUrl},data-media-type=${file.contentType},data-media-alt=${file.originalFileName}">
                <span class="public-photo-thumb">
                    <img th:if="${file.image()}" th:src="${file.fileUrl}" th:alt="${file.originalFileName}">
                    <video th:if="${file.video()}" th:src="${file.fileUrl}" muted preload="metadata"></video>
                    <span th:if="${!file.image() and !file.video()}" th:text="${file.originalFileName}">파일</span>
                </span>
                <strong th:text="${file.originalFileName}">파일명</strong>
            </button>
        </div>
        <p class="empty" th:if="${#lists.isEmpty(photoFiles)}">등록된 첨부 파일이 없습니다.</p>
    </section>
    <div class="detail-action-bar"><a class="btn btn-neutral" th:href="@{/board/photo/list.do}">목록</a></div>
</main>
<div class="public-photo-modal" data-public-photo-modal aria-hidden="true">
    <div class="public-photo-modal-backdrop" data-public-photo-modal-close></div>
    <div class="public-photo-modal-dialog" role="dialog" aria-modal="true" aria-label="첨부 파일 확대 보기">
        <button type="button" class="photo-modal-close" data-public-photo-modal-close aria-label="닫기">×</button>
        <img class="public-photo-modal-image" data-public-photo-modal-image alt="">
        <video class="public-photo-modal-video" data-public-photo-modal-video controls></video>
    </div>
</div>
<footer th:replace="~{fragments/layout :: footer}"></footer>
<th:block th:replace="~{fragments/layout :: scripts}"></th:block>
<script th:src="@{/common/js/photo-public.js}" defer></script>
</body>
</html>
```

- [ ] **Step 4: Create invalid access template**

Create `src/main/resources/templates/client/photo/invalid-access.html` with this content.

```html
<!doctype html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/layout :: head('포토 게시판')}"></head>
<body>
<!-- screen: 포토 게시판 비정상 접근 / purpose: 비게시 또는 삭제 게시글 접근을 목록으로 돌려보내는 화면 / created: 2026-05-30 -->
<script th:inline="javascript">
    alert(/*[[${message}]]*/'비정상적인 접근입니다.');
    location.replace(/*[[${redirectUrl}]]*/'/board/photo/list.do');
</script>
</body>
</html>
```

- [ ] **Step 5: Run template route tests**

Run this command.

```bash
./gradlew test --tests 'com.reven.project.client.COMainControllerTest'
```

Expected result: PASS.

---

### Task 6: Public Photo Styling And Modal Script

**Files:**
- Modify: `src/main/resources/static/common/css/app.css`
- Create: `src/main/resources/static/common/js/photo-public.js`

- [ ] **Step 1: Add public photo CSS**

Append this CSS near the existing public news and photo styles in `app.css`.

```css
.public-photo-search {
    grid-template-columns: 64px minmax(0, 1fr) auto auto auto auto;
    align-items: center;
    background: var(--public-cream);
}

.search-check {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    margin: 0;
    white-space: nowrap;
    font-weight: 800;
    color: var(--public-ink);
}

.search-check input {
    width: 16px;
    height: 16px;
    margin: 0;
}

.public-photo-grid,
.public-photo-file-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 18px;
}

.public-photo-card,
.public-photo-file-card {
    display: flex;
    flex-direction: column;
    gap: 10px;
    min-width: 0;
    padding: 0 0 14px;
    border: 1px solid rgba(201, 154, 66, 0.35);
    border-radius: var(--radius-public-box);
    background: #fff;
    color: var(--public-ink);
    text-align: left;
    text-decoration: none;
    overflow: hidden;
    box-shadow: var(--public-shadow);
}

.public-photo-file-card {
    border: 1px solid rgba(201, 154, 66, 0.35);
    cursor: zoom-in;
}

.public-photo-thumb {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    aspect-ratio: 1 / 1;
    background: #f8fbfe;
    color: #64748b;
    font-size: 14px;
    font-weight: 800;
    overflow: hidden;
}

.public-photo-thumb img,
.public-photo-thumb video {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.public-photo-card strong,
.public-photo-file-card strong {
    padding: 0 14px;
    color: var(--public-ink);
    font-size: 16px;
    line-height: 1.45;
    word-break: break-word;
}

.public-photo-card small {
    padding: 0 14px;
    color: var(--color-muted);
    font-weight: 800;
}

.public-pagination {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    margin-top: 22px;
}

.public-photo-modal {
    position: fixed;
    inset: 0;
    z-index: 130;
    display: none;
    align-items: center;
    justify-content: center;
    padding: 24px;
}

.public-photo-modal.is-open {
    display: flex;
}

.public-photo-modal-backdrop {
    position: absolute;
    inset: 0;
    background: rgba(12, 18, 28, 0.78);
}

.public-photo-modal-dialog {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    max-width: min(96vw, 1600px);
    max-height: 92vh;
}

.public-photo-modal-image,
.public-photo-modal-video {
    display: none;
    max-width: 96vw;
    max-height: 92vh;
    width: auto;
    height: auto;
    border-radius: 6px;
    background: #0f1720;
    box-shadow: 0 18px 48px rgba(0, 0, 0, 0.42);
}

.public-photo-modal-image.is-active,
.public-photo-modal-video.is-active {
    display: block;
}
```

Also add this inside the existing mobile media query.

```css
    .public-photo-search {
        grid-template-columns: 1fr;
    }

    .public-photo-grid,
    .public-photo-file-grid {
        grid-template-columns: 1fr;
    }
```

- [ ] **Step 2: Create public photo modal script**

Create `src/main/resources/static/common/js/photo-public.js` with this content.

```javascript
(function () {
    document.addEventListener('DOMContentLoaded', function () {
        const modal = document.querySelector('[data-public-photo-modal]');
        const image = modal ? modal.querySelector('[data-public-photo-modal-image]') : null;
        const video = modal ? modal.querySelector('[data-public-photo-modal-video]') : null;
        const closeControls = modal ? modal.querySelectorAll('[data-public-photo-modal-close]') : [];

        function clearMedia() {
            if (image) {
                image.classList.remove('is-active');
                image.removeAttribute('src');
                image.setAttribute('alt', '');
            }
            if (video) {
                video.pause();
                video.classList.remove('is-active');
                video.removeAttribute('src');
                video.load();
            }
        }

        function closeModal() {
            if (!modal) {
                return;
            }
            clearMedia();
            modal.classList.remove('is-open');
            modal.setAttribute('aria-hidden', 'true');
            document.body.classList.remove('has-photo-modal');
        }

        function openModal(src, mediaType, alt) {
            if (!modal || !image || !video) {
                return;
            }
            clearMedia();
            if (mediaType && mediaType.indexOf('video/') === 0) {
                video.src = src;
                video.classList.add('is-active');
            } else {
                image.src = src;
                image.alt = alt || '';
                image.classList.add('is-active');
            }
            modal.classList.add('is-open');
            modal.setAttribute('aria-hidden', 'false');
            document.body.classList.add('has-photo-modal');
        }

        document.querySelectorAll('[data-public-photo-preview]').forEach(function (trigger) {
            trigger.addEventListener('click', function () {
                openModal(
                        trigger.getAttribute('data-media-src'),
                        trigger.getAttribute('data-media-type'),
                        trigger.getAttribute('data-media-alt')
                );
            });
        });

        closeControls.forEach(function (control) {
            control.addEventListener('click', closeModal);
        });

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closeModal();
            }
        });
    });
})();
```

- [ ] **Step 3: Run full focused tests**

Run this command.

```bash
./gradlew test --tests 'com.reven.project.service.bd.*' --tests 'com.reven.project.client.COMainControllerTest'
```

Expected result: PASS.

---

### Task 7: Worklog, Checklist, And Final Verification

**Files:**
- Modify: `docs/worklog.md`
- Modify: `docs/checklist.md`

- [ ] **Step 1: Update worklog**

Append this entry to `docs/worklog.md`.

```markdown

## 2026-05-30 사용자 포토 게시판 추가

- 사용자 GNB의 게시판 드롭다운을 `AI News`와 `포토 게시판`으로 확장했다.
- 관리자 포토 게시판 데이터를 재사용해 게시 상태 포토 게시글을 사용자 목록과 상세 화면에 노출했다.
- 목록은 9개 단위 카드형 썸네일 그리드로 구성하고 제목, 이미지 포함, 동영상 포함 검색을 제공했다.
- 파일 유형은 별도 컬럼 없이 기존 첨부 `content_type`으로 판단했다.
- 상세 첨부는 딤드 레이어 팝업에서 이미지 확대와 동영상 재생을 지원한다.
- 비게시, 삭제, 존재하지 않는 상세 접근은 알럿 후 목록으로 이동하고 공개 파일 직접 접근은 404로 차단한다.
```

- [ ] **Step 2: Update checklist**

In `docs/checklist.md`, tick completed implementation items after code and tests pass.

```markdown
- [x] 구현 계획 작성.
- [x] 사용자 포토 게시판 컨트롤러, 서비스, 매퍼 구현.
- [x] 사용자 포토 게시판 템플릿, CSS, JS 구현.
- [x] 서비스와 컨트롤러 테스트 추가.
- [x] 관련 테스트 실행.
```

- [ ] **Step 3: Run required verification**

Run this command.

```bash
./gradlew test --tests 'com.reven.project.service.bd.*' --tests 'com.reven.project.client.COMainControllerTest'
```

Expected result: PASS.

- [ ] **Step 4: Run broader project tests**

Run this command.

```bash
./gradlew test
```

Expected result: PASS.

- [ ] **Step 5: Inspect final diff**

Run this command.

```bash
git diff --stat
```

Expected result: only user photo board implementation, worklog, and checklist files changed. Existing untracked `croll/ai-news/*.json` files may still appear in `git status` and should not be included unless the user asks.
