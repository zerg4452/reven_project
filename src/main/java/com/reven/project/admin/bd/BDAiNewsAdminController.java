package com.reven.project.admin.bd;

import com.reven.project.service.bd.dto.BDAiNewsSaveRequestDto;
import com.reven.project.service.bd.dto.BDAiNewsSearchRequestDto;
import com.reven.project.service.bd.BDAiNewsService;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/admin/news/ai-news", "/admin/news"})
public class BDAiNewsAdminController {

    private final BDAiNewsService aiNewsService;

    public BDAiNewsAdminController(BDAiNewsService aiNewsService) {
        this.aiNewsService = aiNewsService;
    }

    /**
     * AI News 목록 화면을 검색 조건과 함께 조회한다.
     */
    @GetMapping
    public String list(
            @ModelAttribute("search") BDAiNewsSearchRequestDto search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model
    ) {
        if (dateFrom != null || dateTo != null) {
            search = new BDAiNewsSearchRequestDto(
                    dateFrom,
                    dateTo,
                    search.keywordType(),
                    search.keyword(),
                    search.statuses(),
                    search.offset(),
                    search.limit()
            );
        }
        var page = aiNewsService.searchAiNews(search);
        model.addAttribute("page", page);
        model.addAttribute("newsList", page.news());
        model.addAttribute("totalCount", page.totalCount());
        model.addAttribute("dateFrom", page.search().startDate());
        model.addAttribute("dateTo", page.search().endDate());
        model.addAttribute("keyword", page.search().keyword());
        return "admin/news/list";
    }

    /**
     * AI News 신규 등록 화면을 표시한다.
     */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("news", null);
        return "admin/news/edit";
    }

    /**
     * AI News 원고를 새로 저장한다.
     */
    @PostMapping
    public String create(@ModelAttribute("news") BDAiNewsSaveRequestDto requestDto, Principal principal) {
        Long newsSeq = aiNewsService.saveAiNews(withActor(requestDto, principal));
        return "redirect:/admin/news/ai-news/" + newsSeq;
    }

    /**
     * AI News 상세 화면을 조회한다.
     */
    @GetMapping("/{newsSeq}")
    public String detail(@PathVariable Long newsSeq, Model model) {
        model.addAttribute("news", aiNewsService.findAiNews(newsSeq));
        return "admin/news/detail";
    }

    /**
     * AI News 수정 화면을 조회한다.
     */
    @GetMapping("/{newsSeq}/edit")
    public String editForm(@PathVariable Long newsSeq, Model model) {
        model.addAttribute("news", aiNewsService.findAiNews(newsSeq));
        return "admin/news/edit";
    }

    /**
     * AI News 원고와 게시 상태를 수정한다.
     */
    @PostMapping("/{newsSeq}")
    public String update(
            @PathVariable Long newsSeq,
            @ModelAttribute("news") BDAiNewsSaveRequestDto requestDto,
            Principal principal
    ) {
        aiNewsService.saveAiNews(withActor(new BDAiNewsSaveRequestDto(
                newsSeq,
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
        ), principal));
        return "redirect:/admin/news/ai-news/" + newsSeq;
    }

    /**
     * AI News를 화면에서 제외하도록 soft delete 처리한다.
     */
    @PostMapping("/{newsSeq}/delete")
    public String delete(@PathVariable Long newsSeq) {
        aiNewsService.deleteAiNews(newsSeq);
        return "redirect:/admin/news/ai-news";
    }

    /**
     * 등록/수정 작업자를 DTO에 보강한다.
     */
    private BDAiNewsSaveRequestDto withActor(BDAiNewsSaveRequestDto requestDto, Principal principal) {
        String actorId = principal == null ? "system" : principal.getName();
        return new BDAiNewsSaveRequestDto(
                requestDto.newsSeq(),
                requestDto.slug(),
                requestDto.title(),
                requestDto.category(),
                requestDto.summary(),
                requestDto.content(),
                requestDto.tagsJson(),
                requestDto.sourcesJson(),
                requestDto.publishedDate(),
                requestDto.status(),
                actorId
        );
    }
}
