// AI News 상태 정규화와 검색 기본값을 검증한다.
package com.reven.project.service.bd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reven.project.service.bd.dto.BDAiNewsDetailResponseDto;
import com.reven.project.service.bd.dto.BDAiNewsCrawlResultDto;
import com.reven.project.service.bd.dto.BDAiNewsListItemResponseDto;
import com.reven.project.service.bd.dto.BDAiNewsSaveRequestDto;
import com.reven.project.service.bd.dto.BDAiNewsSearchRequestDto;
import com.reven.project.service.bd.mapper.BDAiNewsMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BDAiNewsServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void searchAiNewsDefaultsToCurrentVisibleStatusesWithoutN() {
        BDAiNewsMapper mapper = mock(BDAiNewsMapper.class);
        when(mapper.countAiNews(any())).thenReturn(0L);
        when(mapper.selectAiNewsList(any())).thenReturn(List.of());

        BDAiNewsService service = new BDAiNewsService(mapper, new com.fasterxml.jackson.databind.ObjectMapper());

        service.searchAiNews(new BDAiNewsSearchRequestDto(null, null, null, null, null, null, null));

        ArgumentCaptor<BDAiNewsSearchRequestDto> captor = ArgumentCaptor.forClass(BDAiNewsSearchRequestDto.class);
        verify(mapper).countAiNews(captor.capture());
        assertThat(captor.getValue().statuses()).containsExactly("P", "Y", "E");
    }

    @Test
    void saveAiNewsNormalizesIncomingNStatusToP() {
        BDAiNewsMapper mapper = mock(BDAiNewsMapper.class);
        BDAiNewsService service = new BDAiNewsService(mapper, new com.fasterxml.jackson.databind.ObjectMapper());

        BDAiNewsSaveRequestDto request = new BDAiNewsSaveRequestDto(
                null,
                "ai-news-sample",
                "샘플",
                "AI News",
                "요약",
                "본문",
                "[]",
                "",
                LocalDate.of(2026, 5, 30),
                "N",
                "admin"
        );

        service.saveAiNews(request);

        ArgumentCaptor<BDAiNewsSaveRequestDto> captor = ArgumentCaptor.forClass(BDAiNewsSaveRequestDto.class);
        verify(mapper).insertAiNews(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo("P");
    }

    @Test
    void listItemAndDetailStatusTextUsePipelineLabels() {
        BDAiNewsListItemResponseDto listItem = new BDAiNewsListItemResponseDto(
                1L,
                "제목",
                "slug",
                "AI News",
                "P",
                LocalDate.of(2026, 5, 30),
                LocalDate.of(2026, 5, 29),
                LocalDate.of(2026, 5, 30)
        );
        BDAiNewsDetailResponseDto detail = new BDAiNewsDetailResponseDto(
                1L,
                "slug",
                "제목",
                "AI News",
                "요약",
                "본문",
                "[]",
                "",
                "E",
                "N",
                LocalDate.of(2026, 5, 30),
                null,
                null,
                LocalDateTime.of(2026, 5, 30, 9, 0),
                "admin",
                LocalDateTime.of(2026, 5, 30, 10, 0),
                "admin",
                0L
        );

        assertThat(listItem.statusText()).isEqualTo("처리중");
        assertThat(detail.statusText()).isEqualTo("에러");
    }

    @Test
    void crawlLegacyJsonFilesConsumesPendingFilesAndKeepsPublishedNewsPublished() throws Exception {
        Path crawlDirectory = tempDir.resolve("croll/ai-news");
        Files.createDirectories(crawlDirectory);
        writeLegacyJson(crawlDirectory.resolve("fresh.json"), "N", "fresh-slug", "새 글", "2026-05-24T09:17:16+09:00");
        writeLegacyJson(crawlDirectory.resolve("published.json"), "N", "published-slug", "기존 게시", "2026-05-24T09:17:16+09:00");
        writeLegacyJson(crawlDirectory.resolve("consumed.json"), "P", "consumed-slug", "이미 사용됨", "2026-05-24T09:17:16+09:00");

        BDAiNewsMapper mapper = mock(BDAiNewsMapper.class);
        ObjectMapper objectMapper = new ObjectMapper();
        BDAiNewsService service = new BDAiNewsService(mapper, objectMapper);

        BDAiNewsDetailResponseDto freshSaved = detail(11L, "fresh-slug", "새 글", "P");
        BDAiNewsDetailResponseDto published = detail(42L, "published-slug", "기존 게시", "Y");

        when(mapper.selectAiNewsBySlug("fresh-slug")).thenReturn(null, freshSaved);
        when(mapper.selectAiNewsBySlug("published-slug")).thenReturn(published);
        when(mapper.insertAiNews(any())).thenReturn(1);

        String previousUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            BDAiNewsCrawlResultDto result = service.crawlLegacyJsonFiles("admin");

            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(2);
            assertThat(result.failed()).isEqualTo(0);
        } finally {
            System.setProperty("user.dir", previousUserDir);
        }

        ArgumentCaptor<BDAiNewsSaveRequestDto> insertCaptor = ArgumentCaptor.forClass(BDAiNewsSaveRequestDto.class);
        verify(mapper, times(1)).insertAiNews(insertCaptor.capture());
        verify(mapper, never()).updateAiNews(any());
        assertThat(insertCaptor.getValue().title()).isEqualTo("[2026-05-24] 새 글");

        JsonNode freshJson = readLegacyJson(crawlDirectory.resolve("fresh.json"));
        JsonNode publishedJson = readLegacyJson(crawlDirectory.resolve("published.json"));
        JsonNode consumedJson = readLegacyJson(crawlDirectory.resolve("consumed.json"));

        assertThat(freshJson.path("status").asText()).isEqualTo("P");
        assertThat(freshJson.path("inserted_at").asText()).isNotBlank();
        assertThat(freshJson.path("error").isNull()).isTrue();

        assertThat(publishedJson.path("status").asText()).isEqualTo("P");
        assertThat(publishedJson.path("inserted_at").asText()).isNotBlank();
        assertThat(publishedJson.path("error").isNull()).isTrue();

        assertThat(consumedJson.path("status").asText()).isEqualTo("P");
    }

    private void writeLegacyJson(Path path, String status, String slug, String title, String publishedAt) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        var node = objectMapper.createObjectNode();
        node.put("status", status);
        node.put("slug", slug);
        node.put("title", title);
        node.put("category", "AI News");
        node.put("summary", title + " 요약");
        node.put("content_markdown", title + " 본문");
        node.put("published_at", publishedAt);
        node.putArray("tags");
        node.putArray("sources");
        objectMapper.writeValue(path.toFile(), node);
    }

    private JsonNode readLegacyJson(Path path) throws IOException {
        return new ObjectMapper().readTree(path.toFile());
    }

    private BDAiNewsDetailResponseDto detail(Long newsSeq, String slug, String title, String status) {
        return new BDAiNewsDetailResponseDto(
                newsSeq,
                slug,
                title,
                "AI News",
                "요약",
                "본문",
                "[]",
                "[]",
                status,
                "N",
                LocalDate.of(2026, 5, 30),
                null,
                null,
                LocalDateTime.of(2026, 5, 30, 9, 0),
                "admin",
                LocalDateTime.of(2026, 5, 30, 10, 0),
                "admin",
                0L
        );
    }
}
