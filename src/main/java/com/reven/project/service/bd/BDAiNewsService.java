package com.reven.project.service.bd;

import com.reven.project.service.bd.dto.BDAiNewsDetailResponseDto;
import com.reven.project.service.bd.dto.BDAiNewsPageResponseDto;
import com.reven.project.service.bd.dto.BDAiNewsSaveRequestDto;
import com.reven.project.service.bd.dto.BDAiNewsSearchRequestDto;
import com.reven.project.service.bd.mapper.BDAiNewsMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BDAiNewsService {

    private final BDAiNewsMapper aiNewsMapper;

    public BDAiNewsService(BDAiNewsMapper aiNewsMapper) {
        this.aiNewsMapper = aiNewsMapper;
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
     * 검색 조건이 비어 있을 때 화면 기본값과 같은 날짜/상태/페이징 조건을 채운다.
     */
    private BDAiNewsSearchRequestDto normalizeSearch(BDAiNewsSearchRequestDto search) {
        LocalDate endDate = search.endDate() == null ? LocalDate.now().plusDays(1) : search.endDate();
        LocalDate startDate = search.startDate() == null ? LocalDate.now().minusDays(60) : search.startDate();
        String keywordType = List.of("all", "title", "tag", "status").contains(search.keywordType())
                ? search.keywordType()
                : "all";
        List<String> statuses = search.statuses() == null || search.statuses().isEmpty()
                ? List.of("N", "P", "Y", "E")
                : search.statuses();
        int limit = search.limit() <= 0 ? 10 : search.limit();
        int offset = Math.max(0, search.offset());
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
        String status = firstText(requestDto.status(), existing == null ? null : existing.status(), "P");
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
}
