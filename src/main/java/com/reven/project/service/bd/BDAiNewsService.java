package com.reven.project.service.bd;

import com.reven.project.service.bd.dto.BDAiNewsDetailResponseDto;
import com.reven.project.service.bd.dto.BDAiNewsCrawlResultDto;
import com.reven.project.service.bd.dto.BDAiNewsPageResponseDto;
import com.reven.project.service.bd.dto.BDAiNewsSaveRequestDto;
import com.reven.project.service.bd.dto.BDAiNewsSearchRequestDto;
import com.reven.project.service.bd.mapper.BDAiNewsMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BDAiNewsService {

    private final BDAiNewsMapper aiNewsMapper;
    private final ObjectMapper objectMapper;

    public BDAiNewsService(BDAiNewsMapper aiNewsMapper, ObjectMapper objectMapper) {
        this.aiNewsMapper = aiNewsMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * AI News 목록을 검색 조건과 페이징 조건으로 조회한다.
     */
    public BDAiNewsPageResponseDto searchAiNews(BDAiNewsSearchRequestDto search) {
        BDAiNewsSearchRequestDto normalized = normalizeSearch(search);
        return new BDAiNewsPageResponseDto(
                normalized,
                aiNewsMapper.countAiNews(normalized),
                aiNewsMapper.selectAiNewsList(normalized)
        );
    }

    /**
     * AI News 단건 상세를 조회한다.
     */
    public BDAiNewsDetailResponseDto findAiNews(Long newsSeq) {
        return aiNewsMapper.selectAiNewsDetail(newsSeq);
    }

    /**
     * 사용자 메인 화면에 노출할 게시 상태 AI News 최신 목록을 조회한다.
     */
    public List<BDAiNewsDetailResponseDto> findPublishedAiNews(int limit) {
        return aiNewsMapper.selectPublishedAiNewsList(null, Math.max(1, limit));
    }

    /**
     * 사용자 AI News 목록에서 제목과 내용 기준으로 게시 글을 검색한다.
     */
    public List<BDAiNewsDetailResponseDto> searchPublishedAiNews(String keyword) {
        return aiNewsMapper.selectPublishedAiNewsList(keyword, 100);
    }

    /**
     * 사용자 AI News 상세에서 게시 상태인 글만 조회한다.
     */
    public BDAiNewsDetailResponseDto findPublishedAiNewsDetail(Long newsSeq) {
        return aiNewsMapper.selectPublishedAiNewsDetail(newsSeq);
    }

    /**
     * AI News 조회수를 1 증가시킨다.
     */
    @Transactional
    public void increaseViewCount(Long newsSeq) {
        if (newsSeq != null) {
            aiNewsMapper.increaseViewCount(newsSeq);
        }
    }

    /**
     * AI News를 등록하거나 기존 원고를 수정한다.
     */
    @Transactional
    public Long saveAiNews(BDAiNewsSaveRequestDto requestDto) {
        BDAiNewsSaveRequestDto normalized = normalizeSaveRequest(requestDto);
        if (normalized.newsSeq() == null) {
            aiNewsMapper.insertAiNews(normalized);
            BDAiNewsDetailResponseDto saved = aiNewsMapper.selectAiNewsBySlug(normalized.slug());
            return saved == null ? null : saved.newsSeq();
        }
        aiNewsMapper.updateAiNews(normalized);
        return normalized.newsSeq();
    }

    /**
     * AI News를 soft delete 처리한다.
     */
    @Transactional
    public void deleteAiNews(Long newsSeq) {
        aiNewsMapper.deleteAiNews(newsSeq);
    }

    /**
     * 레거시 JSON 디렉터리에서 AI News 파일을 읽어 DB에 반영한다.
     */
    @Transactional
    public BDAiNewsCrawlResultDto crawlLegacyJsonFiles(String actorId) {
        Path crawlDirectory = resolveCrawlDirectory();
        if (!Files.isDirectory(crawlDirectory)) {
            return new BDAiNewsCrawlResultDto(0, 0, 0);
        }

        List<Path> jsonFiles;
        try (var stream = Files.list(crawlDirectory)) {
            jsonFiles = stream
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("AI News JSON 디렉터리를 읽을 수 없습니다.", exception);
        }

        int success = 0;
        int failed = 0;
        int total = 0;

        for (Path path : jsonFiles) {
            JsonNode payload = null;
            try {
                payload = objectMapper.readTree(path.toFile());
                if (!"N".equalsIgnoreCase(text(payload, "status", "N"))) {
                    continue;
                }

                total++;
                String slug = text(payload, "slug", generatedSlug());
                BDAiNewsDetailResponseDto existing = aiNewsMapper.selectAiNewsBySlug(slug);
                if (existing != null && "Y".equalsIgnoreCase(existing.status())) {
                    markLegacyJsonFile(path, payload, "P", existing.newsSeq(), null);
                    success++;
                    continue;
                }

                BDAiNewsSaveRequestDto request = new BDAiNewsSaveRequestDto(
                        null,
                        slug,
                        formatCrawlTitle(text(payload, "title", "제목 없음"), parsePublishedDate(payload.path("published_at"))),
                        text(payload, "category", "AI News"),
                        text(payload, "summary", ""),
                        text(payload, "content_markdown", text(payload, "content", "")),
                        toJsonArrayString(payload.path("tags")),
                        toJsonArrayString(payload.path("sources")),
                        parsePublishedDate(payload.path("published_at")),
                        "P",
                        actorId
                );
                Long savedSeq = upsertAiNews(request, existing);
                markLegacyJsonFile(path, payload, "P", savedSeq, null);
                success++;
            } catch (Exception exception) {
                failed++;
                if (payload != null) {
                    try {
                        markLegacyJsonFile(path, payload, "E", null, exception.getMessage());
                    } catch (Exception ignored) {
                        // 원본 파일 상태 갱신 실패는 크롤링 실패를 덮어쓰지 않는다.
                    }
                }
            }
        }

        return new BDAiNewsCrawlResultDto(total, success, failed);
    }

    /**
     * 검색 조건이 비어 있을 때 화면 기본값과 같은 날짜/상태/페이징 조건을 채운다.
     */
    private BDAiNewsSearchRequestDto normalizeSearch(BDAiNewsSearchRequestDto search) {
        LocalDate endDate = search.endDate() == null ? LocalDate.now().plusDays(1) : search.endDate();
        LocalDate startDate = search.startDate() == null ? LocalDate.now().minusDays(60) : search.startDate();
        String keywordType = search.keywordType() != null && List.of("all", "title", "tag", "status").contains(search.keywordType())
                ? search.keywordType()
                : "all";
        List<String> statuses = normalizeStatuses(search.statuses());
        int limit = search.limit() == null || search.limit() <= 0 ? 10 : search.limit();
        int offset = search.offset() == null || search.offset() < 0 ? 0 : search.offset();
        return new BDAiNewsSearchRequestDto(
                startDate,
                endDate,
                keywordType,
                search.keyword(),
                statuses,
                offset,
                limit
        );
    }

    /**
     * 등록/수정 요청에 비어 있는 값이 있으면 기존 값 또는 기본값으로 보정한다.
     */
    private BDAiNewsSaveRequestDto normalizeSaveRequest(BDAiNewsSaveRequestDto requestDto) {
        BDAiNewsDetailResponseDto existing = requestDto.newsSeq() == null
                ? null
                : aiNewsMapper.selectAiNewsDetail(requestDto.newsSeq());
        // 수정 화면에서 일부 필드만 넘어와도 기존 값을 유지하도록 보정한다.
        String title = firstText(requestDto.title(), existing == null ? null : existing.title(), "제목 없음");
        String slug = firstText(requestDto.slug(), existing == null ? null : existing.slug(), generatedSlug());
        String category = firstText(requestDto.category(), existing == null ? null : existing.category(), "AI News");
        String summary = firstText(requestDto.summary(), existing == null ? null : existing.summary(), title);
        String content = firstText(requestDto.content(), existing == null ? null : existing.content(), "");
        String status = normalizeAiNewsStatus(firstText(requestDto.status(), existing == null ? null : existing.status(), "P"));
        LocalDate publishedDate = requestDto.publishedDate() != null
                ? requestDto.publishedDate()
                : existing == null ? LocalDate.now() : existing.publishedDate();
        return new BDAiNewsSaveRequestDto(
                requestDto.newsSeq(),
                slug,
                title,
                category,
                summary,
                content,
                firstText(requestDto.tagsJson(), existing == null ? null : existing.tagsJson(), "[]"),
                firstText(requestDto.sourcesJson(), existing == null ? null : existing.sourcesJson(), ""),
                publishedDate,
                status,
                firstText(requestDto.actorId(), "system")
        );
    }

    /**
     * slug 기준으로 기존 원고가 있으면 수정하고, 없으면 새로 등록한다.
     */
    private Long upsertAiNews(BDAiNewsSaveRequestDto requestDto, BDAiNewsDetailResponseDto existing) {
        BDAiNewsSaveRequestDto payload = new BDAiNewsSaveRequestDto(
                existing == null ? null : existing.newsSeq(),
                requestDto.slug(),
                requestDto.title(),
                requestDto.category(),
                requestDto.summary(),
                requestDto.content(),
                requestDto.tagsJson(),
                requestDto.sourcesJson(),
                requestDto.publishedDate(),
                requestDto.status(),
                requestDto.actorId()
        );
        return saveAiNews(payload);
    }

    private void markLegacyJsonFile(Path path, JsonNode payload, String status, Long newsSeq, String error) throws IOException {
        ObjectNode file = payload != null && payload.isObject()
                ? ((ObjectNode) payload).deepCopy()
                : objectMapper.createObjectNode();
        String normalizedStatus = status == null || status.isBlank() ? "P" : status.trim().toUpperCase();
        file.put("status", normalizedStatus);
        if (newsSeq != null && newsSeq > 0) {
            file.put("news_seq", newsSeq);
        }
        if ("E".equals(normalizedStatus)) {
            file.put("error", error == null ? "" : error);
        } else {
            file.put("inserted_at", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            file.putNull("error");
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), file);
    }

    private List<String> normalizeStatuses(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of("P", "Y", "E");
        }

        List<String> normalizedStatuses = new ArrayList<>();
        for (String status : statuses) {
            String normalizedStatus = normalizeAiNewsStatus(status);
            if (!normalizedStatuses.contains(normalizedStatus)) {
                normalizedStatuses.add(normalizedStatus);
            }
        }
        return normalizedStatuses.isEmpty() ? List.of("P", "Y", "E") : normalizedStatuses;
    }

    private String normalizeAiNewsStatus(String value) {
        if (value == null || value.isBlank()) {
            return "P";
        }

        return switch (value.trim().toUpperCase()) {
            case "N" -> "P";
            case "P", "Y", "E" -> value.trim().toUpperCase();
            default -> "P";
        };
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String generatedSlug() {
        return "news-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String formatCrawlTitle(String title, LocalDate publishedDate) {
        String normalizedTitle = title == null ? "" : title.trim();
        String prefix = "[" + publishedDate + "] ";
        if (normalizedTitle.startsWith(prefix)) {
            return normalizedTitle;
        }
        return prefix + normalizedTitle;
    }

    /**
     * AI News 크롤 JSON 디렉터리(croll/ai-news) 위치를 찾는다.
     */
    private Path resolveCrawlDirectory() {
        Path repositoryPath = Paths.get(System.getProperty("user.dir"), "croll", "ai-news");
        return Files.exists(repositoryPath) ? repositoryPath : Paths.get("croll", "ai-news");
    }

    private String text(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? defaultValue : text;
    }

    private String toJsonArrayString(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return "[]";
        }
        if (node.isArray()) {
            try {
                return objectMapper.writeValueAsString(node);
            } catch (Exception exception) {
                return "[]";
            }
        }
        return node.asText("[]");
    }

    private LocalDate parsePublishedDate(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return LocalDate.now();
        }
        String value = node.asText("");
        if (value.isBlank()) {
            return LocalDate.now();
        }
        try {
            return OffsetDateTime.parse(value).toLocalDate();
        } catch (Exception ignored) {
            try {
                return LocalDate.parse(value.substring(0, Math.min(value.length(), 10)));
            } catch (Exception ignoredAgain) {
                return LocalDate.now();
            }
        }
    }
}
